package meetion.rc.core.setting.impl;

import meetion.rc.core.setting.Setting;

import java.awt.Color;

public class ColorSetting extends Setting<Color> {
    private boolean rainbow;

    public ColorSetting(String name, Color defaultColor) {
        super(name, defaultColor);
    }

    public int getRGB() { return value.getRGB(); }
    public boolean isRainbow() { return rainbow; }
    public void setRainbow(boolean rainbow) { this.rainbow = rainbow; }

    public int animate() {
        if (!rainbow) return value.getRGB();
        float hue = (System.currentTimeMillis() % 3000L) / 3000f;
        return Color.HSBtoRGB(hue, 1f, 1f);
    }
}
