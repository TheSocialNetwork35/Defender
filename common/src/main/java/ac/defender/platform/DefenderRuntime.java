// SPDX-License-Identifier: GPL-3.0-only
package ac.defender.platform;

import ac.defender.firewall.CombatWindow;
import ac.defender.firewall.PacketWindow;
import ac.defender.firewall.PayloadDecoder;
import ac.defender.intelligence.ClientIntelligence;
import ac.defender.intelligence.Signature;
import ac.defender.intelligence.Signatures;
import ac.defender.logging.IncidentLog;
import ac.defender.risk.Category;
import ac.defender.risk.Evidence;
import ac.defender.risk.RiskEngine;
import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.LogUtil;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.configuration.client.WrapperConfigClientPluginMessage;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPluginMessage;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Small common-platform bridge. No Bukkit world access on Netty or Folia threads. */
public final class DefenderRuntime extends PacketListenerAbstract {
    public static final DefenderRuntime INSTANCE = new DefenderRuntime();
    private record State(ClientIntelligence.Session session, PacketWindow packets, CombatWindow combat) {
        State() { this(new ClientIntelligence.Session(), new PacketWindow(), new CombatWindow()); }
    }
    private final Map<UUID, State> sessions = new ConcurrentHashMap<>();
    private final ClientIntelligence intelligence = new ClientIntelligence(List.of());
    private final RiskEngine risk = new RiskEngine();
    private volatile IncidentLog log;
    private volatile boolean active;
    private volatile boolean diagnostics;
    // Updated off the packet thread; stale health is treated as unknown.
    private volatile double tps = -1;
    private volatile long healthAt;
    private Path directory;

    private DefenderRuntime() {}

    public void start() {
        directory = GrimAPI.INSTANCE.getGrimPlugin().getDataFolder().toPath().resolve("defender");
        try {
            Files.createDirectories(directory);
            copyDefault("client-signatures.yml"); copyDefault("defender.yml");
            reload();
            active = true;
            PacketEvents.getAPI().getEventManager().registerListener(this);
            GrimAPI.INSTANCE.getScheduler().getAsyncScheduler().runAtFixedRate(GrimAPI.INSTANCE.getGrimPlugin(), () -> {
                if (!active) return;
                try {
                    tps = GrimAPI.INSTANCE.getPlatformServer().getTPS();
                    healthAt = System.nanoTime();
                } catch (RuntimeException ex) { tps = -1; }
            }, 20, 20);
            LogUtil.info("Defender experimental modules enabled; observation only. Upstream: GrimAC.");
        } catch (IOException | RuntimeException ex) {
            LogUtil.error("Defender startup failed; Defender features unavailable", ex);
        }
    }

    private void copyDefault(String name) throws IOException {
        Path file = directory.resolve(name);
        if (Files.notExists(file)) try (InputStream in = getClass().getResourceAsStream("/" + name)) {
            if (in == null) throw new IOException("Missing default " + name);
            Files.copy(in, file);
        }
    }

    public synchronized void reload() throws IOException {
        // Parse everything before replacing working configuration.
        List<Signature> signatures;
        try (InputStream in = Files.newInputStream(directory.resolve("client-signatures.yml"))) { signatures = Signatures.load(in); }
        LoaderOptions options = new LoaderOptions(); options.setAllowDuplicateKeys(false);
        options.setMaxAliasesForCollections(0); options.setCodePointLimit(8192);
        Object loaded;
        try (InputStream in = Files.newInputStream(directory.resolve("defender.yml"))) {
            loaded = new Yaml(new SafeConstructor(options)).load(in);
        }
        if (!(loaded instanceof Map<?, ?> config) || !(config.get("incident-log") instanceof Boolean logging)
                || !(config.get("packet-diagnostics") instanceof Boolean packetDiagnostics)) throw new IOException("Invalid defender.yml");
        IncidentLog next = logging ? (log == null ? new IncidentLog(directory.resolve("incidents")) : log) : null;
        IncidentLog old = log;
        intelligence.replace(signatures); diagnostics = packetDiagnostics; log = next;
        if (old != null && old != next) old.close();
    }

    public synchronized void stop() {
        active = false;
        PacketEvents.getAPI().getEventManager().unregisterListener(this);
        IncidentLog old = log; log = null;
        if (old != null) old.close();
        sessions.clear();
    }

    public void begin(GrimPlayer player) {
        if (active) sessions.put(player.uuid, new State());
    }

