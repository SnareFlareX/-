package com.mcfriend.customizer.mixin;

import com.mcfriend.customizer.phrase.PhraseEngine;
import net.donila.friend.talk.Lines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = Lines.class, remap = false)
public abstract class LinesMixin {
    @Inject(method = "прочитать(Ljava/lang/String;)Ljava/util/List;", at = @At("RETURN"), cancellable = true, remap = false)
    private static void mcfriendCustomizer$overrideRead(String name, CallbackInfoReturnable<List<String>> cir) {
        List<String> processed = PhraseEngine.processLoadedFile(name, cir.getReturnValue());
        cir.setReturnValue(processed);
    }
}
