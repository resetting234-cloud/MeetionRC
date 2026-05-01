package meetion.rc.ui.framework;

import meetion.rc.core.module.Module;
import meetion.rc.core.setting.Setting;
import meetion.rc.ui.animation.Animator;
import meetion.rc.ui.animation.Easing;
import meetion.rc.ui.font.Fonts;
import meetion.rc.ui.utils.ColorUtil;
import meetion.rc.ui.utils.Palette;
import meetion.rc.ui.utils.RenderUtil;
import net.minecraft.client.gui.DrawContext;

/**
 * The right-hand "Contextual Dock" pane.
 *
 * <p>Hidden until a module is selected in the {@link ModuleListPanel}; on selection
 * the panel slides out from width 0 → {@link #FULL_WIDTH} via an
 * {@link Easing#EASE_OUT_EXPO} animator, then renders a header (module name + description)
 * followed by a stack of widgets generated from {@link Module#getSettings()} via
 * {@link ComponentRegistry#create}.
 *
 * <p>If a setting type has no registered factory the panel renders a muted "<setting>:
 * <value>" placeholder line instead of crashing — the registry can be extended later
 * without breaking layout.
 */
public class SettingsPanel extends Panel {

    public static final int FULL_WIDTH = 300;
    private static final int HEADER_H  = 56;
    private static final int ROW_PAD_X = 14;
    private static final int ROW_GAP   = 10;

    private final Animator slideOut = new Animator(0, 320, Easing.EASE_OUT_EXPO);
    private Module current = null;
    private int anchorX = 0;   // x of the left panel's right edge — we slide out from here
    private int anchorY = 0;
    private int anchorH = 0;

    public SettingsPanel() {
        this.clip = true;
    }

    /** Pin the panel against another panel's right edge. */
    public SettingsPanel anchor(int rightEdgeX, int y, int h) {
        this.anchorX = rightEdgeX;
        this.anchorY = y;
        this.anchorH = h;
        applyBounds();
        return this;
    }

    public SettingsPanel show(Module m) {
        this.current = m;
        slideOut.setTarget(m == null ? 0 : 1);
        rebuild();
        return this;
    }

    public Module getCurrent() { return current; }

    public boolean isCollapsed() {
        return current == null && slideOut.getValue() < 0.01;
    }

    private void applyBounds() {
        int width = (int) (FULL_WIDTH * slideOut.getValue());
        super.setBounds(anchorX, anchorY, width, anchorH);
    }

    private void rebuild() {
        clearChildren();
        if (current == null) {
            setContentHeight(0);
            return;
        }
        int rowY = anchorY + HEADER_H;
        int rowW = FULL_WIDTH - ROW_PAD_X * 2;
        for (Setting<?> s : current.getSettings()) {
            if (!s.isVisible()) continue;
            Component comp = ComponentRegistry.create(s);
            if (comp == null) continue;
            int rowH = comp.getHeight() > 0 ? comp.getHeight() : 22;
            comp.setBounds(anchorX + ROW_PAD_X, rowY, rowW, rowH);
            addChild(comp);
            rowY += rowH + ROW_GAP;
        }
        setContentHeight(rowY - anchorY);
    }

    // ---------------------------------------------------------------------------
    // Render
    // ---------------------------------------------------------------------------

    @Override
    public void update(double mouseX, double mouseY) {
        applyBounds();   // keep size in sync with the slide animator each frame
        super.update(mouseX, mouseY);
    }

    @Override
    public void render(DrawContext ctx, double mouseX, double mouseY, float delta) {
        applyBounds();
        if (w <= 1) return;

        // background panel + thin red dividing edge on the left
        RenderUtil.roundedRect(ctx, x, y, w, h, 0, Palette.BG_PANEL_LIGHT);
        ctx.fill(x, y, x + 1, y + h, ColorUtil.withAlpha(Palette.RED, 96));

        // header (module name + description), only when sufficiently revealed
        float reveal = slideOut.floatValue();
        if (current != null && reveal > 0.1f) {
            int headerY = y + 14;
            int textAlpha = (int) (255 * Math.min(1, (reveal - 0.1f) / 0.5f));
            int titleColor = ColorUtil.withAlpha(Palette.TEXT_PRIMARY, textAlpha);
            int descColor  = ColorUtil.withAlpha(Palette.TEXT_SECONDARY, textAlpha);

            Fonts.draw(ctx, Fonts.INTER_BOLD, current.getName(), x + ROW_PAD_X, headerY, titleColor);
            String desc = current.getDescription();
            if (desc != null && !desc.isEmpty()) {
                Fonts.draw(ctx, Fonts.INTER, desc, x + ROW_PAD_X, headerY + 14, descColor);
            }
            // divider
            ctx.fill(x + ROW_PAD_X, y + HEADER_H - 6, x + w - ROW_PAD_X, y + HEADER_H - 5,
                    ColorUtil.argb(255, 255, 255, 16));
        }

        // children (settings widgets)
        super.render(ctx, mouseX, mouseY, delta);
    }
}
