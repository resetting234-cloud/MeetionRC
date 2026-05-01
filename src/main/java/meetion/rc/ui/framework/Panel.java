package meetion.rc.ui.framework;

import meetion.rc.ui.animation.Animator;
import meetion.rc.ui.animation.Easing;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Component container with optional vertical scrolling.
 *
 * <p>Panel does not lay its children out itself — it just holds them in insertion order
 * and routes events. Concrete UIs ({@code ModuleListPanel}, {@code SettingsPanel}) are
 * responsible for assigning bounds to children when they add them.
 *
 * <p>Scrolling is content-driven: set {@link #setContentHeight} after children are
 * arranged and the panel will clamp scroll between 0 and {@code max(0, contentH - h)}.
 * A small {@link Animator} smooths wheel jumps so the list eases into place.
 */
public class Panel extends Component {

    protected final List<Component> children = new ArrayList<>();
    protected boolean clip = true;
    protected int contentHeight = 0;
    protected boolean scrollable = false;
    protected final Animator scroll = new Animator(0, 180, Easing.EASE_OUT_CUBIC);

    public Panel addChild(Component c) {
        c.setParent(this);
        children.add(c);
        return this;
    }

    public Panel removeChild(Component c) {
        children.remove(c);
        c.setParent(null);
        return this;
    }

    public Panel clearChildren() {
        for (Iterator<Component> it = children.iterator(); it.hasNext(); ) {
            it.next().setParent(null);
            it.remove();
        }
        return this;
    }

    public List<Component> getChildren() { return children; }

    public Panel setContentHeight(int h) {
        this.contentHeight = h;
        this.scrollable = h > this.h;
        clampScroll();
        return this;
    }

    public Panel setClipToBounds(boolean clip) { this.clip = clip; return this; }

    public int getScroll() { return (int) scroll.getValue(); }

    private void clampScroll() {
        double max = Math.max(0, contentHeight - h);
        if (scroll.getTarget() > max) scroll.setTarget(max);
        if (scroll.getTarget() < 0)   scroll.setTarget(0);
    }

    // ---------------------------------------------------------------------------
    // Render & dispatch
    // ---------------------------------------------------------------------------

    @Override
    public void update(double mouseX, double mouseY) {
        super.update(mouseX, mouseY);
        double adjY = mouseY + getScroll();
        for (Component c : children) c.update(mouseX, adjY);
    }

    @Override
    public void render(DrawContext ctx, double mouseX, double mouseY, float delta) {
        if (!visible) return;
        if (clip) ctx.enableScissor(x, y, x + w, y + h);
        int s = getScroll();
        ctx.getMatrices().pushMatrix();
        ctx.getMatrices().translate(0, -s);
        double adjY = mouseY + s;
        for (Component c : children) {
            if (c.isVisible()) c.render(ctx, mouseX, adjY, delta);
        }
        ctx.getMatrices().popMatrix();
        if (clip) ctx.disableScissor();
    }

    // ---------------------------------------------------------------------------
    // Events
    // ---------------------------------------------------------------------------

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (!isHovered(mx, my)) return false;
        double adjY = my + getScroll();
        // iterate front-to-back so children added later appear "on top"
        for (int i = children.size() - 1; i >= 0; i--) {
            Component c = children.get(i);
            if (c.isVisible() && c.isEnabled() && c.mouseClicked(mx, adjY, button)) return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        double adjY = my + getScroll();
        for (Component c : children) {
            if (c.mouseReleased(mx, adjY, button)) return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        double adjY = my + getScroll();
        for (Component c : children) {
            if (c.mouseDragged(mx, adjY, button, dx, dy)) return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double horizontal, double vertical) {
        if (!isHovered(mx, my)) return false;
        if (scrollable) {
            double max = Math.max(0, contentHeight - h);
            double target = Math.max(0, Math.min(max, scroll.getTarget() - vertical * 24));
            scroll.setTarget(target);
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (Component c : children) {
            if (c.keyPressed(keyCode, scanCode, modifiers)) return true;
        }
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        for (Component c : children) {
            if (c.charTyped(chr, modifiers)) return true;
        }
        return false;
    }
}
