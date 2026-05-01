package meetion.rc.ui.hud;

import meetion.rc.MeetionRC;
import meetion.rc.ui.font.Fonts;
import meetion.rc.ui.utils.ColorUtil;
import meetion.rc.ui.utils.Palette;
import meetion.rc.ui.utils.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/**
 * Top-left brand wordmark with a small monospace FPS readout.
 *
 * <p>FPS string is throttled — we only re-format every 500ms to avoid producing
 * a fresh {@link String} every frame for a value that visually only changes at
 * sub-second granularity.
 */
public final class Watermark {

    private Watermark() {}

    private static final long FPS_REFRESH_MS = 500L;
    private static long lastRefresh = 0L;
    private static String cachedFps = "fps 0";

    public static void render(DrawContext ctx) {
        int x = 8;
        int y = 8;

        // wordmark
        Fonts.wordmark(ctx, x, y, false);
        int wordmarkW = Fonts.width(Fonts.INTER_BOLD, MeetionRC.NAME);

        // thin red accent line just under the wordmark
        int lineY = y + Fonts.lineHeight() + 2;
        RenderUtil.rect(ctx, x, lineY, Math.max(28, wordmarkW / 2), 1,
                ColorUtil.withAlpha(Palette.RED, 220));

        // FPS readout (monospace, secondary grey)
        long now = System.currentTimeMillis();
        if (now - lastRefresh > FPS_REFRESH_MS) {
            cachedFps = "fps " + MinecraftClient.getInstance().getCurrentFps();
            lastRefresh = now;
        }
        Fonts.draw(ctx, Fonts.MONO, cachedFps, x, lineY + 4, Palette.TEXT_SECONDARY);
    }
}
