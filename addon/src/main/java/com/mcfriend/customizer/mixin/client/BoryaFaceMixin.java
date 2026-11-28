package com.mcfriend.customizer.mixin.client;

import com.mcfriend.customizer.client.ClientSkinManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.donila.friend.client.skills.BoryaFace", remap = false)
public abstract class BoryaFaceMixin {
    @Inject(
            method = "нарисовать([III[[DDZ)[I",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void mcfriendCustomizer$renderPlainHeadIcon(
            int[] skinPixels, int skinW, int skinH, double[][] eyes, double pupil, boolean onlyCollar,
            CallbackInfoReturnable<int[]> cir
    ) {
        if (onlyCollar || ClientSkinManager.shouldRenderCatEyes()) {
            return;
        }
        if (skinPixels == null || skinW < 64 || skinH < 32) {
            return;
        }
        int[] out = new int[64 * 64];
        int scale = 5; // 8 * 5 = 40px head matching original BoryaFace (OX=12, OY=9, K=5)
        int ox = 12;
        int oy = 9;
        for (int fy = 0; fy < 8; fy++) {
            for (int fx = 0; fx < 8; fx++) {
                int base = skinPixels[(8 + fy) * skinW + (8 + fx)];
                int color = base | 0xFF000000;
                if (skinW >= 48) {
                    int hat = skinPixels[(8 + fy) * skinW + (40 + fx)];
                    int ha = (hat >>> 24) & 0xFF;
                    if (ha == 255) {
                        color = hat;
                    } else if (ha > 0) {
                        int br = (color >>> 16) & 0xFF;
                        int bg = (color >>> 8) & 0xFF;
                        int bb = color & 0xFF;
                        int hr = (hat >>> 16) & 0xFF;
                        int hg = (hat >>> 8) & 0xFF;
                        int hb = hat & 0xFF;
                        int rr = (hr * ha + br * (255 - ha)) / 255;
                        int rg = (hg * ha + bg * (255 - ha)) / 255;
                        int rb = (hb * ha + bb * (255 - ha)) / 255;
                        color = 0xFF000000 | (rr << 16) | (rg << 8) | rb;
                    }
                }
                for (int dy = 0; dy < scale; dy++) {
                    int py = oy + fy * scale + dy;
                    int rowOff = py * 64;
                    for (int dx = 0; dx < scale; dx++) {
                        out[rowOff + ox + fx * scale + dx] = color;
                    }
                }
            }
        }
        cir.setReturnValue(out);
    }
}
