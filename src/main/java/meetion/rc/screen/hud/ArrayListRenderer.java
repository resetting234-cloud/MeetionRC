package meetion.rc.screen.hud;

import meetion.rc.MeetionRC;
import meetion.rc.core.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

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
        int y = 4;
        int screenW = ctx.getScaledWindowWidth();
        for (Module m : enabled) {
            int w = mc.textRenderer.getWidth(m.getName());
            int x = screenW - w - 4;
            ctx.fill(x - 2, y - 1, screenW, y + mc.textRenderer.fontHeight, 0x60000000);
            ctx.drawTextWithShadow(mc.textRenderer, m.getName(), x, y, 0xFF44C2FF);
            y += mc.textRenderer.fontHeight + 1;
        }
    }
}
