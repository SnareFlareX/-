package com.mcfriend.customizer.phrase;

import com.mcfriend.customizer.McFriendCustomizer;
import com.mcfriend.customizer.config.CustomizerConfig;
import com.mcfriend.customizer.util.NameFormatter;
import net.donila.friend.talk.Lines;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public final class PhraseEngine {
    private PhraseEngine() {
    }

    /**
     * Called by LinesMixin on every file read inside Lines.прочитать(String имя).
     * Applies:
     * 1. External override file config/mcfriend-customizer/lines/<name>.md (if present)
     * 2. In-game JSON phraseOverrides (REPLACE or APPEND)
     * 3. Friend name substitution + global word replacements on phrase lines (preserving headers & [tags])
     */
    public static List<String> processLoadedFile(String name, List<String> original) {
        if (name == null || name.isBlank()) {
            return original;
        }
        List<String> base = original;
        Path diskOverride = CustomizerConfig.getLinesDir().resolve(name + ".md");
        boolean hasDisk = Files.isRegularFile(diskOverride);
        if (hasDisk) {
            try {
                base = Files.readAllLines(diskOverride, StandardCharsets.UTF_8);
            } catch (IOException e) {
                McFriendCustomizer.LOGGER.warn("Не удалось прочитать пользовательский файл фраз {}: {}", diskOverride, e.getMessage());
            }
        }
        if (base == null) {
            return null;
        }

        CustomizerConfig cfg = CustomizerConfig.get();
        CustomizerConfig.PhraseOverride override = cfg.phraseOverrides != null ? cfg.phraseOverrides.get(name) : null;

        List<String> headers = new ArrayList<>();
        List<String> phraseBodies = new ArrayList<>();
        for (String rawLine : base) {
            String trimmed = rawLine.trim();
            Matcher m = PhraseCatalog.NUMBERED_LINE.matcher(trimmed);
            if (m.matches()) {
                phraseBodies.add(m.group(1).trim());
            } else {
                headers.add(rawLine);
            }
        }

        List<String> effectiveBodies;
        if (override != null && override.lines != null && !override.lines.isEmpty()) {
            if ("APPEND".equalsIgnoreCase(override.mode)) {
                effectiveBodies = new ArrayList<>(phraseBodies);
                for (String custom : override.lines) {
                    if (custom != null && !custom.isBlank()) {
                        effectiveBodies.add(stripLeadingNumber(custom.trim()));
                    }
                }
            } else {
                effectiveBodies = new ArrayList<>();
                for (String custom : override.lines) {
                    if (custom != null && !custom.isBlank()) {
                        effectiveBodies.add(stripLeadingNumber(custom.trim()));
                    }
                }
            }
        } else {
            effectiveBodies = phraseBodies;
        }

        List<String> out = new ArrayList<>(headers.size() + effectiveBodies.size() + 2);
        out.addAll(headers);
        int num = 1;
        boolean isUserCustom = hasDisk || (override != null && override.lines != null && !override.lines.isEmpty());
        for (String body : effectiveBodies) {
            if (body == null || body.isBlank()) continue;
            // Preserve unfinished original mcfriend TODO lines containing '(' only if from unmodified source
            String formatted = isUserCustom
                    ? NameFormatter.formatPhraseLineForLines(body)
                    : (body.contains("(") ? body : NameFormatter.formatPhraseLineForLines(body));
            out.add(num + ". " + formatted);
            num++;
        }
        return out;
    }

    public static String stripLeadingNumber(String line) {
        Matcher m = PhraseCatalog.NUMBERED_LINE.matcher(line);
        return m.matches() ? m.group(1).trim() : line;
    }

    public static void reloadLines() {
        try {
            Lines.load();
        } catch (Throwable t) {
            McFriendCustomizer.LOGGER.warn("Не удалось перезагрузить реплики Lines.load(): {}", t.toString());
        }
    }

    public static Path exportCategoryToDisk(String id) throws IOException {
        PhraseCatalog.Entry entry = PhraseCatalog.get(id);
        if (entry == null) {
            throw new IOException("Категория не найдена: " + id);
        }
        Path target = CustomizerConfig.getLinesDir().resolve(id + ".md");
        List<String> lines = new ArrayList<>();
        CustomizerConfig.PhraseOverride ov = CustomizerConfig.get().phraseOverrides.get(id);
        // Copy headers from original
        for (String raw : entry.rawLines()) {
            if (!PhraseCatalog.NUMBERED_LINE.matcher(raw.trim()).matches()) {
                lines.add(raw);
            }
        }
        if (ov != null && ov.lines != null && !ov.lines.isEmpty()) {
            List<String> merged = new ArrayList<>();
            if ("APPEND".equalsIgnoreCase(ov.mode)) {
                merged.addAll(entry.defaultPhrases());
            }
            merged.addAll(ov.lines);
            int idx = 1;
            for (String p : merged) {
                if (p != null && !p.isBlank()) {
                    lines.add(idx + ". " + stripLeadingNumber(p.trim()));
                    idx++;
                }
            }
        } else {
            int idx = 1;
            for (String p : entry.defaultPhrases()) {
                lines.add(idx + ". " + p);
                idx++;
            }
        }
        Files.write(target, lines, StandardCharsets.UTF_8);
        return target;
    }

    public static boolean hasDiskOverride(String id) {
        return id != null && Files.isRegularFile(CustomizerConfig.getLinesDir().resolve(id + ".md"));
    }

    public static List<String> getEffectiveEditablePhrases(String id) {
        CustomizerConfig.PhraseOverride ov = CustomizerConfig.get().phraseOverrides.get(id);
        if (ov != null && ov.lines != null && !ov.lines.isEmpty()) {
            return new ArrayList<>(ov.lines);
        }
        Path disk = CustomizerConfig.getLinesDir().resolve(id + ".md");
        if (Files.isRegularFile(disk)) {
            try {
                List<String> res = new ArrayList<>();
                for (String raw : Files.readAllLines(disk, StandardCharsets.UTF_8)) {
                    Matcher m = PhraseCatalog.NUMBERED_LINE.matcher(raw.trim());
                    if (m.matches()) {
                        res.add(m.group(1).trim());
                    }
                }
                if (!res.isEmpty()) return res;
            } catch (IOException ignored) {
            }
        }
        PhraseCatalog.Entry e = PhraseCatalog.get(id);
        return e == null ? new ArrayList<>() : new ArrayList<>(e.defaultPhrases());
    }
}
