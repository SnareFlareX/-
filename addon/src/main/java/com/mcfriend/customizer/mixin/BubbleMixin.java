package com.mcfriend.customizer.mixin;

import com.mcfriend.customizer.util.NameFormatter;
import net.donila.friend.talk.Bubble;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = Bubble.class, remap = false)
public abstract class BubbleMixin {
    @ModifyVariable(method = "положить(Ljava/lang/String;)V", at = @At("HEAD"), ordinal = 0, argsOnly = true, remap = false)
    private String mcfriendCustomizer$formatBubbleText(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return NameFormatter.format(text);
    }
}
