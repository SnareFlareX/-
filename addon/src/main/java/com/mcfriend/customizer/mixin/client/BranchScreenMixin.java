package com.mcfriend.customizer.mixin.client;

import com.mcfriend.customizer.client.PlaqueTextRenderer;
import com.mcfriend.customizer.config.CustomizerConfig;
import net.donila.friend.client.skills.BranchScreen;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = BranchScreen.class, remap = false)
public abstract class BranchScreenMixin {
    @Redirect(
            method = "method_25394(Lnet/minecraft/class_332;IIF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/class_332;method_25290(Lnet/minecraft/class_2960;IIFFIIII)V",
                    remap = false
            ),
            remap = false
    )
    private void mcfriendCustomizer$renderCustomBranchBg(
            class_332 g, class_2960 tex, int x, int y, float u, float v, int w, int h, int tw, int th
    ) {
        CustomizerConfig cfg = CustomizerConfig.get();
        if (!cfg.isDefaultName() && tex != null && tex.method_12832().endsWith("branch_borya.png")) {
            g.method_25290(PlaqueTextRenderer.BLANK_BRANCH_BORYA, x, y, u, v, w, h, tw, th);
            PlaqueTextRenderer.drawCenteredOnPlaque(g, cfg.getEffectiveName(), x + 145, y, 84, 26);
            return;
        }
        g.method_25290(tex, x, y, u, v, w, h, tw, th);
    }
}
