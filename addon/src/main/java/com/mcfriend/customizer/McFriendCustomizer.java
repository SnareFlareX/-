package com.mcfriend.customizer;

import com.mcfriend.customizer.command.CustomizerCommands;
import com.mcfriend.customizer.config.CustomizerConfig;
import com.mcfriend.customizer.network.CustomizerPayloads;
import com.mcfriend.customizer.phrase.PhraseCatalog;
import com.mcfriend.customizer.phrase.PhraseEngine;
import com.mcfriend.customizer.util.NameFormatter;
import net.donila.friend.entity.BoryaEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.class_124;
import net.minecraft.class_1297;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class McFriendCustomizer implements ModInitializer {
    public static final String MOD_ID = "mcfriend_customizer";
    public static final Logger LOGGER = LoggerFactory.getLogger("MCFriendCustomizer");

    public static class_2960 id(String path) {
        return class_2960.method_60655(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        CustomizerConfig.load();
        PhraseCatalog.init();

        // Reload mcfriend Lines once with our overrides & name formatting active
        PhraseEngine.reloadLines();

        // Register networking payloads
        PayloadTypeRegistry.playS2C().register(CustomizerPayloads.SyncConfigS2C.ID, CustomizerPayloads.SyncConfigS2C.CODEC);
        PayloadTypeRegistry.playS2C().register(CustomizerPayloads.OpenGuiS2C.ID, CustomizerPayloads.OpenGuiS2C.CODEC);
        PayloadTypeRegistry.playS2C().register(CustomizerPayloads.DownloadSkinS2C.ID, CustomizerPayloads.DownloadSkinS2C.CODEC);
        PayloadTypeRegistry.playC2S().register(CustomizerPayloads.UpdateConfigC2S.ID, CustomizerPayloads.UpdateConfigC2S.CODEC);
        PayloadTypeRegistry.playC2S().register(CustomizerPayloads.TestPhraseC2S.ID, CustomizerPayloads.TestPhraseC2S.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(CustomizerPayloads.UpdateConfigC2S.ID, (payload, context) -> {
            MinecraftServer server = context.server();
            server.execute(() -> {
                try {
                    CustomizerConfig updated = CustomizerConfig.fromJson(payload.configJson());
                    applyAndBroadcast(server, updated);
                } catch (Exception e) {
                    LOGGER.warn("Некорректный пакет настроек от игрока: {}", e.getMessage());
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(CustomizerPayloads.TestPhraseC2S.ID, (payload, context) -> {
            MinecraftServer server = context.server();
            class_3222 player = context.player();
            server.execute(() -> speakTestPhrase(player, payload.text()));
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            class_3222 player = handler.field_14140;
            if (player != null && ServerPlayNetworking.canSend(player, CustomizerPayloads.SyncConfigS2C.ID)) {
                ServerPlayNetworking.send(player, new CustomizerPayloads.SyncConfigS2C(CustomizerConfig.toJson(CustomizerConfig.get())));
            }
        });

        CustomizerCommands.register();
        LOGGER.info("MCFriend Customizer загружен! Имя друга: «{}», скин: «{}»",
                CustomizerConfig.get().getEffectiveName(),
                CustomizerConfig.get().skinFile.isEmpty() ? "стандартный" : CustomizerConfig.get().skinFile);
    }

    public static void applyAndBroadcast(MinecraftServer server, CustomizerConfig updated) {
        CustomizerConfig.set(updated, true);
        PhraseEngine.reloadLines();
        if (server != null) {
            updateAllFriendEntities(server);
            String json = CustomizerConfig.toJson(CustomizerConfig.get());
            CustomizerPayloads.SyncConfigS2C pkt = new CustomizerPayloads.SyncConfigS2C(json);
            for (class_3222 player : server.method_3760().method_14571()) {
                if (ServerPlayNetworking.canSend(player, CustomizerPayloads.SyncConfigS2C.ID)) {
                    ServerPlayNetworking.send(player, pkt);
                }
            }
        }
    }

    public static void updateAllFriendEntities(MinecraftServer server) {
        if (server == null) return;
        for (class_3218 world : server.method_3738()) {
            for (class_1297 entity : world.method_27909()) {
                if (entity instanceof BoryaEntity borya) {
                    applyNameToEntity(borya);
                }
            }
        }
    }

    public static void applyNameToEntity(BoryaEntity borya) {
        if (borya == null) return;
        CustomizerConfig cfg = CustomizerConfig.get();
        if (cfg.isDefaultName() && !cfg.showNametag) {
            borya.method_5665(null);
            borya.method_5880(false);
        } else {
            borya.method_5665(class_2561.method_43470(cfg.getEffectiveName()));
            borya.method_5880(cfg.showNametag);
        }
    }

    public static boolean speakTestPhrase(class_3222 player, String rawText) {
        if (player == null || rawText == null || rawText.isBlank()) return false;
        BoryaEntity nearest = findNearestFriend(player, 64.0);
        if (nearest == null) {
            player.method_7353(class_2561.method_43470("Рядом не найден " + CustomizerConfig.get().getEffectiveName() + " (в радиусе 64 блоков).")
                    .method_27692(class_124.method_544('e')), false);
            return false;
        }
        String clean = NameFormatter.format(rawText.trim());
        nearest.bubble().замолчать();
        nearest.bubble().сказать(clean);
        return true;
    }

    public static BoryaEntity findNearestFriend(class_3222 player, double maxDist) {
        if (player == null) return null;
        class_3218 world = player.method_51469();
        if (world == null) return null;
        double bestDistSq = maxDist * maxDist;
        BoryaEntity best = null;
        for (class_1297 e : world.method_27909()) {
            if (e instanceof BoryaEntity borya) {
                double d2 = borya.method_5858(player);
                if (d2 < bestDistSq) {
                    bestDistSq = d2;
                    best = borya;
                }
            }
        }
        return best;
    }
}
