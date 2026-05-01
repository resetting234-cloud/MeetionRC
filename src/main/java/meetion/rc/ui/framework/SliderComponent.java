package meetion.rc.ui.framework;

import meetion.rc.core.setting.impl.DoubleSetting;
import meetion.rc.ui.animation.Animator;
import meetion.rc.ui.animation.Easing;
import meetion.rc.ui.font.Fonts;
import meetion.rc.ui.utils.ColorUtil;
import meetion.rc.ui.utils.Palette;
import meetion.rc.ui.utils.RenderUtil;
import net.minecraft.client.gui.DrawContext;

/**
 * Horizontal slider bound to a {@link DoubleSetting}.
 *
 * <p>Interaction: click anywhere on the bar to jump there, drag to scrub. The visual
 * fill is interpolated through an {@link Animator} (150ms ease-out-cubic) so the red
 * bar smoothly chases the underlying value rather than teleporting under the cursor.
 *
 * <p>Layout: the component reserves {@link #h} for the label row + bar; default is
 * {@code 22px}. Caller should set width to fill the SettingsPanel.
 */
public class SliderComponent extends Component {

    private final DoubleSetting setting;
    private final Animator fill;
    private boolean dragging = false;

    private static final int LABEL_HEIGHT = 11;
    private static final int BAR_HEIGHT   = 4;
    private static final int HANDLE_SIZE  = 10;

    public SliderComponent(DoubleSetting setting) {
        this.setting = setting;
        this.fill = new Animator(setting.normalised(), 150, Easing.EASE_OUT_CUBIC);
        setting.onChange(v -> fill.setTarget(setting.normalised()));
        this.h = 22;
    }

    public DoubleSetting getSetting() { return setting; }

    // ---------------------------------------------------------------------------
    // Render
    // ---------------------------------------------------------------------------

    @Override
    public void render(DrawContext ctx, double mouseX, double mouseY, float delta) {
        // label row
        int labelY = y;
        Fonts.draw(ctx, Fonts.INTER, setting.getName(), x, labelY, Palette.TEXT_PRIMARY);
        String fmt = setting.format();
        int fmtW = Fonts.width(Fonts.INTER, fmt);
        Fonts.draw(ctx, Fonts.INTER, fmt, x + w - fmtW, labelY, Palette.TEXT_SECONDARY);

        // bar
        int barY = y + LABEL_HEIGHT + 4;
        int barCornerR = BAR_HEIGHT / 2;
        RenderUtil.roundedRect(ctx, x, barY, w, BAR_HEIGHT, barCornerR, Palette.TOGGLE_OFF_BG);

        // animated fill
        float t = fill.floatValue();
        int fillW = (int) (w * t);
        if (fillW > 0) {
            RenderUtil.roundedRect(ctx, x, barY, fillW, BAR_HEIGHT, barCornerR, Palette.RED);
            // soft glow under the fill on hover
            float hov = hover.floatValue();
            if (hov > 0.05f) {
                int alpha = (int) (40 * hov);
                RenderUtil.glow(ctx, x, barY, fillW, BAR_HEIGHT, barCornerR, 5, 4,
                        ColorUtil.withAlpha(Palette.RED, alpha));
            }
        }

        // handle (white dot) on top of the fill end
        int handleX = x + Math.max(0, Math.min(w - HANDLE_SIZE, fillW - HANDLE_SIZE / 2));
        int handleY = barY + BAR_HEIGHT / 2 - HANDLE_SIZE / 2;
        RenderUtil.roundedRect(ctx, handleX, handleY, HANDLE_SIZE, HANDLE_SIZE, HANDLE_SIZE / 2, Palette.TEXT_PRIMARY);
    }

    // ---------------------------------------------------------------------------
    // Drag interaction
    // ---------------------------------------------------------------------------

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || !isHovered(mouseX, mouseY)) return false;
        dragging = true;
        updateFromMouse(mouseX);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (dragging) {
            dragging = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (!dragging) return false;
        updateFromMouse(mouseX);
        return true;
    }

    private void updateFromMouse(double mouseX) {
        if (w <= 0) return;
        double t = (mouseX - x) / (double) w;
        setting.setFromNormalised(t);
        // The setting's onChange listener already updates fill; this just gives a
        // snappier first frame in case the listener registration was bypassed.
        fill.setTarget(setting.normalised());
    }
}
