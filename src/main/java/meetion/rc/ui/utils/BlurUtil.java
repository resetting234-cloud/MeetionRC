package meetion.rc.ui.utils;

import net.minecraft.client.gui.DrawContext;

/**
 * Background blur for ClickGUI. Two modes:
 *
 * <ol>
 *   <li>{@link #applyFake} — overlays a deep-black tint + scanlines via {@link RenderUtil#dimBackground}.
 *       Zero GPU cost, looks "matte cyberpunk", recommended on integrated GPUs.</li>
 *   <li>{@link #applyReal} — invokes Mojang's native gui blur (Kawase under the hood
 *       in 1.21+). Heavier, but matches the "true frosted glass" look in the spec.</li>
 * </ol>
 *
 * <h3>"Can only blur once per frame"</h3>
 * Mojang's {@code GuiRenderState.applyBlur} throws if called twice in the same frame,
 * which is what the older ClickGUI hit. We guard with {@link #blurAppliedThisFrame};
 * call {@link #beginFrame()} once at the top of each Render2D pass to reset the flag.
 */
public final class BlurUtil {

    private BlurUtil() {}

    private static boolean blurAppliedThisFrame = false;
    private static long lastApplyNanos = 0;
    private static final long FRAME_GAP_NANOS = 5_000_000L; // 5ms — anything older is a different frame

    /** Reset the per-frame guard. Call once at the top of each Render2D pass. */
    public static void beginFrame() {
        blurAppliedThisFrame = false;
    }

    private static boolean newFrame() {
        long now = System.nanoTime();
        if (now - lastApplyNanos > FRAME_GAP_NANOS) {
            blurAppliedThisFrame = false;
            return true;
        }
        return false;
    }

    /**
     * Apply the configured blur. Picks fake vs real based on {@code useReal}.
     *
     * @param useReal   if {@code true}, attempt the native Kawase blur (no-op if it
     *                  was already applied this frame).
     * @param intensity radius of the real blur in pixels (only used when {@code useReal}).
     */
    public static void apply(DrawContext ctx, boolean useReal, float intensity, int screenW, int screenH) {
        if (useReal) {
            applyReal(ctx, intensity, screenW, screenH);
        } else {
            applyFake(ctx, screenW, screenH);
        }
    }

    /** Cheap matte tint + scanlines. Always safe to call. */
    public static void applyFake(DrawContext ctx, int screenW, int screenH) {
        RenderUtil.dimBackground(ctx, screenW, screenH);
    }

    /**
     * Native vanilla blur via {@code DrawContext.applyBlur}. If it's already been
     * applied this frame we silently fall back to {@link #applyFake} so the screen
     * still gets visually separated from the scene.
     */
    public static void applyReal(DrawContext ctx, float intensity, int screenW, int screenH) {
        newFrame();
        if (blurAppliedThisFrame) {
            applyFake(ctx, screenW, screenH);
            return;
        }
        try {
            // 1.21.x: parameterless DrawContext.applyBlur(); the underlying post-effect
            // controls the kernel radius. The intensity argument is reserved for future
            // use when we ship our own Kawase shader.
            ctx.applyBlur();
            blurAppliedThisFrame = true;
            lastApplyNanos = System.nanoTime();
            // overlay a subtle dark tint so the blurred world doesn't drown out our panels
            ctx.fill(0, 0, screenW, screenH, ColorUtil.argb(0, 0, 0, 0.45f));
        } catch (IllegalStateException once) {
            // some other Screen / mod already blurred this frame → fall back gracefully
            applyFake(ctx, screenW, screenH);
        }
    }
}
