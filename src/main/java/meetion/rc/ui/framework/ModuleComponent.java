package meetion.rc.ui.framework;

import meetion.rc.core.module.Module;
import meetion.rc.ui.animation.Animator;
import meetion.rc.ui.animation.Easing;
import meetion.rc.ui.font.Fonts;
import meetion.rc.ui.utils.ColorUtil;
import meetion.rc.ui.utils.Palette;
import meetion.rc.ui.utils.RenderUtil;
import net.minecraft.client.gui.DrawContext;

import java.util.function.Consumer;

/**
 * One row in the left panel of ClickGUI. Composes:
 * <ul>
 *   <li>category glyph (left)</li>
 *   <li>module name (center, white when selected/enabled, secondary grey otherwise)</li>
 *   <li>{@link ToggleComponent} bound to {@link Module#toggle()} (right)</li>
 * </ul>
 *
 * <p>The whole row hover-fades a subtle white tint (rgba 1,1,1,0.03) so the user gets
 * a clean visual cue without obscuring the dark base. A click anywhere outside the
 * toggle pill fires the {@link #onSelect} callback so the parent ClickGUI can swap
 * the right SettingsPanel to this module's settings.
 */
public class ModuleComponent extends Component {

    private final Module module;
    private final ToggleComponent toggle;
    private final Animator selectedGlow;
    private boolean selected = false;

    private Consumer<Module> onSelect = m -> {};

    private static final int ROW_HEIGHT = 22;

    public ModuleComponent(Module module) {
        this.module = module;
        this.toggle = ToggleComponent.ofModule(module);
        this.selectedGlow = new Animator(0, 200, Easing.EASE_OUT_CUBIC);
        this.h = ROW_HEIGHT;
    }

    public Module getModule() { return module; }

    public ModuleComponent onSelect(Consumer<Module> handler) {
        this.onSelect = handler == null ? m -> {} : handler;
        return this;
    }

    public ModuleComponent setSelected(boolean s) {
        if (this.selected == s) return this;
        this.selected = s;
        this.selectedGlow.setTarget(s ? 1 : 0);
        return this;
    }

    public boolean isSelected() { return selected; }

    @Override
    public Component setBounds(int x, int y, int w, int h) {
        super.setBounds(x, y, w, h);
        // place the toggle on the right edge, vertically centered
        int tw = 28, th = 14;
        toggle.setBounds(x + w - tw - 8, y + (h - th) / 2, tw, th);
        return this;
    }

    // ---------------------------------------------------------------------------
    // Render
    // ---------------------------------------------------------------------------

    @Override
    public void update(double mouseX, double mouseY) {
        super.update(mouseX, mouseY);
        toggle.update(mouseX, mouseY);
    }

    @Override
    public void render(DrawContext ctx, double mouseX, double mouseY, float delta) {
        float hov = hover.floatValue();
        float sel = selectedGlow.floatValue();

        // hover/selected background tint (additive whiteness)
        int bgAlpha = (int) (8 * hov + 16 * sel);
        if (bgAlpha > 0) {
            RenderUtil.rect(ctx, x, y, w, h, ColorUtil.argb(255, 255, 255, bgAlpha));
        }

        // left red accent bar when selected
        if (sel > 0.05f) {
            int barAlpha = (int) (255 * sel);
            RenderUtil.rect(ctx, x, y + 3, 2, h - 6,
                    ColorUtil.withAlpha(Palette.RED, barAlpha));
        }

        // category glyph (small, secondary)
        int glyphX = x + 10;
        Fonts.draw(ctx, Fonts.INTER, module.getCategory().getGlyph(), glyphX, y + 6,
                ColorUtil.lerp(Palette.TEXT_MUTED, Palette.RED, sel * 0.7f));

        // module name
        int nameX = glyphX + 14;
        int nameColor = module.isEnabled()
                ? Palette.TEXT_PRIMARY
                : ColorUtil.lerp(Palette.TEXT_SECONDARY, Palette.TEXT_PRIMARY, hov);
        Fonts.draw(ctx, Fonts.INTER, module.getName(), nameX, y + 6, nameColor);

        // toggle pill
        toggle.render(ctx, mouseX, mouseY, delta);
    }

    // ---------------------------------------------------------------------------
    // Events
    // ---------------------------------------------------------------------------

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isHovered(mouseX, mouseY)) return false;
        // toggle pill takes precedence
        if (toggle.mouseClicked(mouseX, mouseY, button)) return true;

        if (button == 0) {
            onSelect.accept(module);
            return true;
        }
        return false;
    }
}
