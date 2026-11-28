package com.mcfriend.customizer.mixin.client;

import com.mcfriend.customizer.client.ClientSkinManager;
import net.minecraft.class_2960;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Constructor;

@Mixin(targets = "net.donila.friend.client.skills.SkillArt", remap = false)
public abstract class SkillArtMixin {
    @Unique
    private static volatile Constructor<?> mcfriendCustomizer$kartinkaCtor;

    @Inject(
            method = "картинка(Lnet/minecraft/class_2960;)Lnet/donila/friend/client/skills/SkillArt$Картинка;",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void mcfriendCustomizer$overrideBoryaSkillSkin(class_2960 id, CallbackInfoReturnable<Object> cir) {
        if (id == null || !id.equals(ClientSkinManager.DEFAULT_BORYA_SKIN)) {
            return;
        }
        int[] customPixels = ClientSkinManager.getActiveArgb64();
        if (customPixels == null || customPixels.length != 4096) {
            return;
        }
        try {
            Constructor<?> ctor = mcfriendCustomizer$kartinkaCtor;
            if (ctor == null) {
                Class<?> cls = Class.forName("net.donila.friend.client.skills.SkillArt$Картинка");
                ctor = cls.getDeclaredConstructor(int.class, int.class, int[].class);
                ctor.setAccessible(true);
                mcfriendCustomizer$kartinkaCtor = ctor;
            }
            cir.setReturnValue(ctor.newInstance(64, 64, customPixels));
        } catch (Exception ignored) {
        }
    }
}
