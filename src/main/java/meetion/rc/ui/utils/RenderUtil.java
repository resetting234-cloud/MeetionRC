package meetion.rc.ui.utils;

import net.minecraft.client.gui.DrawContext;

/**
 * Render primitives built on top of {@link DrawContext}. No raw GL calls — every
 * primitive composes vanilla {@code fill} / {@code fillGradient} batches so it stays
 * compatible with Mojang's render pipeline (and survives shader-pack overrides).
 *
 * <h3>Why integer rounded corners?</h3>
 * Mojang's gui pipeline doesn't expose vertex shaders for screen-space AA without
 * either uploading custom GLSL programs (heavy, prone to break across patch versions)
 * or sampling SDF textures (extra texture roundtrips). For the design we want, an
 * integer-step quarter-circle decomposition is indistinguishable at the panel sizes
 * the ClickGUI uses — the visual quality cost is negligible and the perf cost is zero.
 *
 * <h3>Alpha correctness</h3>
 * Every method takes an ARGB int. Passing translucent colors works directly because
 * {@code ctx.fill} respects the alpha channel and the gui blending state is left at
 * the default (one - source alpha) on the consumer side.
 */
public final class RenderUtil {

    private RenderUtil() {}

    // ---------------------------------------------------------------------------
    // Filled rectangles
    // ---------------------------------------------------------------------------

