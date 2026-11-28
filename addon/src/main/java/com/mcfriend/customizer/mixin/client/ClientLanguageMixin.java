package com.mcfriend.customizer.mixin.client;

import com.mcfriend.customizer.util.NameFormatter;
import net.minecraft.class_1078;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = class_1078.class, remap = false)
public abstract class ClientLanguageMixin {
    @Inject(
            method = "method_4679(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;",
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private void mcfriendCustomizer$renameLangKeys(String key, String fallback, CallbackInfoReturnable<String> cir) {
        if (key == null) return;
        if (key.contains("mcfriend_customizer")) return;
        if (key.contains("mcfriend") || key.contains("borya")) {
            String value = cir.getReturnValue();
            if (value != null && !value.isEmpty()) {
                String formatted = NameFormatter.format(value);
                if (!formatted.equals(value)) {
                    cir.setReturnValue(formatted);
                }
            }
        }
    }
}
