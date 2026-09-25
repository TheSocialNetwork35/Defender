// SPDX-License-Identifier: GPL-3.0-only
package ac.defender.logging;
import ac.defender.risk.Evidence;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
/** Bounded async JSONL log. Random session IDs, no player names, UUIDs, IPs or raw payloads. */
public final class IncidentLog implements AutoCloseable {
    private final ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(2048);
    private final AtomicLong dropped = new AtomicLong();
    private final AtomicLong errors = new AtomicLong();
    private final Path directory;
    private final Thread worker;
    private volatile boolean running = true;
    public IncidentLog(Path directory) throws IOException {
        this.directory = directory;
        Files.createDirectories(directory);
        worker = new Thread(this::write, "defender-incidents"); worker.setDaemon(true); worker.start();
    }
    public void offer(UUID session, Evidence e) {
        if (!running || !queue.offer(json(session, e))) dropped.incrementAndGet();
    }
    public long dropped() { return dropped.get(); }
    public long errors() { return errors.get(); }
    public static String json(UUID session, Evidence e) {
        return "{\"session\":" + quote(session.toString()) + ",\"signal\":" + quote(e.signal())
                + ",\"category\":" + quote(e.category().name()) + ",\"confidence\":" + e.confidence()
                + ",\"timestamp\":" + quote(e.timestamp().toString()) + ",\"check\":" + quote(e.check())
                + ",\"evidence\":" + quote(e.evidence()) + ",\"protocolVersion\":" + e.protocolVersion()
                + ",\"ping\":" + e.ping() + ",\"tps\":" + e.tps()
                + ",\"clientInformation\":" + quote(e.clientInformation()) + "}";
    }
    private static String quote(String s) {
        StringBuilder result = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            if (c == '"' || c == '\\') result.append('\\');
            if (c < 32 || Character.isSurrogate(c)) result.append(String.format("\\u%04x", (int)c));
            else result.append(c);
        }
        return result.append('"').toString();
    }
    private void write() {
        LocalDate lastDay = null;
        while (running || !queue.isEmpty()) {
            try {
                String entry = queue.poll(200, TimeUnit.MILLISECONDS);
                if (entry == null) continue;
                LocalDate day = LocalDate.now(ZoneOffset.UTC);
                if (!day.equals(lastDay)) { prune(day); lastDay = day; }
                Path file = directory.resolve(day + ".jsonl");
                // 16 MiB/day hard cap. Loss is visible to admins, never silently unbounded.
                if (Files.exists(file) && Files.size(file) >= 16 * 1024 * 1024) { dropped.incrementAndGet(); continue; }
                Files.writeString(file, entry + "\n", StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException ex) { errors.incrementAndGet(); }
            catch (InterruptedException ex) { if (running) Thread.currentThread().interrupt(); }
        }
    }
    private void prune(LocalDate now) throws IOException {
        try (var files = Files.list(directory)) {
            for (Path p : files.toList()) {
                String name = p.getFileName().toString();
                if (name.matches("\\d{4}-\\d{2}-\\d{2}\\.jsonl")
                        && LocalDate.parse(name.substring(0, 10)).isBefore(now.minusDays(6))) Files.deleteIfExists(p);
            }
        }
    }
    @Override public void close() {
        running = false;
        try { worker.join(3000); } catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
        if (worker.isAlive()) { dropped.addAndGet(queue.size()); queue.clear(); }
    }
}