    /** Plain solid rectangle, position+size (NOT position+position). */
    public static void rect(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x, y, x + w, y + h, color);
    }

    /** Plain solid rectangle from corner-to-corner coordinates. */
    public static void rectXY(DrawContext ctx, int x1, int y1, int x2, int y2, int color) {
        ctx.fill(x1, y1, x2, y2, color);
    }

    /** Hollow outlined rectangle of given thickness. */
    public static void outline(DrawContext ctx, int x, int y, int w, int h, int thickness, int color) {
        ctx.fill(x, y, x + w, y + thickness, color);                       // top
        ctx.fill(x, y + h - thickness, x + w, y + h, color);               // bottom
        ctx.fill(x, y + thickness, x + thickness, y + h - thickness, color); // left
        ctx.fill(x + w - thickness, y + thickness, x + w, y + h - thickness, color); // right
    }

    // ---------------------------------------------------------------------------
    // Rounded rectangles
    // ---------------------------------------------------------------------------

    /** Rounded-corner rectangle with the same corner radius on all four corners. */
    public static void roundedRect(DrawContext ctx, int x, int y, int w, int h, int radius, int color) {
        if (radius <= 0) { rect(ctx, x, y, w, h, color); return; }
        int r = Math.min(radius, Math.min(w, h) / 2);

        // central solid blocks (everything except the four rounded corners)
        ctx.fill(x + r,    y,        x + w - r, y + h,     color); // wide center
        ctx.fill(x,        y + r,    x + r,     y + h - r, color); // left strip
        ctx.fill(x + w - r,y + r,    x + w,     y + h - r, color); // right strip

        // four rounded corners drawn as horizontal scanlines
        for (int dy = 0; dy < r; dy++) {
            // chord half-width at row dy
            int yy = r - dy - 1;
            int dx = (int) Math.round(Math.sqrt(r * r - yy * yy));
            // top-left
            ctx.fill(x + r - dx, y + dy,         x + r,        y + dy + 1,         color);
            // top-right
            ctx.fill(x + w - r,  y + dy,         x + w - r + dx, y + dy + 1,       color);
            // bottom-left
            ctx.fill(x + r - dx, y + h - dy - 1, x + r,        y + h - dy,         color);
            // bottom-right
            ctx.fill(x + w - r,  y + h - dy - 1, x + w - r + dx, y + h - dy,       color);
        }
    }

    /**
     * Rounded outline with the given thickness — drawn as four edge-strips so it's
     * cheap and doesn't need a stencil. Corners are scanline-quarter-circles, just
     * like {@link #roundedRect} but only the outer ring (between radius and radius-thickness).
     */
    public static void roundedOutline(DrawContext ctx, int x, int y, int w, int h, int radius, int thickness, int color) {
        if (radius <= 0) { outline(ctx, x, y, w, h, thickness, color); return; }
        int r = Math.min(radius, Math.min(w, h) / 2);
        int t = Math.max(1, Math.min(thickness, r));

        // straight edges
        ctx.fill(x + r,         y,                 x + w - r,     y + t,             color); // top
        ctx.fill(x + r,         y + h - t,         x + w - r,     y + h,             color); // bottom
        ctx.fill(x,             y + r,             x + t,         y + h - r,         color); // left
        ctx.fill(x + w - t,     y + r,             x + w,         y + h - r,         color); // right

        // arc rings on the four corners
        for (int dy = 0; dy < r; dy++) {
            int yy = r - dy - 1;
            int outer = (int) Math.round(Math.sqrt(r * r - yy * yy));
            int innerR = Math.max(0, r - t);
            int inner = (yy < innerR) ? (int) Math.round(Math.sqrt((double) innerR * innerR - yy * yy)) : 0;
            // top-left
            ctx.fill(x + r - outer, y + dy,         x + r - inner, y + dy + 1,         color);
            // top-right
            ctx.fill(x + w - r + inner, y + dy,     x + w - r + outer, y + dy + 1,     color);
            // bottom-left
            ctx.fill(x + r - outer, y + h - dy - 1, x + r - inner, y + h - dy,         color);
            // bottom-right
            ctx.fill(x + w - r + inner, y + h - dy - 1, x + w - r + outer, y + h - dy, color);
        }
    }

    // ---------------------------------------------------------------------------
    // Gradients
    // ---------------------------------------------------------------------------

    /**
     * Vertical gradient (top color → bottom color). Uses Mojang's native
     * {@code fillGradient} for a correctly batched single-quad call.
     */
    public static void verticalGradient(DrawContext ctx, int x, int y, int w, int h, int top, int bottom) {
        ctx.fillGradient(x, y, x + w, y + h, top, bottom);
    }

    /**
     * Horizontal gradient. Mojang's native fillGradient is vertical-only on most
     * pipeline versions, so we composite as 1-pixel vertical slices. Size is small
     * for typical UI elements (≤ 300px) so the overdraw is irrelevant.
     */
    public static void horizontalGradient(DrawContext ctx, int x, int y, int w, int h, int left, int right) {
        for (int i = 0; i < w; i++) {
            float t = w <= 1 ? 0 : (float) i / (w - 1);
            int color = ColorUtil.lerp(left, right, t);
            ctx.fill(x + i, y, x + i + 1, y + h, color);
        }
    }

    // ---------------------------------------------------------------------------
    // Glow & shadow
    // ---------------------------------------------------------------------------

    /**
     * Layered glow around a rectangle. Each layer is a slightly-larger rounded rect
     * with reduced alpha — a cheap fake of a Gaussian halo.
     *
     * @param spread   pixels of spread per side (e.g. 6)
     * @param layers   number of falloff steps (e.g. 5)
     * @param color    ARGB; alpha is treated as the *peak* alpha and decays outwards
     */
    public static void glow(DrawContext ctx, int x, int y, int w, int h, int radius,
                            int spread, int layers, int color) {
        int peakAlpha = (color >>> 24) & 0xFF;
        for (int i = layers; i >= 1; i--) {
            float t = (float) i / layers;            // 1.0 at outermost, 1/layers at innermost
            int pad = Math.round(spread * t);
            int alpha = (int) (peakAlpha * (1 - t) * 0.5f);
            if (alpha <= 0) continue;
            int layerColor = ColorUtil.withAlpha(color, alpha);
            roundedRect(ctx, x - pad, y - pad, w + pad * 2, h + pad * 2, radius + pad, layerColor);
        }
    }

    /** Soft drop shadow (downwards offset, multiplicative dark tint). */
    public static void dropShadow(DrawContext ctx, int x, int y, int w, int h, int radius,
                                  int yOffset, int alpha) {
        roundedRect(ctx, x + 2, y + yOffset + 2, w, h, radius, ColorUtil.argb(0, 0, 0, alpha / 4));
        roundedRect(ctx, x + 1, y + yOffset + 1, w, h, radius, ColorUtil.argb(0, 0, 0, alpha / 2));
        roundedRect(ctx, x,     y + yOffset,     w, h, radius, ColorUtil.argb(0, 0, 0, alpha));
    }

    // ---------------------------------------------------------------------------
    // Backgrounds (fake-blur)
    // ---------------------------------------------------------------------------

    /**
     * Cheap "fake blur" backdrop for ClickGUI: a deep-black panel-tinted fill plus
     * sparse horizontal scanlines for visual depth. ~free at any resolution and looks
     * right on integrated GPUs where a true Kawase blur would tank fps.
     */
    public static void dimBackground(DrawContext ctx, int screenW, int screenH) {
        ctx.fill(0, 0, screenW, screenH, Palette.BG_DEEP);
        // light vertical gradient overlay (tighter at top)
        ctx.fillGradient(0, 0, screenW, screenH, 0x40000000, 0x00000000);
        // sparse scanlines
        for (int y = 0; y < screenH; y += 3) {
            ctx.fill(0, y, screenW, y + 1, Palette.BG_SCANLINE);
        }
    }

    // ---------------------------------------------------------------------------
    // Lines
    // ---------------------------------------------------------------------------

    /** 1-pixel-thick horizontal divider. */
    public static void hLine(DrawContext ctx, int x, int y, int w, int color) {
        ctx.fill(x, y, x + w, y + 1, color);
    }

    /** 1-pixel-thick vertical divider. */
    public static void vLine(DrawContext ctx, int x, int y, int h, int color) {
        ctx.fill(x, y, x + 1, y + h, color);
    }
}
