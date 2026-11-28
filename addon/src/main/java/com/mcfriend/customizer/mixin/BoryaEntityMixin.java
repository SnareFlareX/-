package com.mcfriend.customizer.mixin;

import com.mcfriend.customizer.McFriendCustomizer;
import com.mcfriend.customizer.config.CustomizerConfig;
import net.donila.friend.entity.BoryaEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BoryaEntity.class, remap = false)
public abstract class BoryaEntityMixin {
    @Unique
    private long mcfriendCustomizer$appliedVersion = -1L;

    @Inject(method = "method_5773()V", at = @At("TAIL"), remap = false)
    private void mcfriendCustomizer$syncEntityName(CallbackInfo ci) {
        BoryaEntity self = (BoryaEntity) (Object) this;
        if (self.method_37908() == null || self.method_37908().field_9236) {
            return;
        }
        long ver = CustomizerConfig.getVersion();
        if (this.mcfriendCustomizer$appliedVersion != ver) {
            this.mcfriendCustomizer$appliedVersion = ver;
            McFriendCustomizer.applyNameToEntity(self);
        }
    }
}
