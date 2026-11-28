package com.mcfriend.customizer.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mcfriend.customizer.McFriendCustomizer;
import com.mcfriend.customizer.config.CustomizerConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_1060;
import net.minecraft.class_2960;
import net.minecraft.class_310;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
public final class ClientSkinManager {
    public static final class_2960 DEFAULT_BORYA_SKIN = class_2960.method_60655("mcfriend", "textures/entity/borya2.png");

    private static final AtomicInteger TEX_SEQ = new AtomicInteger(1);
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private static volatile class_2960 activeTextureId = null;
    private static volatile int[] activeArgb64 = null;
    private static volatile String loadedSkinFile = "";
    private static volatile long lastCheckedVersion = -1L;
    private static volatile long loadedFileMtime = -1L;

    public record DownloadResult(boolean ok, String fileName, String detectedModel, String message) {}

    private ClientSkinManager() {
    }

    public static boolean shouldRenderCatEyes() {
        return CustomizerConfig.get().showCatEyes;
    }

    public static boolean isClassicArms() {
        return "classic".equalsIgnoreCase(CustomizerConfig.get().skinModel);
    }

    public static class_2960 getActiveCustomTexture() {
        return activeTextureId;
    }

    public static class_2960 getPreviewTexture() {
        class_2960 custom = activeTextureId;
        return custom != null ? custom : DEFAULT_BORYA_SKIN;
    }

    public static int[] getActiveArgb64() {
        return activeArgb64;
    }

    public static void tickCheckReload() {
        long ver = CustomizerConfig.getVersion();
        String desired = CustomizerConfig.get().skinFile;
        if (desired == null) desired = "";

        boolean needReload = (ver != lastCheckedVersion) || !desired.equals(loadedSkinFile);
        if (!needReload && !desired.isEmpty()) {
            Path p = CustomizerConfig.getSkinsDir().resolve(desired);
            try {
                long mtime = Files.isRegularFile(p) ? Files.getLastModifiedTime(p).toMillis() : -1L;
                if (mtime != loadedFileMtime) {
                    needReload = true;
                }
            } catch (IOException ignored) {
            }
        }
        if (needReload) {
            lastCheckedVersion = ver;
            reloadNow();
        }
    }

    public static void forceReloadOnRenderThread() {
        class_310 client = class_310.method_1551();
        if (client != null) {
            client.execute(() -> {
                lastCheckedVersion = CustomizerConfig.getVersion();
                reloadNow();
            });
        }
    }

    public static void reloadNow() {
        class_310 client = class_310.method_1551();
        if (client == null) return;
        class_1060 tm = client.method_1531();
        if (tm == null) return;

        String desired = CustomizerConfig.get().skinFile;
        if (desired == null || desired.isBlank()) {
            clearCustomTexture(tm);
            loadedSkinFile = "";
            loadedFileMtime = -1L;
            return;
        }

        Path path = CustomizerConfig.getSkinsDir().resolve(desired);
        if (!Files.isRegularFile(path)) {
            McFriendCustomizer.LOGGER.warn("Файл скина не найден: {}", path);
            clearCustomTexture(tm);
            loadedSkinFile = desired;
            loadedFileMtime = -1L;
            return;
        }

        try {
            byte[] bytes = Files.readAllBytes(path);
            long mtime = Files.getLastModifiedTime(path).toMillis();
            class_1011 raw = class_1011.method_49277(bytes);
            class_1011 processed = normalizeSkinImage(raw);
            if (processed != raw) {
                raw.close();
            }

            int[] argb64 = extractArgb64(processed);
            class_1043 dynTex = new class_1043(processed);
            class_2960 newId = McFriendCustomizer.id("dynamic/friend_skin_" + TEX_SEQ.getAndIncrement());

            class_2960 oldId = activeTextureId;
            tm.method_4616(newId, dynTex);
            activeTextureId = newId;
            activeArgb64 = argb64;
            loadedSkinFile = desired;
            loadedFileMtime = mtime;

            if (oldId != null) {
                try {
                    tm.method_4615(oldId);
                } catch (Exception ignored) {
                }
            }
            McFriendCustomizer.LOGGER.info("Загружен скин друга: {} ({}x{})", desired, processed.method_4307(), processed.method_4323());
        } catch (Exception e) {
            McFriendCustomizer.LOGGER.error("Ошибка загрузки скина {}", path, e);
            clearCustomTexture(tm);
            loadedSkinFile = desired;
            loadedFileMtime = -1L;
        }
    }

