package com.mcfriend.customizer.mixin.client;

import com.mcfriend.customizer.client.ClassicBoryaModel;
import com.mcfriend.customizer.client.ClientSkinManager;
import net.donila.friend.client.BoryaRenderer;
import net.donila.friend.client.WalkerRenderer;
import net.donila.friend.entity.WalkerEntity;
import net.minecraft.class_2960;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_5601;
import net.minecraft.class_5617;
import net.minecraft.class_591;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BoryaRenderer.class, remap = false)
public abstract class BoryaRendererMixin extends WalkerRenderer {
    @Unique
    private class_591<WalkerEntity> mcfriendCustomizer$slimModel;
    @Unique
    private class_591<WalkerEntity> mcfriendCustomizer$classicModel;

    protected BoryaRendererMixin(class_5617.class_5618 ctx) {
        super(ctx);
    }

    @Inject(method = "<init>(Lnet/minecraft/class_5617$class_5618;)V", at = @At("TAIL"), remap = false)
    private void mcfriendCustomizer$cacheClassicModel(class_5617.class_5618 ctx, CallbackInfo ci) {
        this.mcfriendCustomizer$slimModel = this.field_4737;
        try {
            class_5601 playerLayer = new class_5601(class_2960.method_60656("player"), "main");
            this.mcfriendCustomizer$classicModel = new ClassicBoryaModel(ctx.method_32167(playerLayer));
        } catch (Exception ignored) {
            this.mcfriendCustomizer$classicModel = this.mcfriendCustomizer$slimModel;
        }
    }

    @Inject(
            method = "render(Lnet/donila/friend/entity/WalkerEntity;FFLnet/minecraft/class_4587;Lnet/minecraft/class_4597;I)V",
            at = @At("HEAD"),
            remap = false
    )
    private void mcfriendCustomizer$selectArmModel(
            WalkerEntity entity, float yaw, float partialTick, class_4587 pose, class_4597 buffers, int light, CallbackInfo ci
    ) {
        if (this.mcfriendCustomizer$slimModel == null) return;
        if (ClientSkinManager.isClassicArms() && this.mcfriendCustomizer$classicModel != null) {
            this.field_4737 = this.mcfriendCustomizer$classicModel;
        } else {
            this.field_4737 = this.mcfriendCustomizer$slimModel;
        }
    }
}
