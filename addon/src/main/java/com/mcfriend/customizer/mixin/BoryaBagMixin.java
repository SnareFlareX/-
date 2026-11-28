package com.mcfriend.customizer.mixin;

import com.mcfriend.customizer.util.NameFormatter;
import net.donila.friend.entity.BoryaBag;
import net.minecraft.class_2561;
import net.minecraft.class_5250;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = BoryaBag.class, remap = false)
public abstract class BoryaBagMixin {
    @Redirect(
            method = "открыть(Lnet/minecraft/class_1657;Lnet/donila/friend/entity/BoryaEntity;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/class_2561;method_43470(Ljava/lang/String;)Lnet/minecraft/class_5250;", remap = false),
            remap = false
    )
    private static class_5250 mcfriendCustomizer$renameBagTitle(String title) {
        return class_2561.method_43470(NameFormatter.format(title));
    }
}