    private static void clearCustomTexture(class_1060 tm) {
        class_2960 oldId = activeTextureId;
        activeTextureId = null;
        activeArgb64 = null;
        if (oldId != null && tm != null) {
            try {
                tm.method_4615(oldId);
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Converts legacy 64x32 Minecraft skins to modern 64x64 format so limbs render cleanly.
     */
    private static class_1011 normalizeSkinImage(class_1011 src) {
        int w = src.method_4307();
        int h = src.method_4323();
        if (w == 64 && h == 32) {
            class_1011 dst = new class_1011(64, 64, true);
            // Copy top 64x32 verbatim
            for (int y = 0; y < 32; y++) {
                for (int x = 0; x < 64; x++) {
                    dst.method_4305(x, y, src.method_4315(x, y));
                }
            }
            // Mirror right leg (0,16,16x16) to left leg (16,48,16x16) & right arm (40,16,16x16) to left arm (32,48,16x16)
            copyRectFlipX(dst, 4, 16, 16, 32, 4, 4);
            copyRectFlipX(dst, 8, 16, 16, 32, 4, 4);
            copyRectFlipX(dst, 0, 20, 24, 32, 4, 12);
            copyRectFlipX(dst, 4, 20, 16, 32, 4, 12);
            copyRectFlipX(dst, 8, 20, 8, 32, 4, 12);
            copyRectFlipX(dst, 12, 20, 16, 32, 4, 12);

            copyRectFlipX(dst, 44, 16, -8, 32, 4, 4);
            copyRectFlipX(dst, 48, 16, -8, 32, 4, 4);
            copyRectFlipX(dst, 40, 20, 0, 32, 4, 12);
            copyRectFlipX(dst, 44, 20, -8, 32, 4, 12);
            copyRectFlipX(dst, 48, 20, -16, 32, 4, 12);
            copyRectFlipX(dst, 52, 20, -8, 32, 4, 12);
            // Force opaque alpha on base skin regions (head/body/limbs)
            forceOpaque(dst, 0, 0, 32, 16);
            forceOpaque(dst, 0, 16, 64, 32);
            forceOpaque(dst, 16, 48, 48, 64);
            return dst;
        }
        return src;
    }

    private static void copyRectFlipX(class_1011 img, int x, int y, int dx, int dy, int w, int h) {
        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                int c = img.method_4315(x + i, y + j);
                img.method_4305(x + dx + (w - 1 - i), y + dy + j, c);
            }
        }
    }

    private static void forceOpaque(class_1011 img, int x0, int y0, int x1, int y1) {
        for (int y = y0; y < y1; y++) {
            for (int x = x0; x < x1; x++) {
                int c = img.method_4315(x, y);
                img.method_4305(x, y, c | 0xFF000000);
            }
        }
    }

    private static int[] extractArgb64(class_1011 img) {
        int w = img.method_4307();
        int h = img.method_4323();
        int[] out = new int[64 * 64];
        for (int y = 0; y < 64; y++) {
            int sy = Math.min(h - 1, (y * h) / 64);
            for (int x = 0; x < 64; x++) {
                int sx = Math.min(w - 1, (x * w) / 64);
                int abgr = img.method_4315(sx, sy);
                // Convert NativeImage ABGR to ARGB used by SkillArt/BoryaFace
                out[y * 64 + x] = (abgr & 0xFF00FF00) | ((abgr & 0xFF) << 16) | ((abgr >>> 16) & 0xFF);
            }
        }
        return out;
    }

    public static List<String> listLocalSkins() {
        List<String> files = new ArrayList<>();
        try (Stream<Path> s = Files.list(CustomizerConfig.getSkinsDir())) {
            s.filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString())
                    .filter(n -> n.toLowerCase(Locale.ROOT).endsWith(".png"))
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .forEach(files::add);
        } catch (IOException ignored) {
        }
        return files;
    }

