package meetion.rc.screen.hud;

import meetion.rc.MeetionRC;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public final class WatermarkRenderer {

    private WatermarkRenderer() {}

    public static void render(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        String name = MeetionRC.NAME;
        String ver = "v" + MeetionRC.VERSION;
        int padX = 6;
        int padY = 4;
        int x = 6;
        int y = 6;
        int textW = mc.textRenderer.getWidth(name + "  " + ver);
        int boxW = textW + padX * 2;
        int boxH = mc.textRenderer.fontHeight + padY * 2;

        // background box
        ctx.fill(x, y, x + boxW, y + boxH, 0xCC0E0E12);
        // accent strip on left
        ctx.fill(x, y, x + 2, y + boxH, 0xFF44C2FF);
        // accent line on top
        ctx.fill(x + 2, y, x + boxW, y + 1, 0x4044C2FF);

        ctx.drawTextWithShadow(mc.textRenderer, name, x + padX, y + padY, 0xFFFFFFFF);
        int nameW = mc.textRenderer.getWidth(name + "  ");
        ctx.drawTextWithShadow(mc.textRenderer, ver, x + padX + nameW, y + padY, 0xFF8AA0B0);
    }
}
