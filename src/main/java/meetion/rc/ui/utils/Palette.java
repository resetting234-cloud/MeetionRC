package meetion.rc.ui.utils;

/**
 * Project palette per the METTRC design system.
 * <p>
 * All values are 0xAARRGGBB ints — Minecraft's {@code DrawContext.fill} consumes them
 * directly. Use {@link ColorUtil#withAlpha} to derive translucent variants on the fly.
 */
public final class Palette {

    private Palette() {}

    // --- backgrounds (deep black with subtle blue cast) ---
    public static final int BG_PANEL       = 0xF208080A; // rgba(8, 8, 10, 0.95)
    public static final int BG_PANEL_LIGHT = 0xD90E0E12; // rgba(14, 14, 18, 0.85)
    public static final int BG_DEEP        = 0xE0050507;
    public static final int BG_HOVER       = 0xFF14141A;
    public static final int BG_SCANLINE    = 0x14FFFFFF;

    // --- text ---
    public static final int TEXT_PRIMARY   = 0xFFFFFFFF;
    public static final int TEXT_SECONDARY = 0xFF888888;
    public static final int TEXT_MUTED     = 0xFF444444;

    // --- accent (aggressive red, used for active state, glow, dividers) ---
    public static final int RED            = 0xFFFF1A1A;
    public static final int RED_DIM        = 0xCCFF1A1A;
    public static final int RED_GLOW       = 0x40FF1A1A;
    public static final int RED_BG         = 0x1AFF1A1A;

    // --- toggle states ---
    public static final int TOGGLE_OFF_BG  = 0xFF1F1F26;
    public static final int TOGGLE_OFF_DOT = 0xFF555560;
    public static final int TOGGLE_ON_BG   = RED;
    public static final int TOGGLE_ON_DOT  = TEXT_PRIMARY;

    // --- HP / status ---
    public static final int HP_FULL        = 0xFF44E08A;
    public static final int HP_MID         = 0xFFFFB030;
    public static final int HP_LOW         = 0xFFFF5050;
}
