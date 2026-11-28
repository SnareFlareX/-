package com.mcfriend.customizer.mixin;

import com.mcfriend.customizer.util.NameFormatter;
import net.donila.friend.wheel.WheelShow;
import net.minecraft.class_2540;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = WheelShow.class, remap = false)
public abstract class WheelShowMixin {
    @Inject(method = "read(Lnet/minecraft/class_2540;)Lnet/donila/friend/wheel/WheelShow;", at = @At("RETURN"), cancellable = true, remap = false)
    private static void mcfriendCustomizer$formatWheel(class_2540 buf, CallbackInfoReturnable<WheelShow> cir) {
        WheelShow orig = cir.getReturnValue();
        if (orig == null) return;
        String question = NameFormatter.format(orig.вопрос());
        List<WheelShow.Ответ> answers = new ArrayList<>(orig.ответы().size());
        for (WheelShow.Ответ a : orig.ответы()) {
            answers.add(new WheelShow.Ответ(
                    a.id(),
                    NameFormatter.format(a.текст()),
                    a.можно(),
                    NameFormatter.format(a.почему()),
                    a.папка()
            ));
        }
        cir.setReturnValue(new WheelShow(question, answers, orig.кровать()));
    }
}
