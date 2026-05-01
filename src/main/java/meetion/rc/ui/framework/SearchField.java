package meetion.rc.ui.framework;

import meetion.rc.ui.animation.Animator;
import meetion.rc.ui.animation.Easing;
import meetion.rc.ui.font.Fonts;
import meetion.rc.ui.utils.ColorUtil;
import meetion.rc.ui.utils.Palette;
import meetion.rc.ui.utils.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

/**
 * Lightweight single-line text input in the project's visual language.
 *
 * <p>Backspace/left-right/home/end/typing handled. Focus is implicit: clicking inside
 * sets {@link #focused}, clicking outside clears it. Reports every keystroke to the
 * registered {@link #onChange} consumer so consumers can re-filter their lists in
 * real time without polling.
 */
public class SearchField extends Component {

    private String value = "";
    private String placeholder = "Search…";
    private int maxLength = 64;
    private boolean focused = false;
    private int cursor = 0;

    private final Animator focusGlow = new Animator(0, 200, Easing.EASE_OUT_CUBIC);
    private long blinkStart = System.currentTimeMillis();
    private Consumer<String> onChange = s -> {};

    public SearchField() { this.h = 22; }

    public SearchField placeholder(String s) { this.placeholder = s; return this; }
    public SearchField maxLength(int n)      { this.maxLength = Math.max(1, n); return this; }
    public SearchField onChange(Consumer<String> c) { this.onChange = c == null ? s -> {} : c; return this; }

    public String getValue() { return value; }
    public boolean isFocused() { return focused; }

    public void setValue(String v) {
        this.value = v == null ? "" : v;
        this.cursor = this.value.length();
        onChange.accept(this.value);
    }

    // ---------------------------------------------------------------------------
    // Render
    // ---------------------------------------------------------------------------

    @Override
    public void render(DrawContext ctx, double mouseX, double mouseY, float delta) {
        float fg = focusGlow.floatValue();

        // background
        int bg = ColorUtil.lerp(Palette.BG_DEEP, Palette.BG_HOVER, hover.floatValue() * 0.6f + fg * 0.4f);
        RenderUtil.roundedRect(ctx, x, y, w, h, 4, bg);

        // border (red glow when focused)
        if (fg > 0.05f) {
            int alpha = (int) (180 * fg);
            RenderUtil.roundedOutline(ctx, x, y, w, h, 4, 1, ColorUtil.withAlpha(Palette.RED, alpha));
        } else {
            RenderUtil.roundedOutline(ctx, x, y, w, h, 4, 1, ColorUtil.argb(255, 255, 255, 16));
        }

        // text
        int textY = y + (h - Fonts.lineHeight()) / 2;
        int padding = 8;
        if (value.isEmpty() && !focused) {
            Fonts.draw(ctx, Fonts.INTER, placeholder, x + padding, textY, Palette.TEXT_MUTED);
        } else {
            Fonts.draw(ctx, Fonts.INTER, value, x + padding, textY, Palette.TEXT_PRIMARY);
        }

        // cursor
        if (focused) {
            long t = (System.currentTimeMillis() - blinkStart) % 1000;
            if (t < 500) {
                String prefix = value.substring(0, Math.min(cursor, value.length()));
                int cx = x + padding + Fonts.width(Fonts.INTER, prefix);
                ctx.fill(cx, textY - 1, cx + 1, textY + Fonts.lineHeight(), Palette.TEXT_PRIMARY);
            }
        }
    }

    // ---------------------------------------------------------------------------
    // Events
    // ---------------------------------------------------------------------------

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean inside = isHovered(mouseX, mouseY);
        boolean wasFocused = focused;
        focused = inside;
        focusGlow.setTarget(focused ? 1 : 0);
        if (focused) {
            cursor = value.length();
            blinkStart = System.currentTimeMillis();
        }
        return inside || wasFocused;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!focused) return false;
        switch (keyCode) {
            case GLFW.GLFW_KEY_BACKSPACE -> {
                if (cursor > 0 && !value.isEmpty()) {
                    value = value.substring(0, cursor - 1) + value.substring(cursor);
                    cursor--;
                    onChange.accept(value);
                }
                return true;
            }
            case GLFW.GLFW_KEY_DELETE -> {
                if (cursor < value.length()) {
                    value = value.substring(0, cursor) + value.substring(cursor + 1);
                    onChange.accept(value);
                }
                return true;
            }
            case GLFW.GLFW_KEY_LEFT  -> { cursor = Math.max(0, cursor - 1); return true; }
            case GLFW.GLFW_KEY_RIGHT -> { cursor = Math.min(value.length(), cursor + 1); return true; }
            case GLFW.GLFW_KEY_HOME  -> { cursor = 0; return true; }
            case GLFW.GLFW_KEY_END   -> { cursor = value.length(); return true; }
            case GLFW.GLFW_KEY_ESCAPE -> { focused = false; focusGlow.setTarget(0); return false; }
            default -> {}
        }
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (!focused) return false;
        if (chr < 32 || chr == 127) return false;
        if (value.length() >= maxLength) return true;
        value = value.substring(0, cursor) + chr + value.substring(cursor);
        cursor++;
        onChange.accept(value);
        return true;
    }
}
