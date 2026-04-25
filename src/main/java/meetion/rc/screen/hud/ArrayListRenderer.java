package meetion.rc.screen.hud;

import meetion.rc.MeetionRC;
import meetion.rc.core.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.awt.Color;
import java.util.Comparator;
import java.util.List;

public final class ArrayListRenderer {

    private ArrayListRenderer() {}

    public static void render(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (MeetionRC.getInstance() == null) return;
        List<Module> enabled = MeetionRC.getInstance().getModuleManager().getModules().stream()
                .filter(Module::isEnabled)
                .sorted(Comparator.comparingInt((Module m) -> mc.textRenderer.getWidth(m.getName())).reversed())
                .toList();
        if (enabled.isEmpty()) return;

        int y = 4;
        int screenW = ctx.getScaledWindowWidth();
        int padX = 4;
        int rowH = mc.textRenderer.fontHeight + 3;

        for (int i = 0; i < enabled.size(); i++) {
            Module m = enabled.get(i);
            int textW = mc.textRenderer.getWidth(m.getName());
            int boxW = textW + padX * 2 + 1;
            int x = screenW - boxW;
            int color = colorForIndex(i, enabled.size());

            // background fade
            ctx.fill(x, y, screenW, y + rowH, 0xB0101012);
            // colored side strip on right edge
            ctx.fill(screenW - 1, y, screenW, y + rowH, color);
            // top accent
            ctx.fill(x, y, screenW, y + 1, (color & 0x00FFFFFF) | 0x40000000);

            ctx.drawTextWithShadow(mc.textRenderer, m.getName(), x + padX, y + 2, color);
            y += rowH;
        }
    }

    private static int colorForIndex(int index, int total) {
        // hue ramp from cyan to violet
        float hue = 0.55f + 0.18f * (total <= 1 ? 0 : index / (float) (total - 1));
        return Color.HSBtoRGB(hue, 0.55f, 1f);
    }
}
