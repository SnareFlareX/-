package com.mcfriend.customizer.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mcfriend.customizer.McFriendCustomizer;
import com.mcfriend.customizer.util.RussianDecliner;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public final class CustomizerConfig {
    public static final String DEFAULT_NAME = "Боря";
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private static final AtomicLong VERSION = new AtomicLong(1L);
    private static volatile CustomizerConfig instance = new CustomizerConfig();

    public String name = DEFAULT_NAME;
    public boolean autoDecline = true;
    public String genitive = "";
    public String dative = "";
    public String accusative = "";
    public String instrumental = "";
    public String prepositional = "";
    public boolean showNametag = false;

    public String skinFile = "";
    public String skinModel = "slim";
    public boolean showCatEyes = true;

    public Map<String, PhraseOverride> phraseOverrides = new LinkedHashMap<>();
    public Map<String, String> globalReplacements = new LinkedHashMap<>();

    public static final class PhraseOverride {
        public String mode = "REPLACE";
        public List<String> lines = new ArrayList<>();

        public PhraseOverride() {
        }

        public PhraseOverride(String mode, List<String> lines) {
            this.mode = "APPEND".equalsIgnoreCase(mode) ? "APPEND" : "REPLACE";
            this.lines = lines == null ? new ArrayList<>() : new ArrayList<>(lines);
        }

        public PhraseOverride copy() {
            return new PhraseOverride(this.mode, this.lines);
        }
    }

    public static CustomizerConfig get() {
        return instance;
    }

    public static long getVersion() {
        return VERSION.get();
    }

    public static void bumpVersion() {
        VERSION.incrementAndGet();
    }

    public static Path getConfigDir() {
        Path dir = FabricLoader.getInstance().getConfigDir().resolve("mcfriend-customizer");
        ensureDir(dir);
        return dir;
    }

    public static Path getSkinsDir() {
        Path dir = getConfigDir().resolve("skins");
        ensureDir(dir);
        return dir;
    }

    public static Path getLinesDir() {
        Path dir = getConfigDir().resolve("lines");
        ensureDir(dir);
        return dir;
    }

    public static Path getProfilesDir() {
        Path dir = getConfigDir().resolve("profiles");
        ensureDir(dir);
        return dir;
    }

    public static Path getConfigFile() {
        return getConfigDir().resolve("config.json");
    }

    private static void ensureDir(Path dir) {
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            McFriendCustomizer.LOGGER.warn("Не удалось создать папку {}: {}", dir, e.getMessage());
        }
    }

    public static synchronized void load() {
        getSkinsDir();
        getLinesDir();
        getProfilesDir();
        Path file = getConfigFile();
        if (!Files.exists(file)) {
            instance = new CustomizerConfig();
            save();
            return;
        }
        try {
            String json = Files.readString(file, StandardCharsets.UTF_8);
            CustomizerConfig loaded = fromJson(json);
            instance = loaded;
            bumpVersion();
        } catch (Exception e) {
            McFriendCustomizer.LOGGER.warn("Ошибка чтения {}: {} — используются настройки по умолчанию", file, e.getMessage());
            instance = new CustomizerConfig();
            bumpVersion();
        }
    }

    public static synchronized void save() {
        Path file = getConfigFile();
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, toJson(instance), StandardCharsets.UTF_8);
        } catch (IOException e) {
            McFriendCustomizer.LOGGER.error("Не удалось сохранить настройки в {}", file, e);
        }
    }

    public static synchronized void set(CustomizerConfig updated, boolean saveToDisk) {
        CustomizerConfig sanitized = updated == null ? new CustomizerConfig() : updated.copy();
        sanitized.sanitize();
        instance = sanitized;
        bumpVersion();
        if (saveToDisk) {
            save();
        }
    }

    public static CustomizerConfig fromJson(String json) {
        if (json == null || json.isBlank()) {
            return new CustomizerConfig();
        }
        CustomizerConfig parsed = GSON.fromJson(json, CustomizerConfig.class);
        if (parsed == null) {
            parsed = new CustomizerConfig();
        }
        parsed.sanitize();
        return parsed;
    }

    public static String toJson(CustomizerConfig cfg) {
        CustomizerConfig target = cfg == null ? new CustomizerConfig() : cfg;
        target.sanitize();
        return GSON.toJson(target);
    }

    public CustomizerConfig copy() {
        CustomizerConfig c = new CustomizerConfig();
        c.name = this.name;
        c.autoDecline = this.autoDecline;
        c.genitive = this.genitive;
        c.dative = this.dative;
        c.accusative = this.accusative;
        c.instrumental = this.instrumental;
        c.prepositional = this.prepositional;
        c.showNametag = this.showNametag;
        c.skinFile = this.skinFile;
        c.skinModel = this.skinModel;
        c.showCatEyes = this.showCatEyes;
        c.phraseOverrides = new LinkedHashMap<>();
        if (this.phraseOverrides != null) {
            for (Map.Entry<String, PhraseOverride> e : this.phraseOverrides.entrySet()) {
                if (e.getKey() != null && e.getValue() != null) {
                    c.phraseOverrides.put(e.getKey(), e.getValue().copy());
                }
            }
        }
        c.globalReplacements = new LinkedHashMap<>();
        if (this.globalReplacements != null) {
            c.globalReplacements.putAll(this.globalReplacements);
        }
        c.sanitize();
        return c;
    }

    public void sanitize() {
        if (this.name == null || this.name.isBlank()) {
            this.name = DEFAULT_NAME;
        } else {
            this.name = this.name.trim();
            if (this.name.length() > 32) {
                this.name = this.name.substring(0, 32);
            }
        }
        if (this.genitive == null) this.genitive = "";
        if (this.dative == null) this.dative = "";
        if (this.accusative == null) this.accusative = "";
        if (this.instrumental == null) this.instrumental = "";
        if (this.prepositional == null) this.prepositional = "";
        if (this.skinFile == null) {
            this.skinFile = "";
        } else {
            this.skinFile = this.skinFile.trim().replace('\\', '/');
            int slash = this.skinFile.lastIndexOf('/');
            if (slash >= 0) this.skinFile = this.skinFile.substring(slash + 1);
            if (".".equals(this.skinFile) || "..".equals(this.skinFile)) this.skinFile = "";
        }
        if (!"classic".equalsIgnoreCase(this.skinModel)) {
            this.skinModel = "slim";
        } else {
            this.skinModel = "classic";
        }
        if (this.phraseOverrides == null) {
            this.phraseOverrides = new LinkedHashMap<>();
        }
        if (this.globalReplacements == null) {
            this.globalReplacements = new LinkedHashMap<>();
        }
    }

    public boolean isDefaultName() {
        return DEFAULT_NAME.equalsIgnoreCase(getEffectiveName());
    }

    public String getEffectiveName() {
        return (this.name == null || this.name.isBlank()) ? DEFAULT_NAME : this.name.trim();
    }

    public RussianDecliner.Declensions resolveDeclensions() {
        String nom = getEffectiveName();
        RussianDecliner.Declensions auto = RussianDecliner.decline(nom);
        if (this.autoDecline) {
            return auto;
        }
        return new RussianDecliner.Declensions(
                nom,
                notBlankOr(this.genitive, auto.genitive()),
                notBlankOr(this.dative, auto.dative()),
                notBlankOr(this.accusative, auto.accusative()),
                notBlankOr(this.instrumental, auto.instrumental()),
                notBlankOr(this.prepositional, auto.prepositional())
        );
    }

    private static String notBlankOr(String candidate, String fallback) {
        return (candidate == null || candidate.isBlank()) ? fallback : candidate.trim();
    }

    public static String sanitizeFileName(String raw) {
        if (raw == null) return "";
        String cleaned = raw.trim()
                .replaceAll("[\\\\/:*?\"<>|]+", "_")
                .replaceAll("\\.{2,}", "_")
                .replaceAll("\\s*_\\s*", "_")
                .replaceAll("^[_\\s.]+|[_\\s.]+$", "");
        if (cleaned.length() > 48) {
            cleaned = cleaned.substring(0, 48).replaceAll("[_\\s.]+$", "");
        }
        return cleaned;
    }

    public static List<String> listProfiles() {
        List<String> result = new ArrayList<>();
        try (Stream<Path> s = Files.list(getProfilesDir())) {
            s.filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString())
                    .filter(n -> n.toLowerCase().endsWith(".json"))
                    .map(n -> n.substring(0, n.length() - 5))
                    .sorted(Comparator.naturalOrder())
                    .forEach(result::add);
        } catch (IOException ignored) {
        }
        return result;
    }

    public static boolean saveProfile(String profileName, CustomizerConfig cfg) {
        String safe = sanitizeFileName(profileName);
        if (safe.isEmpty()) return false;
        Path target = getProfilesDir().resolve(safe + ".json");
        try {
            Files.writeString(target, toJson(cfg), StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            McFriendCustomizer.LOGGER.error("Не удалось сохранить профиль {}", safe, e);
            return false;
        }
    }

    public static CustomizerConfig loadProfile(String profileName) {
        String safe = sanitizeFileName(profileName);
        if (safe.isEmpty()) return null;
        Path target = getProfilesDir().resolve(safe + ".json");
        if (!Files.exists(target)) return null;
        try {
            return fromJson(Files.readString(target, StandardCharsets.UTF_8));
        } catch (Exception e) {
            McFriendCustomizer.LOGGER.error("Не удалось загрузить профиль {}", safe, e);
            return null;
        }
    }

    public static boolean deleteProfile(String profileName) {
        String safe = sanitizeFileName(profileName);
        if (safe.isEmpty()) return false;
        Path target = getProfilesDir().resolve(safe + ".json");
        try {
            return Files.deleteIfExists(target);
        } catch (IOException e) {
            return false;
        }
    }
}
