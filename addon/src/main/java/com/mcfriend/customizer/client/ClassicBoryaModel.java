package com.mcfriend.customizer.client;

import net.donila.friend.entity.BoryaEntity;
import net.donila.friend.entity.WalkerEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1309;
import net.minecraft.class_1802;
import net.minecraft.class_3489;
import net.minecraft.class_4050;
import net.minecraft.class_572;
import net.minecraft.class_591;
import net.minecraft.class_630;

import java.util.List;

@Environment(EnvType.CLIENT)
public final class ClassicBoryaModel extends class_591<WalkerEntity> {
    private static final float ARM_FORWARD = -0.8f;

    public ClassicBoryaModel(class_630 root) {
        super(root, false);
    }

    @Override
    public void method_17086(WalkerEntity who, float limbAngle, float limbDistance, float tickDelta) {
        this.field_3395 = who.method_6115() && who.method_6030().method_31574(class_1802.field_8102)
                ? class_572.class_573.field_3403
                : class_572.class_573.field_3409;
        this.field_3399 = who.method_6115() && who.method_6058() == class_1268.field_5810
                && who.method_6030().method_31574(class_1802.field_8255)
                ? class_572.class_573.field_3406
                : class_572.class_573.field_3409;
        super.method_17086(who, limbAngle, limbDistance, tickDelta);
    }

    @Override
    public void method_17087(WalkerEntity who, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        super.method_17087(who, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
        if (who.method_41328(class_4050.field_18078)) {
            for (class_630 part : List.of(this.field_3401, this.field_27433, this.field_3392, this.field_3397)) {
                part.field_3654 = 0.0f;
                part.field_3675 = 0.0f;
                part.field_3674 = 0.0f;
            }
            this.field_3486.method_17138(this.field_3401);
            this.field_3484.method_17138(this.field_27433);
            this.field_3479.method_17138(this.field_3392);
            this.field_3482.method_17138(this.field_3397);
            return;
        }
        boolean pointing = who instanceof BoryaEntity borya && borya.показывает();
        if (pointing
                || who.method_6079().method_31573(class_3489.field_20344)
                || who.method_6079().method_31573(class_3489.field_15527)
                || who.method_6079().method_31573(class_3489.field_42611)
                || who.method_6079().method_31573(class_3489.field_42612)) {
            this.field_27433.field_3654 = ARM_FORWARD;
            this.field_27433.field_3675 = 0.0f;
            this.field_27433.field_3674 = 0.0f;
            this.field_3484.method_17138(this.field_27433);
        }
    }
}
