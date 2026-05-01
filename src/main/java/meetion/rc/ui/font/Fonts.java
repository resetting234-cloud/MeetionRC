package meetion.rc.ui.font;

import meetion.rc.MeetionRC;
import meetion.rc.ui.utils.Palette;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Central registry of MeetionRC's custom TTF fonts.
 *
 * <p>Each font is a Mojang font definition under
 * {@code assets/meetionrc/font/<id>.json} that points at a {@code .ttf} bundled in
 * the same directory. Use {@link #styled} to attach a font + color to any literal
 * string, or {@link #draw} for a one-liner inside a render loop.
 *
 * <p>{@link #INTER_BOLD} currently aliases {@link #INTER} (one TTF, larger size) — the
 * Inter v4.1 release ships only the variable-axis Bold which Mojang's TTF provider
 * cannot select. We can swap in a static Bold .ttf later without touching call sites.
 */
public final class Fonts {

    private Fonts() {}

    public static final Identifier INTER          = Identifier.of(MeetionRC.MOD_ID, "inter");
    public static final Identifier INTER_BOLD     = Identifier.of(MeetionRC.MOD_ID, "inter_bold");
    public static final Identifier MONO           = Identifier.of(MeetionRC.MOD_ID, "jetbrains_mono");
    public static final Identifier MONO_BOLD      = Identifier.of(MeetionRC.MOD_ID, "jetbrains_mono_bold");

    /** Current TextRenderer (vanilla single instance). */
    public static TextRenderer renderer() {
        return MinecraftClient.getInstance().textRenderer;
    }

    // ---------------------------------------------------------------------------
    // Style construction
    // ---------------------------------------------------------------------------

    /** Build a styled {@link Text} with the given font + ARGB color. Alpha is ignored by Style. */
    public static MutableText styled(String text, Identifier font, int rgb) {
        return Text.literal(text).setStyle(Style.EMPTY.withFont(font).withColor(rgb & 0x00FFFFFF));
    }

    /** Build a styled {@link Text} with just a font (color comes from caller). */
    public static MutableText styled(String text, Identifier font) {
        return Text.literal(text).setStyle(Style.EMPTY.withFont(font));
    }

    // ---------------------------------------------------------------------------
    // Drawing
    // ---------------------------------------------------------------------------

    /**
     * Draw a string with the given font and color (no shadow).
     * @return advance-width in pixels.
     */
    public static int draw(DrawContext ctx, Identifier font, String text, int x, int y, int color) {
        MutableText t = styled(text, font);
        ctx.drawText(renderer(), t, x, y, color, false);
        return renderer().getWidth(t);
    }

    /** Draw a string with shadow. Returns advance-width. */
    public static int drawShadow(DrawContext ctx, Identifier font, String text, int x, int y, int color) {
        MutableText t = styled(text, font);
        ctx.drawText(renderer(), t, x, y, color, true);
        return renderer().getWidth(t);
    }

    /** Measure the rendered width of a string in the given font. */
    public static int width(Identifier font, String text) {
        return renderer().getWidth(styled(text, font));
    }

    /** Convenience: line height of the bound TextRenderer. */
    public static int lineHeight() {
        return renderer().fontHeight;
    }

    // ---------------------------------------------------------------------------
    // Branded helpers
    // ---------------------------------------------------------------------------

    /**
     * Render the wordmark "METTRC" with red "MET" + white "TRC" in a single call.
     * Returns the total pixel width.
     */
    public static int wordmark(DrawContext ctx, int x, int y, boolean shadow) {
        Identifier font = INTER_BOLD;
        MutableText met = styled(MeetionRC.NAME_RED, font, Palette.RED);
        MutableText trc = styled(MeetionRC.NAME_WHITE, font, Palette.TEXT_PRIMARY);
        ctx.drawText(renderer(), met, x, y, Palette.RED, shadow);
        int wMet = renderer().getWidth(met);
        ctx.drawText(renderer(), trc, x + wMet, y, Palette.TEXT_PRIMARY, shadow);
        return wMet + renderer().getWidth(trc);
    }
}
