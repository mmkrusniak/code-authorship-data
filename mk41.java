package com.miolean.arena.entities;

import com.miolean.arena.ui.FieldDisplayPanel;

import java.awt.*;

public class EntityUtils {

    public static void drawCircularGlow(Graphics2D g, Entity e, int x, int y) {
        float[] dist = {0.0f, 0.45f};
        Color[] colors = {Color.WHITE, new Color(0, 0, 0, 0)};

        final double BORDER_FACTOR = 1.6;

        g.setPaint(new RadialGradientPaint((float) x, (float) y, (float) (e.getWidth() * BORDER_FACTOR), dist, colors, MultipleGradientPaint.CycleMethod.NO_CYCLE));
        g.fillOval((int) (x - (e.getWidth() * BORDER_FACTOR / 2)), (int) (y - (e.getHeight() * BORDER_FACTOR) / 2), (int) (e.getWidth() * BORDER_FACTOR), (int) (e.getHeight() * BORDER_FACTOR));
    }

    public static void processPolygon(Entity e) {

        double sinR = Math.sin(e.getR());
        double cosR = Math.cos(e.getR());
    }
}
