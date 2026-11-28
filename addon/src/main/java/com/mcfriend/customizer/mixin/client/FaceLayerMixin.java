package com.mcfriend.customizer.mixin.client;

import com.mcfriend.customizer.client.ClientSkinManager;
import net.donila.friend.client.FaceLayer;
import net.minecraft.class_1309;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FaceLayer.class, remap = false)
public abstract class FaceLayerMixin {
    @Inject(
            method = "render(Lnet/minecraft/class_4587;Lnet/minecraft/class_4597;ILnet/minecraft/class_1309;FFFFFF)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void mcfriendCustomizer$toggleCatEyes(
            class_4587 pose, class_4597 buffers, int light, class_1309 entity,
            float limbAngle, float limbDistance, float tickDelta, float age, float headYaw, float headPitch,
            CallbackInfo ci
    ) {
        if (!ClientSkinManager.shouldRenderCatEyes()) {
            ci.cancel();
        }
    }
}
