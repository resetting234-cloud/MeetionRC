package meetion.rc.ui.framework;

import meetion.rc.core.module.Category;
import meetion.rc.ui.animation.Animator;
import meetion.rc.ui.animation.Easing;
import meetion.rc.ui.font.Fonts;
import meetion.rc.ui.utils.ColorUtil;
import meetion.rc.ui.utils.Palette;
import meetion.rc.ui.utils.RenderUtil;
import net.minecraft.client.gui.DrawContext;

import java.util.function.Consumer;

/**
 * Horizontal tab pill for switching the {@link Category} filter in {@code ClickGuiScreen}.
 * Selected state shows a thin red underline + slightly lighter text. Hover lerps a subtle
 * white tint on the background.
 */
public class CategoryTab extends Component {

    private final Category category;
    private boolean selected = false;
    private final Animator selectGlow = new Animator(0, 200, Easing.EASE_OUT_CUBIC);
    private Consumer<Category> onSelect = c -> {};

    public CategoryTab(Category category) {
        this.category = category;
        this.h = 26;
    }

    public CategoryTab onSelect(Consumer<Category> handler) {
        this.onSelect = handler == null ? c -> {} : handler;
        return this;
    }

    public Category getCategory() { return category; }
    public boolean isSelected() { return selected; }

    public CategoryTab setSelected(boolean s) {
        if (this.selected == s) return this;
        this.selected = s;
        this.selectGlow.setTarget(s ? 1 : 0);
        return this;
    }

    @Override
    public void render(DrawContext ctx, double mouseX, double mouseY, float delta) {
        float hov = hover.floatValue();
        float sel = selectGlow.floatValue();

        // background tint
        int bgAlpha = (int) (10 * hov + 18 * sel);
        if (bgAlpha > 0) {
            RenderUtil.roundedRect(ctx, x, y, w, h, 4, ColorUtil.argb(255, 255, 255, bgAlpha));
        }

        // text — glyph + name (centered horizontally)
        String label = category.getGlyph() + "  " + category.getName();
        int color = ColorUtil.lerp(Palette.TEXT_SECONDARY, Palette.TEXT_PRIMARY, Math.max(hov * 0.7f, sel));
        int textW = Fonts.width(Fonts.INTER, label);
        int tx = x + (w - textW) / 2;
        int ty = y + (h - Fonts.lineHeight()) / 2;
        Fonts.draw(ctx, Fonts.INTER, label, tx, ty, color);

        // red underline when selected
        if (sel > 0.05f) {
            int barW = (int) ((w - 16) * sel);
            int barX = x + (w - barW) / 2;
            ctx.fill(barX, y + h - 2, barX + barW, y + h - 1,
                    ColorUtil.withAlpha(Palette.RED, (int) (255 * sel)));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || !isHovered(mouseX, mouseY)) return false;
        onSelect.accept(category);
        return true;
    }
}
