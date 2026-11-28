package com.mcfriend.customizer.mixin.client;

import com.mcfriend.customizer.util.NameFormatter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.donila.friend.client.skills.FightScreen$Строка", remap = false)
public abstract class FightLineMixin {
    @Shadow
    @Final
    @Mutable
    String t;

    @Inject(method = "<init>(Ljava/lang/String;I)V", at = @At("RETURN"), remap = false)
    private void mcfriendCustomizer$formatSkillLine(String text, int color, CallbackInfo ci) {
        if (this.t != null && !this.t.isEmpty()) {
            this.t = NameFormatter.format(this.t);
        }
    }
}
