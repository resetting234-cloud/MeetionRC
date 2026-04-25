package meetion.rc.screen.hud;

import meetion.rc.MeetionRC;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public final class WatermarkRenderer {

    private WatermarkRenderer() {}

    public static void render(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        String text = "§b" + MeetionRC.NAME + " §8| §7v" + MeetionRC.VERSION;
        ctx.drawTextWithShadow(mc.textRenderer, text, 4, 4, 0xFFFFFFFF);
    }
}
