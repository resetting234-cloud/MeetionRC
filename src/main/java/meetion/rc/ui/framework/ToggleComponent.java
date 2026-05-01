package meetion.rc.ui.framework;

import meetion.rc.core.module.Module;
import meetion.rc.core.setting.impl.BooleanSetting;
import meetion.rc.ui.animation.Animator;
import meetion.rc.ui.animation.Easing;
import meetion.rc.ui.utils.ColorUtil;
import meetion.rc.ui.utils.Palette;
import meetion.rc.ui.utils.RenderUtil;
import net.minecraft.client.gui.DrawContext;

import java.util.function.BooleanSupplier;

/**
 * Rectangular pill-shaped toggle ("OFF" grey → "ON" red with a sliding white dot).
 *
 * <p>Source-of-truth agnostic: holds a {@code getter} and an {@code onToggle} {@link Runnable}
 * so the same component can drive a {@link BooleanSetting} or a {@link Module}'s enabled
 * flag with no special-casing. Use the {@link #ofSetting} / {@link #ofModule} factories.
 *
 * <p>The white dot's X position is animated via {@link Animator}, which means changing
 * the underlying value externally (e.g. via the keybind manager) still produces a
 * smooth visual transition rather than a teleport.
 */
public class ToggleComponent extends Component {

    private final BooleanSupplier getter;
    private final Runnable onToggle;
    private final Animator dot;

    public ToggleComponent(BooleanSupplier getter, Runnable onToggle) {
        this.getter = getter;
        this.onToggle = onToggle;
        this.dot = new Animator(getter.getAsBoolean() ? 1 : 0, 180, Easing.EASE_OUT_CUBIC);
        // sensible default size for ClickGUI; caller can override
        this.w = 28;
        this.h = 14;
    }

    /** Bind to a {@link BooleanSetting}. The component subscribes to setting changes
     *  so external mutations animate naturally. */
    public static ToggleComponent ofSetting(BooleanSetting s) {
        ToggleComponent tc = new ToggleComponent(s::getValue, s::toggle);
        s.onChange(v -> tc.dot.setTarget(v ? 1 : 0));
        return tc;
    }

    /** Bind to a {@link Module}'s enable state. */
    public static ToggleComponent ofModule(Module m) {
        return new ToggleComponent(m::isEnabled, m::toggle);
    }

    // ---------------------------------------------------------------------------
    // Render
    // ---------------------------------------------------------------------------

    @Override
    public void render(DrawContext ctx, double mouseX, double mouseY, float delta) {
        // keep animator in sync (covers external value changes when no listener exists, e.g. ofModule)
        dot.setTarget(getter.getAsBoolean() ? 1 : 0);

        float t = dot.floatValue();
        int radius = h / 2;

        // background pill: lerp dim grey → red
        int bg = ColorUtil.lerp(Palette.TOGGLE_OFF_BG, Palette.TOGGLE_ON_BG, t);
        RenderUtil.roundedRect(ctx, x, y, w, h, radius, bg);

        // optional red glow when on
        if (t > 0.05f) {
            int glowAlpha = (int) (60 * t);
            RenderUtil.glow(ctx, x, y, w, h, radius, 4, 4, ColorUtil.withAlpha(Palette.RED, glowAlpha));
        }

        // sliding dot
        int padding = 2;
        int dotSize = h - padding * 2;
        int travel  = w - dotSize - padding * 2;
        int dotX    = x + padding + (int) (travel * t);
        int dotY    = y + padding;
        int dotColor = ColorUtil.lerp(Palette.TOGGLE_OFF_DOT, Palette.TOGGLE_ON_DOT, t);
        RenderUtil.roundedRect(ctx, dotX, dotY, dotSize, dotSize, dotSize / 2, dotColor);
    }

    // ---------------------------------------------------------------------------
    // Events
    // ---------------------------------------------------------------------------

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || !isHovered(mouseX, mouseY)) return false;
        onToggle.run();
        // Animator setTarget happens on next render() call; trigger early for snappier feedback
        dot.setTarget(getter.getAsBoolean() ? 1 : 0);
        return true;
    }
}
