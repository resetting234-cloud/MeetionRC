package meetion.rc.ui.hud;

import meetion.rc.ui.font.Fonts;
import meetion.rc.ui.utils.ColorUtil;
import meetion.rc.ui.utils.Palette;
import meetion.rc.ui.utils.RenderUtil;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Stacked toast notifications under the ArrayList in the top-right corner.
 *
 * <p>Toasts slide in from the right via each {@link Notification}'s slide animator,
 * display a coloured strip on the left (red for module disable / module event,
 * green for enable, blue for info), and a thin progress bar at the bottom that
 * shrinks 100% → 0% over the toast's lifetime.
 */
public final class NotificationManager {

    private NotificationManager() {}

    private static final int MAX_QUEUE = 6;
    private static final int CARD_W    = 200;
    private static final int CARD_H    = 32;
    private static final int GAP       = 4;

    private static final List<Notification> queue = new ArrayList<>();

    // ---------------------------------------------------------------------------
    // Public API
    // ---------------------------------------------------------------------------

    public static void post(String title, String body, Notification.Type type, long durationMs) {
        synchronized (queue) {
            queue.add(new Notification(title, body, type, durationMs));
            while (queue.size() > MAX_QUEUE) queue.remove(0);
        }
    }

    public static void post(String title, String body, Notification.Type type) {
        post(title, body, type, 2500);
    }

    public static void post(String title, String body) {
        post(title, body, Notification.Type.INFO, 2500);
    }

    /** Compatibility shim for the older {@code Notifications.push(text)} API. */
    public static void push(String text) {
        Notification.Type type = Notification.Type.INFO;
        if (text != null) {
            // strip legacy "§a" / "§c" colour codes used by Module.setEnabled
            String stripped = text.replaceAll("§[0-9a-fA-F]", "").trim();
            if (text.contains("§a") || stripped.startsWith("+")) type = Notification.Type.SUCCESS;
            else if (text.contains("§c") || stripped.startsWith("-")) type = Notification.Type.ERROR;
            text = stripped;
        }
        post(text == null ? "" : text, "", type, 2200);
    }

    // ---------------------------------------------------------------------------
    // Render
    // ---------------------------------------------------------------------------

    public static void render(DrawContext ctx) {
        synchronized (queue) {
            for (Iterator<Notification> it = queue.iterator(); it.hasNext(); ) {
                Notification n = it.next();
                n.tick();
                if (n.isFinished()) it.remove();
            }
        }

        int screenW = ctx.getScaledWindowWidth();
        int y = computeStartY(ctx);

        synchronized (queue) {
            for (Notification n : queue) {
                float slide = n.slide.floatValue();
                int travel = CARD_W + 12;
                int x = screenW - 6 - (int) (CARD_W * slide) - (int) ((1 - slide) * 0);
                // slide animates X by 'travel' from off-screen-right
                x = screenW - 6 - CARD_W + (int) ((1 - slide) * travel);

                int alpha = (int) (255 * slide);
                drawCard(ctx, n, x, y, alpha);
                y += CARD_H + GAP;
            }
        }
    }

    private static int computeStartY(DrawContext ctx) {
        // sit underneath the ArrayList — give it ~120px of room for typical 6-row stack
        return 120;
    }

    private static void drawCard(DrawContext ctx, Notification n, int x, int y, int alpha) {
        // panel
        RenderUtil.roundedRect(ctx, x, y, CARD_W, CARD_H, 4,
                ColorUtil.withAlpha(Palette.BG_PANEL, alpha));

        // colored side strip on the left
        int stripColor = stripFor(n.getType());
        ctx.fill(x, y, x + 2, y + CARD_H, ColorUtil.withAlpha(stripColor, alpha));

        // text
        int titleColor = ColorUtil.withAlpha(Palette.TEXT_PRIMARY, alpha);
        int bodyColor  = ColorUtil.withAlpha(Palette.TEXT_SECONDARY, alpha);
        String title = n.getTitle();
        if (!title.isEmpty()) {
            Fonts.draw(ctx, Fonts.INTER_BOLD, title, x + 8, y + 5, titleColor);
        }
        if (!n.getBody().isEmpty()) {
            Fonts.draw(ctx, Fonts.INTER, n.getBody(), x + 8, y + 16, bodyColor);
        }

        // bottom timer bar
        int barH = 2;
        int barY = y + CARD_H - barH;
        ctx.fill(x, barY, x + CARD_W, barY + barH,
                ColorUtil.argb(0, 0, 0, Math.min(120, alpha)));
        int filled = (int) ((CARD_W - 4) * n.timeRemaining());
        ctx.fill(x + 2, barY, x + 2 + filled, barY + barH,
                ColorUtil.withAlpha(stripColor, alpha));
    }

    private static int stripFor(Notification.Type type) {
        return switch (type) {
            case SUCCESS -> 0xFF44E08A;
            case ERROR   -> Palette.RED;
            case WARN    -> 0xFFFFB030;
            case INFO    -> 0xFF6BB6FF;
        };
    }
}
