package com.mcfriend.customizer.client;

import com.mcfriend.customizer.McFriendCustomizer;
import com.mcfriend.customizer.config.CustomizerConfig;
import com.mcfriend.customizer.network.CustomizerPayloads;
import com.mcfriend.customizer.phrase.PhraseEngine;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3675;

@Environment(EnvType.CLIENT)
public final class McFriendCustomizerClient implements ClientModInitializer {
    private static final int GLFW_KEY_G = 71;
    private static class_304 openKey;

    @Override
    public void onInitializeClient() {
        openKey = KeyBindingHelper.registerKeyBinding(new class_304(
                "key.mcfriend_customizer.open",
                class_3675.class_307.field_1668,
                GLFW_KEY_G,
                "category.mcfriend"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientSkinManager.tickCheckReload();
            while (openKey.method_1436()) {
                if (client.field_1755 == null) {
                    client.method_1507(new CustomizerScreen("name"));
                }
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(CustomizerPayloads.SyncConfigS2C.ID, (payload, context) -> {
            context.client().execute(() -> {
                try {
                    CustomizerConfig synced = CustomizerConfig.fromJson(payload.configJson());
                    CustomizerConfig.set(synced, false);
                    PhraseEngine.reloadLines();
                    ClientSkinManager.reloadNow();
                } catch (Exception e) {
                    McFriendCustomizer.LOGGER.warn("Ошибка применения синхронизированных настроек: {}", e.getMessage());
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(CustomizerPayloads.OpenGuiS2C.ID, (payload, context) -> {
            context.client().execute(() -> {
                context.client().method_1507(new CustomizerScreen(payload.initialTab()));
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(CustomizerPayloads.DownloadSkinS2C.ID, (payload, context) -> {
            ClientSkinManager.downloadSkinAsync(payload.source(), res -> {
                class_310 client = class_310.method_1551();
                if (res.ok()) {
                    CustomizerConfig cfg = CustomizerConfig.get().copy();
                    cfg.skinFile = res.fileName();
                    if (res.detectedModel() != null) {
                        cfg.skinModel = res.detectedModel();
                    }
                    cfg.showCatEyes = false;
                    applyFromClient(cfg);
                    if (client != null && client.field_1724 != null) {
                        client.field_1724.method_7353(class_2561.method_43470("[Друг] " + res.message())
                                .method_27692(class_124.method_544('a')), false);
                    }
                } else if (client != null && client.field_1724 != null) {
                    client.field_1724.method_7353(class_2561.method_43470("[Друг] " + res.message())
                            .method_27692(class_124.method_544('c')), false);
                }
            });
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            client.execute(() -> {
                CustomizerConfig.load();
                ClientSkinManager.reloadNow();
            });
        });
    }

    public static void applyFromClient(CustomizerConfig updated) {
        CustomizerConfig.set(updated, true);
        PhraseEngine.reloadLines();
        ClientSkinManager.forceReloadOnRenderThread();
        if (ClientPlayNetworking.canSend(CustomizerPayloads.UpdateConfigC2S.ID)) {
            ClientPlayNetworking.send(new CustomizerPayloads.UpdateConfigC2S(CustomizerConfig.toJson(CustomizerConfig.get())));
        }
    }

    public static void sendTestPhrase(String text) {
        if (text == null || text.isBlank()) return;
        if (ClientPlayNetworking.canSend(CustomizerPayloads.TestPhraseC2S.ID)) {
            ClientPlayNetworking.send(new CustomizerPayloads.TestPhraseC2S(text.trim()));
        }
    }
}
