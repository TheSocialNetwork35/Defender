// SPDX-License-Identifier: GPL-3.0-only
package ac.defender.platform;

import ac.defender.intelligence.ClientIntelligence;
import ac.defender.risk.Category;
import ac.defender.risk.Evidence;
import ac.grim.grimac.command.BuildableCommand;
import ac.grim.grimac.platform.api.command.PlayerSelector;
import ac.grim.grimac.platform.api.manager.cloud.CloudPlatformCommandArguments;
import ac.grim.grimac.platform.api.sender.Sender;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.CommandManager;

import java.time.Instant;
import java.util.List;

/** Permission-gated session diagnostics. Text never interpreted as MiniMessage. */
public final class DefenderCommand implements BuildableCommand {
    @Override public void register(CommandManager<Sender> manager, CloudPlatformCommandArguments arguments) {
        for (String action : List.of("inspect", "client", "evidence", "history", "checkclient")) {
            manager.command(manager.commandBuilder("defender").literal(action).permission("defender.admin")
                    .required("target", arguments.singlePlayerSelectorParser()).handler(context -> {
                        PlayerSelector selector = context.get("target");
                        var player = selector.getSinglePlayer().getPlatformPlayer();
                        if (player == null || player.isExternalPlayer()) { send(context.sender(), "Player is not on this server."); return; }
                        DefenderRuntime runtime = DefenderRuntime.INSTANCE;
                        if (!runtime.active()) { send(context.sender(), "Defender unavailable: check startup log."); return; }
                        ClientIntelligence.Session session = runtime.session(player.getUniqueId());
                        if (action.equals("checkclient")) {
                            send(context.sender(), "UNSUPPORTED: active translation/keybind transport has not been validated for 26.3. Passive signals only; no packets sent.");
                            return;
                        }
                        if (session == null) { send(context.sender(), "No session evidence (offline, exempt, Bedrock, or no observed packets). Unknown does not mean clean."); return; }
                        List<Evidence> evidence = session.snapshot(Instant.now());
                        var assessment = runtime.assessment(session);
                        if (action.equals("inspect") || action.equals("client")) {
                            send(context.sender(), "Client guess: " + session.brand() + " | Loader: " + session.loader() + " (spoofable)");
                            send(context.sender(), "Protocol: " + (evidence.isEmpty() ? "unknown" : evidence.get(evidence.size()-1).protocolVersion())
                                    + " | Identification confidence: " + Math.round(assessment.identificationConfidence()*100) + "% (heuristic)");
                            send(context.sender(), "Risk: " + assessment.level() + " | Recent Grim observations: "
                                    + evidence.stream().filter(e -> e.category() == Category.BEHAVIORAL_CHEAT).count()
                                    + " | Client signals: " + evidence.stream().filter(e -> e.check().equals("ClientIntelligence")).count()
                                    + " | Packet anomalies: " + evidence.stream().filter(e -> e.check().startsWith("Defender")).count());
                            send(context.sender(), "Observation only | Incident logging: " + runtime.loggingStatus());
                        } else {
                            send(context.sender(), "Current session history only: last 10 of " + evidence.size() + " observations (30-minute retention). Session " + session.id());
                            evidence.stream().skip(Math.max(0, evidence.size()-10)).forEach(e -> send(context.sender(),
                                    e.timestamp() + " " + e.category() + " " + e.check() + " " + e.signal() + " confidence=" + e.confidence()
                                            + " protocol=" + e.protocolVersion() + " ping=" + e.ping() + " TPS=" + e.tps() + " " + e.evidence()));
                        }
                    }));
        }
        manager.command(manager.commandBuilder("defender").literal("reload").permission("defender.admin").handler(context -> {
            try {
                if (!DefenderRuntime.INSTANCE.active()) { send(context.sender(), "Defender unavailable; restart after fixing startup configuration."); return; }
                DefenderRuntime.INSTANCE.reload();
                send(context.sender(), "Defender configuration and signatures reloaded. Grim configuration uses /grim reload.");
            } catch (Exception ex) { send(context.sender(), "Reload rejected; previous configuration retained. Check YAML schema and file access."); }
        }));
    }
    private static void send(Sender sender, String message) { sender.sendMessage(Component.text("[Defender] " + message)); }
}
