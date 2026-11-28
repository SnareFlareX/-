package com.mcfriend.customizer.mixin.client;

import com.mcfriend.customizer.client.PlaqueTextRenderer;
import com.mcfriend.customizer.config.CustomizerConfig;
import net.donila.friend.client.okno.ОкноБориScreen;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ОкноБориScreen.class, remap = false)
public abstract class OknoBoryaScreenMixin {
    @Redirect(
            method = "табличка(Lnet/minecraft/class_332;D)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/class_332;method_25290(Lnet/minecraft/class_2960;IIFFIIII)V",
                    remap = false
            ),
            remap = false
    )
    private void mcfriendCustomizer$renderCustomPlaque(
            class_332 g, class_2960 tex, int x, int y, float u, float v, int w, int h, int tw, int th
    ) {
        CustomizerConfig cfg = CustomizerConfig.get();
        if (cfg.isDefaultName()) {
            g.method_25290(tex, x, y, u, v, w, h, tw, th);
            return;
        }
        g.method_25290(PlaqueTextRenderer.BLANK_PLAQUE, x, y, u, v, w, h, tw, th);
        PlaqueTextRenderer.drawCenteredOnPlaque(g, cfg.getEffectiveName(), x, y, w, h);
    }
}
