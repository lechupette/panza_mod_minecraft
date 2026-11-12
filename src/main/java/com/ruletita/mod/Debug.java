package com.ruletita.mod;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Interruptores de diagnóstico de rendimiento. Se leen del archivo config/ruletita_debug.txt
 * cada pocos segundos, sin reiniciar el juego. Por defecto todo está activado (1).
 */
public final class Debug {
    public static volatile boolean hud = true;
    public static volatile boolean belly = true;
    public static volatile boolean clientTick = true;
    public static volatile boolean serverTick = true;

    private static long lastModified = -1L;

    private Debug() { }

    static void reload(File gameDir) {
        try {
            File file = new File(new File(gameDir, "config"), "ruletita_debug.txt");
            if (!file.isFile()) {
                file.getParentFile().mkdirs();
                Files.writeString(file.toPath(),
                        "# 1 = activado, 0 = desactivado. Se aplica solo, en unos segundos.\n"
                                + "hud=1\nbelly=1\nclientTick=1\nserverTick=1\n", StandardCharsets.UTF_8);
            }
            long modified = file.lastModified();
            if (modified == lastModified) return;
            lastModified = modified;
            hud = belly = clientTick = serverTick = true;
            for (String raw : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
                String line = raw.trim();
                if (line.isEmpty() || line.startsWith("#") || !line.contains("=")) continue;
                String[] kv = line.split("=", 2);
                boolean on = !(kv[1].trim().equals("0") || kv[1].trim().equalsIgnoreCase("false"));
                switch (kv[0].trim()) {
                    case "hud" -> hud = on;
                    case "belly" -> belly = on;
                    case "clientTick" -> clientTick = on;
                    case "serverTick" -> serverTick = on;
                    default -> { }
                }
            }
        } catch (Exception ignored) {
            // Diagnóstico opcional: cualquier fallo deja todo activado.
        }
    }
}
