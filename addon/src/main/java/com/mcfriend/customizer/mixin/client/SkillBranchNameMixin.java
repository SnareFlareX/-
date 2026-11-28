package com.mcfriend.customizer.mixin.client;

import com.mcfriend.customizer.config.CustomizerConfig;
import net.donila.friend.client.skills.SkillBranches;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SkillBranches.Ветка.class, remap = false)
public abstract class SkillBranchNameMixin {
    @Shadow
    public abstract String id();

    @Inject(method = "название()Ljava/lang/String;", at = @At("RETURN"), cancellable = true, remap = false)
    private void mcfriendCustomizer$renameBoryaBranch(CallbackInfoReturnable<String> cir) {
        if ("borya".equals(this.id())) {
            cir.setReturnValue(CustomizerConfig.get().getEffectiveName());
        }
    }
}
