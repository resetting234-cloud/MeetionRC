package meetion.rc.ui.utils;

/**
 * Pure helpers for ARGB color manipulation. No allocations, no AWT imports —
 * cheap to call inside a render loop.
 *
 * <p>Color encoding throughout the UI layer: {@code 0xAARRGGBB} (Minecraft's
 * {@code DrawContext.fill} convention). Channel value range is {@code 0..255}.
 */
public final class ColorUtil {

    private ColorUtil() {}

    /** Pack an ARGB color from individual channels (each 0..255). */
    public static int argb(int r, int g, int b, int a) {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    /** Pack an ARGB color from RGB ints + a 0..1 alpha. */
    public static int argb(int r, int g, int b, float alpha) {
        return argb(r, g, b, (int) (clamp01(alpha) * 255));
    }

    /** Replace a color's alpha (0..255). */
    public static int withAlpha(int argb, int alpha) {
        return (argb & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
    }

    /** Replace a color's alpha (0..1 float). */
    public static int withAlpha(int argb, float alpha) {
        return withAlpha(argb, (int) (clamp01(alpha) * 255));
    }

    /** Multiply an existing alpha by a factor (0..1). Useful for fading layers in glow rings. */
    public static int multiplyAlpha(int argb, float factor) {
        int a = (argb >>> 24) & 0xFF;
        return withAlpha(argb, (int) (a * clamp01(factor)));
    }

    /** Linearly interpolate two ARGB colors. */
    public static int lerp(int from, int to, float t) {
        t = clamp01(t);
        int af = (from >>> 24) & 0xFF, ar = (from >>> 16) & 0xFF, ag = (from >>> 8) & 0xFF, ab = from & 0xFF;
        int bf = (to   >>> 24) & 0xFF, br = (to   >>> 16) & 0xFF, bg = (to   >>> 8) & 0xFF, bb = to   & 0xFF;
        int a = (int) (af + (bf - af) * t);
        int r = (int) (ar + (br - ar) * t);
        int g = (int) (ag + (bg - ag) * t);
        int b = (int) (ab + (bb - ab) * t);
        return argb(r, g, b, a);
    }

    /** Choose a palette color based on a 0..1 ratio (e.g. HP fraction). */
    public static int hpRamp(float ratio) {
        ratio = clamp01(ratio);
        if (ratio > 0.6f) return Palette.HP_FULL;
        if (ratio > 0.3f) return Palette.HP_MID;
        return Palette.HP_LOW;
    }

    public static float clamp01(float v) {
        if (v < 0) return 0;
        if (v > 1) return 1;
        return v;
    }
}