    public void forget(UUID player) { sessions.remove(player); }
    public ClientIntelligence.Session session(UUID player) {
        State state = sessions.get(player); return state == null ? null : state.session();
    }
    public boolean active() { return active; }
    public String loggingStatus() {
        IncidentLog current = log;
        return current == null ? "disabled" : "dropped=" + current.dropped() + ", errors=" + current.errors();
    }
    public RiskEngine.Assessment assessment(ClientIntelligence.Session session) { return risk.assess(session.snapshot(Instant.now()), Instant.now()); }
    private State state(GrimPlayer player) { return sessions.get(player.uuid); }
    private double health() { return System.nanoTime() - healthAt < 3_000_000_000L ? tps : -1; }

    @Override public void onPacketReceive(PacketReceiveEvent event) {
        if (!active || event.isCancelled()) return;
        GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
        // Includes upstream's Geyser/Floodgate exclusions; never create unbounded pre-login sessions.
        if (player == null || player.disableGrim || GrimAPI.INSTANCE.getPlayerDataManager().isExemptUser(event.getUser())) return;
        State state = state(player);
        if (state == null) return;
        try {
            if (event.getPacketType() == PacketType.Play.Client.PLUGIN_MESSAGE) {
                var packet = new WrapperPlayClientPluginMessage(event);
                payload(player, state.session(), packet.getChannelName(), packet.getData());
            } else if (event.getPacketType() == PacketType.Configuration.Client.PLUGIN_MESSAGE) {
                var packet = new WrapperConfigClientPluginMessage(event);
                payload(player, state.session(), packet.getChannelName(), packet.getData());
            } else if (diagnostics && WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
                if (state.packets().movement(System.nanoTime(), player.getTransactionPing(), health()))
                    record(player, "sustained-movement-burst", Category.UNKNOWN, .2, "DefenderPacketTiming",
                            "Three windows above 60 movement packets/s; batching/proxy/tick-rate changes possible");
            } else if (diagnostics && event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
                var packet = new WrapperPlayClientInteractEntity(event);
                if (packet.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK
                        && state.combat().attack(packet.getEntityId(), System.nanoTime(), player.getTransactionPing(), health()))
                    record(player, "repeated-multiple-targets", Category.UNKNOWN, .25, "DefenderCombatSequence",
                            "Repeated windows attacking at least eight distinct targets; review server mechanics and batching");
            }
        } catch (IllegalArgumentException ex) {
            record(player, "malformed-technical-payload", Category.UNKNOWN, .2, "DefenderPayload", "Payload rejected by bounded decoder");
        }
    }

    private void payload(GrimPlayer player, ClientIntelligence.Session session, String channel, byte[] data) {
        if (channel.equals("minecraft:brand") || channel.equals("MC|Brand")) {
            String brand = PayloadDecoder.brand(data);
            classify(player, session, Signature.Source.BRAND, brand);
        } else if (channel.equals("minecraft:register")) {
            for (String registered : PayloadDecoder.channels(data)) classify(player, session, Signature.Source.CHANNEL, registered);
        } else {
            classify(player, session, Signature.Source.CHANNEL, channel);
        }
    }

    private void classify(GrimPlayer player, ClientIntelligence.Session session, Signature.Source source, String value) {
        for (ClientIntelligence.Match match : intelligence.classify(source, value)) {
            if (!session.first(match.signal())) continue;
            session.label(match);
            record(player, match.signal(), match.category(), match.confidence(), "ClientIntelligence", match.label() + "; self-reported, spoofable");
        }
    }

    public void grimFlag(GrimPlayer player, Check check) {
        if (!active) return;
        // Do not copy arbitrary upstream verbose text: it may contain unrelated personal data.
        record(player, "grim-violation", Category.BEHAVIORAL_CHEAT, check.isExperimental() ? .35 : .7,
                "Grim:" + check.getCheckName(), "Upstream check flag; not independently confirmed; violations=" + check.getViolations());
    }

    private void record(GrimPlayer player, String signal, Category category, double confidence, String check, String detail) {
        State currentState = state(player);
        if (currentState == null) return;
        ClientIntelligence.Session session = currentState.session();
        Evidence e = new Evidence(signal, category, confidence, Instant.now(), check, detail,
                player.getClientVersion().getProtocolVersion(), player.getTransactionPing(), health(), session.brand() + "/" + session.loader());
        if (session.add(e)) {
            IncidentLog current = log;
            if (current != null) current.offer(session.id(), e);
        }
    }
}
