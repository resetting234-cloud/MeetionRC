package meetion.rc.screen.hud;

import meetion.rc.MeetionRC;
import meetion.rc.modules.combat.KillAura;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public final class TargetHudRenderer {

    private TargetHudRenderer() {}

    public static void render(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (MeetionRC.getInstance() == null) return;
        KillAura aura = MeetionRC.getInstance().getModuleManager().get(KillAura.class);
        if (aura == null || !aura.isEnabled()) return;
        LivingEntity target = aura.getTarget();
        if (target == null) return;

        int boxW = 130;
        int boxH = 38;
        int x = ctx.getScaledWindowWidth() / 2 + 30;
        int y = ctx.getScaledWindowHeight() / 2 - boxH / 2;

        // panel
        ctx.fill(x, y, x + boxW, y + boxH, 0xCC0E0E12);
        ctx.fill(x, y, x + 2, y + boxH, 0xFFFF4565);

        // name
        String name = target instanceof PlayerEntity p ? p.getGameProfile().getName() : Text.translatable(target.getType().getTranslationKey()).getString();
        ctx.drawTextWithShadow(mc.textRenderer, name, x + 8, y + 5, 0xFFFFFFFF);

        // hp bar
        float hp = target.getHealth();
        float maxHp = target.getMaxHealth();
        float ratio = Math.min(1f, Math.max(0f, hp / Math.max(1f, maxHp)));
        int barX = x + 8;
        int barY = y + 18;
        int barW = boxW - 16;
        int barH = 6;
        ctx.fill(barX, barY, barX + barW, barY + barH, 0xFF222530);
        int filled = (int) (barW * ratio);
        int color = ratio > 0.6f ? 0xFF44E08A : ratio > 0.3f ? 0xFFFFB030 : 0xFFFF5050;
        ctx.fill(barX, barY, barX + filled, barY + barH, color);

        // hp text
        String hpText = String.format("%.1f / %.0f HP", hp, maxHp);
        ctx.drawTextWithShadow(mc.textRenderer, hpText, x + 8, y + 27, 0xFFB6BCC8);
    }
}
