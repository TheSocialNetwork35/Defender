// SPDX-License-Identifier: GPL-3.0-only
package ac.defender;

import ac.defender.companion.ChallengeRegistry;
import ac.defender.firewall.*;
import ac.defender.intelligence.*;
import ac.defender.logging.IncidentLog;
import ac.defender.probe.ProbeSession;
import ac.defender.risk.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class DefenderTest {
    final Instant now = Instant.parse("2026-09-25T12:00:00Z");
    Evidence e(Category category, String check, double confidence, int ping, double tps, Instant time) {
        return new Evidence("signal", category, confidence, time, check, "detail", 774, ping, tps, "unknown");
    }
    @ParameterizedTest @CsvSource({"vanilla,CLIENT", "fabric,MOD_LOADER", "lunarclient,CLIENT", "neoforge,MOD_LOADER", "quilt,MOD_LOADER"})
    void legitimateFingerprintsNeverRaiseRisk(String brand, Category category) {
        var engine = new ClientIntelligence(List.of(new Signature("test", Signature.Source.BRAND, brand, brand, category, .3)));
        assertEquals(1, engine.classify(Signature.Source.BRAND, brand).size());
        var risk = new RiskEngine().assess(Collections.nCopies(1000, e(category, "brand", .6, 30, 20, now)), now);
        assertEquals(RiskEngine.Level.LOW, risk.level()); assertEquals(0, risk.behavioralScore());
    }
    @Test void independentBehaviorRequiredAndDecays() {
        var engine = new RiskEngine();
        Evidence reach = e(Category.BEHAVIORAL_CHEAT, "reach", 1, 30, 20, now);
        assertEquals(RiskEngine.Level.MEDIUM, engine.assess(Collections.nCopies(1000, reach), now).level());
        var both = List.of(reach, e(Category.BEHAVIORAL_CHEAT, "timer", 1, 30, 20, now));
        assertEquals(RiskEngine.Level.HIGH, engine.assess(both, now).level());
        assertEquals(RiskEngine.Level.LOW, engine.assess(both, now.plusSeconds(301)).level());
    }
    @ParameterizedTest @CsvSource({"350,20", "30,12", "-1,20", "30,-1"})
    void lagAndUnknownHealthDoNotEscalate(int ping, double tps) {
        assertEquals(0, new RiskEngine().assess(List.of(e(Category.BEHAVIORAL_CHEAT, "reach", 1, ping, tps, now)), now).behavioralScore());
    }
    @Test void protectedAndSpoofedAreNotBehavior() {
        assertEquals(0, new RiskEngine().assess(List.of(e(Category.PROTECTED_OR_SPOOFED, "probe", 1, 30, 20, now)), now).behavioralScore());
        assertThrows(IllegalArgumentException.class, () -> e(Category.UNKNOWN, "bad", Double.NaN, 1, 20, now));
    }
    @Test void velocityNormalizationIsExplicitAndBrandMatchingIsExact() {
        var engine = new ClientIntelligence(List.of(new Signature("fabric", Signature.Source.BRAND, "fabric", "Fabric", Category.MOD_LOADER, .3)));
        assertEquals(1, engine.classify(Signature.Source.BRAND, "Fabric (Velocity)").size());
        assertTrue(engine.classify(Signature.Source.BRAND, "notfabric").isEmpty());
        assertTrue(engine.classify(Signature.Source.BRAND, "fabric-extra").isEmpty());
    }
    @ParameterizedTest @CsvSource({"47", "765", "774", "9999"})
    void protocolsAreEvidenceNotCompatibilityClaims(int protocol) {
        assertEquals(protocol, new Evidence("x",Category.UNKNOWN,0,now,"x","x",protocol,0,20,"unknown").protocolVersion());
    }
    @Test void strictBrandDecoding() {
        assertEquals("fabric", PayloadDecoder.brand(new byte[]{6,102,97,98,114,105,99}));
        assertThrows(IllegalArgumentException.class, () -> PayloadDecoder.brand(new byte[]{6,102}));
        assertThrows(IllegalArgumentException.class, () -> PayloadDecoder.brand(new byte[]{(byte)128,(byte)128,(byte)128}));
        assertThrows(IllegalArgumentException.class, () -> PayloadDecoder.brand(new byte[]{1,(byte)255}));
        assertThrows(IllegalArgumentException.class, () -> PayloadDecoder.brand(new byte[]{1,10}));
    }
    @Test void registrationBounds() {
        assertEquals(List.of("fabric:test", "a:b"), PayloadDecoder.channels("fabric:test\0a:b".getBytes(StandardCharsets.UTF_8)));
        assertThrows(IllegalArgumentException.class, () -> PayloadDecoder.channels(new byte[8193]));
        assertThrows(IllegalArgumentException.class, () -> PayloadDecoder.channels("bad channel".getBytes(StandardCharsets.UTF_8)));
        assertThrows(IllegalArgumentException.class, () -> PayloadDecoder.channels("a:b\0".repeat(129).getBytes(StandardCharsets.UTF_8)));
    }
    @Test void payloadFuzzNeverLeaksUnboundedOrUnexpectedExceptions() {
        Random random = new Random(42);
        for (int i=0;i<5000;i++) {
            byte[] bytes = new byte[random.nextInt(1024)]; random.nextBytes(bytes);
            try { assertTrue(PayloadDecoder.brand(bytes).length() <= 128); } catch (IllegalArgumentException expected) { }
            try { assertTrue(PayloadDecoder.channels(bytes).size() <= 128); } catch (IllegalArgumentException expected) { }
        }
    }
    @Test void doubleCheckUsesFreshChallengeAndRejectsReplay() {
        var probe = new ProbeSession(now, Duration.ofSeconds(10)); UUID first = probe.challenge();
        assertEquals(ProbeSession.State.PENDING, probe.answer(UUID.randomUUID(),"meteor",true,now));
        assertEquals(ProbeSession.State.DOUBLE_CHECK, probe.answer(first,"meteor",true,now));
        assertNotEquals(first, probe.challenge());
        assertEquals(ProbeSession.State.DOUBLE_CHECK, probe.answer(first,"meteor",true,now));
        assertEquals(ProbeSession.State.SIGNAL, probe.answer(probe.challenge(),"meteor",true,now));
    }
    @Test void timeoutAndProtectionAreNotDetection() {
        var probe = new ProbeSession(now, Duration.ofSeconds(10));
        assertEquals(ProbeSession.State.TIMEOUT, probe.answer(probe.challenge(),"meteor",true,now.plusSeconds(10)));
        var protectedProbe = new ProbeSession(now, Duration.ofSeconds(10));
        assertEquals(ProbeSession.State.PROTECTED_OR_SPOOFED, protectedProbe.answer(protectedProbe.challenge(),"meteor",false,now));
        var blocked = new ProbeSession(now, Duration.ofSeconds(10)); blocked.block();
        assertEquals(ProbeSession.State.BLOCKED, blocked.state(now));
    }
    @Test void mismatchedProbeCannotConfirm() {
        var probe = new ProbeSession(now, Duration.ofSeconds(10));
        probe.answer(probe.challenge(),"meteor",true,now);
        assertEquals(ProbeSession.State.PROTECTED_OR_SPOOFED,probe.answer(probe.challenge(),"wurst",true,now));
    }
    @Test void sessionBoundedAndRateLimitedAndExpires() {
        var session = new ClientIntelligence.Session();
        Evidence initial = e(Category.UNKNOWN,"x",.1,30,20,now);
        assertTrue(session.add(initial)); assertFalse(session.add(initial));
        for (int i=0;i<500;i++) session.add(e(Category.UNKNOWN,"x"+i,.1,30,20,now));
        assertEquals(128, session.snapshot(now).size());
        assertTrue(session.snapshot(now.plusSeconds(1801)).isEmpty());
        for (int i=0;i<256;i++) assertTrue(session.first("key"+i));
        assertFalse(session.first("overflow"));
    }
    @Test void badYamlCannotInjectTagsOrBehaviorOrDuplicates() {
        for (String yaml : List.of("!!java.lang.ProcessBuilder {}", "schema: 1\nschema: 1\nsignatures: []", "schema: 2\nsignatures: []",
                "schema: 1\nsignatures:\n- {id: x, source: BRAND, value: x, label: x, category: BEHAVIORAL_CHEAT, confidence: 0.5}"))
            assertThrows(RuntimeException.class, () -> Signatures.load(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8))));
    }
    @Test void atomicSignatureReplacement() {
        var engine = new ClientIntelligence(List.of());
        var signatures = Signatures.load(new ByteArrayInputStream("schema: 1\nsignatures:\n- {id: x, source: BRAND, value: x, label: x, category: CLIENT, confidence: 0.3}".getBytes(StandardCharsets.UTF_8)));
        engine.replace(signatures); assertEquals(1,engine.classify(Signature.Source.BRAND,"x").size());
    }
    @Test void normalAndBatchedMovementRemainQuiet() {
        PacketWindow regular = new PacketWindow();
        for(int i=0;i<1000;i++) assertFalse(regular.movement(i*50_000_000L,30,20));
        PacketWindow delayed = new PacketWindow();
        for(int i=0;i<1000;i++) assertFalse(delayed.movement(i*5_000_000L,400,20));
        PacketWindow oneBurst = new PacketWindow();
        for(int i=0;i<300;i++) assertFalse(oneBurst.movement(i*10_000_000L,30,20));
        assertFalse(oneBurst.movement(6_000_000_000L,30,20));
    }
    @Test void sustainedMovementBurstIsDiagnostic() {
        PacketWindow window = new PacketWindow(); boolean found=false;
        for(int i=0;i<2000;i++) found |= window.movement(i*10_000_000L,30,20);
        assertTrue(found);
    }
    @Test void combatRequiresSustainedDistinctTargetsAndHealthyConnection() {
        CombatWindow regular = new CombatWindow();
        for(int i=0;i<100;i++) assertFalse(regular.attack(1,i*100_000_000L,30,20));
        CombatWindow lagged = new CombatWindow();
        for(int i=0;i<100;i++) assertFalse(lagged.attack(i,i*100_000_000L,400,20));
        CombatWindow burst = new CombatWindow(); boolean found=false;
        for(int i=0;i<50;i++) found |= burst.attack(i,i*100_000_000L,30,20);
        assertTrue(found);
    }
    @Test void nonceIsSessionBoundExpiringAndSingleUse() {
        var registry = new ChallengeRegistry(); UUID a=UUID.randomUUID(), b=UUID.randomUUID();
        byte[] nonce=registry.issue(a,now);
        assertFalse(registry.consume(b,nonce,now)); assertTrue(registry.consume(a,nonce,now)); assertFalse(registry.consume(a,nonce,now));
        nonce=registry.issue(a,now); assertFalse(registry.consume(a,nonce,now.plusSeconds(15)));
        nonce=registry.issue(a,now); registry.disconnect(a); assertFalse(registry.consume(a,nonce,now));
    }
    @Test void loggingEscapesAndRetainsOnlySevenDays(@TempDir Path dir) throws Exception {
        Files.writeString(dir.resolve("2000-01-01.jsonl"),"old");
        Evidence e = new Evidence("quote\"slash\\",Category.UNKNOWN,.1,now,"x","hello\nworld",774,40,20,"vanilla");
        try (var log = new IncidentLog(dir)) { log.offer(UUID.randomUUID(),e); }
        assertFalse(Files.exists(dir.resolve("2000-01-01.jsonl")));
        String json=Files.readString(dir.resolve(LocalDate.now(ZoneOffset.UTC)+".jsonl"));
        assertEquals(1,json.lines().count()); assertTrue(json.contains("quote\\\"slash\\\\"));
        assertFalse(json.contains("hello\nworld")); assertFalse(json.contains("ipAddress"));
    }
}
