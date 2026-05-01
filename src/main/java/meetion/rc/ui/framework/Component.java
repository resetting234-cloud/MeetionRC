package meetion.rc.ui.framework;

import meetion.rc.ui.animation.Animator;
import meetion.rc.ui.animation.Easing;
import net.minecraft.client.gui.DrawContext;

/**
 * Base class for every widget in the Contextual Dock UI.
 *
 * <p>A component has:
 * <ul>
 *   <li>An axis-aligned rectangle ({@code x, y, w, h}) given to it by its parent panel.</li>
 *   <li>A built-in {@link #hover hover} {@link Animator} that smoothly tracks 0/1 based
 *       on mouse position. Subclasses just sample {@code hover.floatValue()} when they
 *       want to lerp colours or sizes — no manual hover-state plumbing required.</li>
 *   <li>A full event chain ({@link #mouseClicked}, {@link #mouseReleased},
 *       {@link #mouseDragged}, {@link #mouseScrolled}, {@link #keyPressed},
 *       {@link #charTyped}). Methods return {@code true} if the event is consumed.</li>
 * </ul>
 *
 * <p>Components do not own children — that's {@link Panel}'s job. Composition over
 * inheritance: a {@code SliderComponent} is a leaf, the panel holding it is the only
 * thing that knows where to put the next slider.
 */
public abstract class Component {

    protected int x;
    protected int y;
    protected int w;
    protected int h;
    protected boolean visible = true;
    protected boolean enabled = true;
    protected Component parent;

    /** Hover animation: 0 when mouse is outside, 1 when inside. 180ms ease-out-quad. */
    protected final Animator hover = new Animator(0, 180, Easing.EASE_IN_OUT_QUAD);

    private boolean wasHovered = false;

    // ---------------------------------------------------------------------------
    // Layout
    // ---------------------------------------------------------------------------

    public Component setBounds(int x, int y, int w, int h) {
        this.x = x; this.y = y; this.w = w; this.h = h;
        return this;
    }

    public Component setPosition(int x, int y) {
        this.x = x; this.y = y;
        return this;
    }

    public Component setSize(int w, int h) {
        this.w = w; this.h = h;
        return this;
    }

    public Component setVisible(boolean v)  { this.visible  = v; return this; }
    public Component setEnabled(boolean en) { this.enabled  = en; return this; }
    public void setParent(Component p)      { this.parent = p; }

    public int  getX()       { return x; }
    public int  getY()       { return y; }
    public int  getWidth()   { return w; }
    public int  getHeight()  { return h; }
    public boolean isVisible() { return visible; }
    public boolean isEnabled() { return enabled; }
    public Component getParent() { return parent; }

    // ---------------------------------------------------------------------------
    // Hover & rendering
    // ---------------------------------------------------------------------------

    public boolean isHovered(double mouseX, double mouseY) {
        return visible && enabled
                && mouseX >= x && mouseX < x + w
                && mouseY >= y && mouseY < y + h;
    }

    /**
     * Per-frame state tick. {@link Panel} calls this on every child before {@link #render}
     * so subclasses can rely on {@code hover.getValue()} being up-to-date when they paint.
     */
    public void update(double mouseX, double mouseY) {
        if (!visible) return;
        boolean h = isHovered(mouseX, mouseY);
        if (h != wasHovered) {
            hover.setTarget(h ? 1 : 0);
            wasHovered = h;
        }
    }

    public abstract void render(DrawContext ctx, double mouseX, double mouseY, float delta);

    // ---------------------------------------------------------------------------
    // Event chain — override only what you need; defaults all return false.
    // ---------------------------------------------------------------------------

    public boolean mouseClicked(double mouseX, double mouseY, int button) { return false; }
    public boolean mouseReleased(double mouseX, double mouseY, int button) { return false; }
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) { return false; }
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) { return false; }
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) { return false; }
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) { return false; }
    public boolean charTyped(char chr, int modifiers) { return false; }
}
