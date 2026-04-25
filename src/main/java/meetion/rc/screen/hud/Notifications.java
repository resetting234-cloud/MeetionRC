package meetion.rc.screen.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayDeque;
import java.util.Deque;

public final class Notifications {

    private static final Deque<Notification> queue = new ArrayDeque<>();

    private Notifications() {}

    public static void push(String text) { push(text, 2500); }

    public static void push(String text, int durationMs) {
        queue.offer(new Notification(text, System.currentTimeMillis() + durationMs));
        if (queue.size() > 6) queue.pollFirst();
    }

    public static void render(DrawContext ctx) {
        long now = System.currentTimeMillis();
        queue.removeIf(n -> n.expires < now);
        MinecraftClient mc = MinecraftClient.getInstance();
        int y = ctx.getScaledWindowHeight() - 16;
        for (Notification n : queue) {
            int w = mc.textRenderer.getWidth(n.text) + 8;
            int x = ctx.getScaledWindowWidth() - w - 4;
            ctx.fill(x, y - 4, x + w, y + mc.textRenderer.fontHeight + 2, 0x90000000);
            ctx.drawTextWithShadow(mc.textRenderer, n.text, x + 4, y - 1, 0xFFFFFFFF);
            y -= mc.textRenderer.fontHeight + 8;
        }
    }

    private record Notification(String text, long expires) {}
}