    public static void downloadSkinAsync(String input, Consumer<DownloadResult> onCompleteOnClient) {
        String trimmed = input == null ? "" : input.trim();
        if (trimmed.isEmpty()) {
            deliver(onCompleteOnClient, new DownloadResult(false, "", null, "Введите ник игрока или ссылку на PNG!"));
            return;
        }
        CompletableFuture.supplyAsync(() -> {
            try {
                if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                    return downloadDirectUrl(trimmed);
                }
                return downloadMojangSkin(trimmed);
            } catch (Exception e) {
                return new DownloadResult(false, "", null, "Ошибка загрузки: " + e.getMessage());
            }
        }).thenAccept(res -> deliver(onCompleteOnClient, res));
    }

    private static void deliver(Consumer<DownloadResult> cb, DownloadResult res) {
        if (cb == null) return;
        class_310 client = class_310.method_1551();
        if (client != null) {
            client.execute(() -> cb.accept(res));
        } else {
            cb.accept(res);
        }
    }

    private static DownloadResult downloadMojangSkin(String username) throws Exception {
        String cleanUser = username.replaceAll("[^A-Za-z0-9_]", "");
        if (cleanUser.isEmpty() || cleanUser.length() > 16) {
            return new DownloadResult(false, "", null, "Некорректный ник Minecraft: " + username);
        }
        String enc = URLEncoder.encode(cleanUser, StandardCharsets.UTF_8);
        HttpRequest req1 = HttpRequest.newBuilder(URI.create("https://api.mojang.com/users/profiles/minecraft/" + enc))
                .timeout(Duration.ofSeconds(10))
                .header("User-Agent", "MCFriendCustomizer/1.0")
                .GET().build();
        HttpResponse<String> resp1 = HTTP.send(req1, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (resp1.statusCode() == 204 || resp1.statusCode() == 404) {
            return new DownloadResult(false, "", null, "Игрок «" + cleanUser + "» не найден в Mojang.");
        }
        if (resp1.statusCode() != 200) {
            return new DownloadResult(false, "", null, "Mojang API вернул код " + resp1.statusCode());
        }

        JsonObject profile1 = JsonParser.parseString(resp1.body()).getAsJsonObject();
        String uuid = profile1.get("id").getAsString();
        String canonicalName = profile1.has("name") ? profile1.get("name").getAsString() : cleanUser;

        HttpRequest req2 = HttpRequest.newBuilder(URI.create("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid))
                .timeout(Duration.ofSeconds(10))
                .header("User-Agent", "MCFriendCustomizer/1.0")
                .GET().build();
        HttpResponse<String> resp2 = HTTP.send(req2, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (resp2.statusCode() != 200) {
            return new DownloadResult(false, "", null, "Не удалось получить профиль скина (код " + resp2.statusCode() + ").");
        }

        JsonObject profile2 = JsonParser.parseString(resp2.body()).getAsJsonObject();
        JsonArray props = profile2.getAsJsonArray("properties");
        String skinUrl = null;
        String detectedModel = "classic";
        for (JsonElement el : props) {
            JsonObject prop = el.getAsJsonObject();
            if ("textures".equals(prop.get("name").getAsString())) {
                String decoded = new String(Base64.getDecoder().decode(prop.get("value").getAsString()), StandardCharsets.UTF_8);
                JsonObject texJson = JsonParser.parseString(decoded).getAsJsonObject();
                JsonObject textures = texJson.getAsJsonObject("textures");
                if (textures != null && textures.has("SKIN")) {
                    JsonObject skinObj = textures.getAsJsonObject("SKIN");
                    skinUrl = skinObj.get("url").getAsString();
                    if (skinObj.has("metadata")) {
                        JsonObject meta = skinObj.getAsJsonObject("metadata");
                        if (meta != null && meta.has("model") && "slim".equalsIgnoreCase(meta.get("model").getAsString())) {
                            detectedModel = "slim";
                        }
                    }
                }
                break;
            }
        }
        if (skinUrl == null || skinUrl.isBlank()) {
            return new DownloadResult(false, "", null, "У игрока «" + canonicalName + "» не найден скин.");
        }

        byte[] png = downloadPngBytes(skinUrl);
        String fileName = CustomizerConfig.sanitizeFileName(canonicalName) + ".png";
        Files.write(CustomizerConfig.getSkinsDir().resolve(fileName), png);
        return new DownloadResult(true, fileName, detectedModel, "Скин игрока «" + canonicalName + "» скачан и применён!");
    }

    private static DownloadResult downloadDirectUrl(String url) throws Exception {
        byte[] png = downloadPngBytes(url);
        String baseName = "skin_" + (System.currentTimeMillis() % 100000);
        try {
            String path = URI.create(url).getPath();
            if (path != null && !path.isBlank()) {
                int slash = path.lastIndexOf('/');
                String tail = slash >= 0 ? path.substring(slash + 1) : path;
                if (tail.toLowerCase(Locale.ROOT).endsWith(".png")) {
                    tail = tail.substring(0, tail.length() - 4);
                }
                String safe = CustomizerConfig.sanitizeFileName(tail);
                if (!safe.isEmpty()) baseName = safe;
            }
        } catch (Exception ignored) {
        }
        String fileName = baseName + ".png";
        Files.write(CustomizerConfig.getSkinsDir().resolve(fileName), png);
        return new DownloadResult(true, fileName, null, "Скин скачан как " + fileName + "!");
    }

    private static byte[] downloadPngBytes(String url) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(12))
                .header("User-Agent", "MCFriendCustomizer/1.0")
                .GET().build();
        HttpResponse<byte[]> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofByteArray());
        if (resp.statusCode() != 200) {
            throw new IOException("HTTP " + resp.statusCode());
        }
        byte[] data = resp.body();
        if (data == null || data.length < 24 || data.length > 4 * 1024 * 1024) {
            throw new IOException("Неверный размер файла PNG");
        }
        // Check PNG magic bytes: 89 50 4E 47 0D 0A 1A 0A
        if ((data[0] & 0xFF) != 0x89 || data[1] != 0x50 || data[2] != 0x4E || data[3] != 0x47) {
            throw new IOException("Ссылка ведёт не на PNG-изображение");
        }
        // Validate PNG IHDR dimensions directly (bytes 12..15 = "IHDR", 16..19 = width, 20..23 = height)
        if (data[12] != 'I' || data[13] != 'H' || data[14] != 'D' || data[15] != 'R') {
            throw new IOException("Повреждённый PNG-файл (нет IHDR)");
        }
        int w = ((data[16] & 0xFF) << 24) | ((data[17] & 0xFF) << 16) | ((data[18] & 0xFF) << 8) | (data[19] & 0xFF);
        int h = ((data[20] & 0xFF) << 24) | ((data[21] & 0xFF) << 16) | ((data[22] & 0xFF) << 8) | (data[23] & 0xFF);
        if (w < 32 || h < 32 || w > 1024 || h > 1024) {
            throw new IOException("Неподдерживаемый размер скина: " + w + "x" + h);
        }
        return data;
    }
}
