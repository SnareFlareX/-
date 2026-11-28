package com.mcfriend.customizer.client;

import com.mcfriend.customizer.McFriendCustomizer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_4587;

@Environment(EnvType.CLIENT)
public final class PlaqueTextRenderer {
    public static final class_2960 BLANK_PLAQUE = McFriendCustomizer.id("textures/gui/blank_plaque.png");
    public static final class_2960 BLANK_BRANCH_BORYA = McFriendCustomizer.id("textures/gui/blank_branch_borya.png");

    private PlaqueTextRenderer() {
    }

    /**
     * Draws a friend's custom name centered inside a stone plaque rectangle (plaqueX, plaqueY, 84x26),
     * automatically scaling to fit cleanly within the golden border.
     */
    public static void drawCenteredOnPlaque(class_332 g, String name, int plaqueX, int plaqueY, int plaqueW, int plaqueH) {
        if (name == null || name.isBlank()) return;
        class_310 client = class_310.method_1551();
        if (client == null || client.field_1772 == null) return;
        class_327 font = client.field_1772;

        String text = name.trim();
        int textW = Math.max(1, font.method_1727(text));
        float maxW = Math.max(16.0f, plaqueW - 14.0f);
        float scale = Math.min(1.15f, maxW / (float) textW);
        if (scale < 0.52f) {
            text = font.method_27523(text, (int) Math.floor(maxW / 0.52f));
            textW = Math.max(1, font.method_1727(text));
            scale = Math.min(1.15f, maxW / (float) textW);
        }

        float cx = plaqueX + plaqueW * 0.5f;
        float cy = plaqueY + plaqueH * 0.5f;

        class_4587 pose = g.method_51448();
        pose.method_22903();
        pose.method_46416(cx, cy, 0.0f);
        pose.method_22905(scale, scale, 1.0f);
        int drawX = -textW / 2;
        int drawY = -4;
        g.method_25303(font, text, drawX, drawY, 0xFFF5EBD0);
        pose.method_22909();
    }
}
