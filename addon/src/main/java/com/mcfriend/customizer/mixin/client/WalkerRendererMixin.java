package com.mcfriend.customizer.mixin.client;

import com.mcfriend.customizer.client.ClientSkinManager;
import net.donila.friend.client.WalkerRenderer;
import net.donila.friend.entity.BoryaEntity;
import net.donila.friend.entity.WalkerEntity;
import net.minecraft.class_2960;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = WalkerRenderer.class, remap = false)
public abstract class WalkerRendererMixin {
    @Inject(
            method = "getTextureLocation(Lnet/donila/friend/entity/WalkerEntity;)Lnet/minecraft/class_2960;",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void mcfriendCustomizer$useCustomSkin(WalkerEntity entity, CallbackInfoReturnable<class_2960> cir) {
        if (entity instanceof BoryaEntity) {
            class_2960 custom = ClientSkinManager.getActiveCustomTexture();
            if (custom != null) {
                cir.setReturnValue(custom);
            }
        }
    }
}
