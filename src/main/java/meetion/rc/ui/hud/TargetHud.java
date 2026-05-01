package meetion.rc.ui.hud;

import meetion.rc.MeetionRC;
import meetion.rc.modules.combat.KillAura;
import meetion.rc.ui.animation.Animator;
import meetion.rc.ui.animation.Easing;
import meetion.rc.ui.font.Fonts;
import meetion.rc.ui.utils.ColorUtil;
import meetion.rc.ui.utils.Palette;
import meetion.rc.ui.utils.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.Collection;

/**
 * Bottom-center target card. Activates while a {@link KillAura}-style module has a
 * locked target; smoothly scales up + fades in on appearance, slides away on loss.
 *
 * <p>Layout (≈220×60 base, scaled by appear animator):
 * <pre>
 *   ┌──────┬───────────────────────────────────┐
 *   │      │ Name                  12.5 / 20 HP│
 *   │ skin │ ████████░░░░ (hp ramp)           │
 *   ├──────┤ [Speed II] [Strength I] …          │
 *   │■■■■  │                                   │
 *   └──────┴───────────────────────────────────┘
 * </pre>
 */
public final class TargetHud {

    private TargetHud() {}

    private static final int CARD_W = 220;
    private static final int CARD_H = 60;
    private static final int SKIN_SIZE = 28;

    private static final Animator appear = new Animator(0, 220, Easing.EASE_OUT_BACK);
    private static LivingEntity lastTarget = null;

    public static void render(DrawContext ctx) {
        if (MeetionRC.getInstance() == null) return;
        KillAura aura = MeetionRC.getInstance().getModuleManager().get(KillAura.class);
        LivingEntity target = (aura != null && aura.isEnabled()) ? aura.getTarget() : null;

        if (target != lastTarget) {
            lastTarget = target;
            appear.setTarget(target == null ? 0 : 1);
        }

        float t = appear.floatValue();
        if (t < 0.01f) return;

        // hold the last known target while we fade out
        LivingEntity drawTarget = target != null ? target : lastTarget;
        if (drawTarget == null) return;

        int screenW = ctx.getScaledWindowWidth();
        int screenH = ctx.getScaledWindowHeight();
        int x = (screenW - CARD_W) / 2;
        int y = screenH - CARD_H - 32;

        // global fade + slight scale via per-pixel alpha multiplier — keeps geometry stable
        float alpha = Math.min(1f, t);
        int alphaByte = (int) (255 * alpha);

        // shadow + panel
        RenderUtil.dropShadow(ctx, x - 2, y - 2, CARD_W + 4, CARD_H + 4, 6, 4,
                Math.min(120, alphaByte / 2));
        RenderUtil.roundedRect(ctx, x, y, CARD_W, CARD_H, 6,
                ColorUtil.withAlpha(Palette.BG_PANEL, alphaByte));
        // top red border line
        ctx.fill(x, y, x + CARD_W, y + 2, ColorUtil.withAlpha(Palette.RED, alphaByte));

        // skin (left)
        renderSkin(ctx, drawTarget, x + 8, y + 8, alphaByte);

        // armor row under skin
        renderArmor(ctx, drawTarget, x + 8, y + 8 + SKIN_SIZE + 2, alphaByte);

        // right column: name, hp text, hp bar, potions
        int textX = x + 8 + SKIN_SIZE + 10;
        int textY = y + 6;
        String name = displayName(drawTarget);
        Fonts.draw(ctx, Fonts.INTER_BOLD, name, textX, textY,
                ColorUtil.withAlpha(Palette.TEXT_PRIMARY, alphaByte));

        // HP text right-aligned
        float hp = Math.max(0, drawTarget.getHealth());
        float maxHp = Math.max(1, drawTarget.getMaxHealth());
        String hpText = String.format("%.1f / %.0f", hp, maxHp);
        int hpW = Fonts.width(Fonts.MONO, hpText);
        Fonts.draw(ctx, Fonts.MONO, hpText, x + CARD_W - 8 - hpW, textY,
                ColorUtil.withAlpha(Palette.TEXT_SECONDARY, alphaByte));

        // hp bar
        int barX = textX;
        int barY = textY + 14;
        int barW = (x + CARD_W - 8) - barX;
        int barH = 5;
        ctx.fill(barX, barY, barX + barW, barY + barH,
                ColorUtil.argb(0, 0, 0, Math.min(180, alphaByte)));
        float ratio = Math.min(1, hp / maxHp);
        int filled = (int) (barW * ratio);
        int hpColor = ColorUtil.withAlpha(ColorUtil.hpRamp(ratio), alphaByte);
        if (filled > 0) {
            RenderUtil.roundedRect(ctx, barX, barY, filled, barH, barH / 2, hpColor);
        }

        // potion tags
        renderPotions(ctx, drawTarget, textX, barY + barH + 4, alphaByte);
    }

    // ---------------------------------------------------------------------------
    // skin
    // ---------------------------------------------------------------------------

    private static void renderSkin(DrawContext ctx, LivingEntity target, int x, int y, int alpha) {
        if (target instanceof PlayerEntity player) {
            SkinTextures st = MinecraftClient.getInstance()
                    .getSkinProvider()
                    .getSkinTextures(player.getGameProfile());
            // PlayerSkinDrawer handles base + overlay layers; alpha not directly supported,
            // so we rely on the panel beneath providing the fade illusion.
            PlayerSkinDrawer.draw(ctx, st, x, y, SKIN_SIZE);
        } else {
            // fallback: render an icon-ish dim box for non-player entities
            RenderUtil.roundedRect(ctx, x, y, SKIN_SIZE, SKIN_SIZE, 4,
                    ColorUtil.withAlpha(Palette.BG_HOVER, alpha));
            Fonts.draw(ctx, Fonts.INTER_BOLD, "?",
                    x + SKIN_SIZE / 2 - 3, y + SKIN_SIZE / 2 - 4,
                    ColorUtil.withAlpha(Palette.TEXT_MUTED, alpha));
        }
    }

    // ---------------------------------------------------------------------------
    // armor
    // ---------------------------------------------------------------------------

    private static void renderArmor(DrawContext ctx, LivingEntity target, int x, int y, int alpha) {
        EquipmentSlot[] slots = {
                EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                EquipmentSlot.LEGS, EquipmentSlot.FEET
        };
        int box = 6;
        int gap = 1;
        for (int i = 0; i < slots.length; i++) {
            ItemStack stack = target.getEquippedStack(slots[i]);
            int color = stack.isEmpty()
                    ? ColorUtil.withAlpha(Palette.TEXT_MUTED, Math.min(60, alpha / 2))
                    : ColorUtil.withAlpha(armorColor(stack.getItem()), alpha);
            ctx.fill(x + i * (box + gap), y, x + i * (box + gap) + box, y + box, color);
        }
    }

    private static int armorColor(Item item) {
        String key = item.toString().toLowerCase();
        if (key.contains("netherite")) return 0xFF555555;
        if (key.contains("diamond"))   return 0xFF6BD9D9;
        if (key.contains("iron"))      return 0xFFD8D8D8;
        if (key.contains("gold"))      return 0xFFE8C463;
        if (key.contains("chainmail")) return 0xFF8A8A8A;
        if (key.contains("leather"))   return 0xFFB57543;
        if (key.contains("turtle"))    return 0xFF6BD96B;
        return 0xFFCCCCCC;
    }

    // ---------------------------------------------------------------------------
    // potions
    // ---------------------------------------------------------------------------

    private static void renderPotions(DrawContext ctx, LivingEntity target, int x, int y, int alpha) {
        Collection<StatusEffectInstance> effects = target.getStatusEffects();
        if (effects == null || effects.isEmpty()) return;
        int cursorX = x;
        int tagW   = 56;
        int tagH   = 10;
        for (StatusEffectInstance fx : effects) {
            if (cursorX + tagW > x + (CARD_W - 16)) break;  // simple overflow guard
            int rim = colorFor(fx.getEffectType().value().getCategory());
            ctx.fill(cursorX, y, cursorX + tagW, y + tagH,
                    ColorUtil.argb(0, 0, 0, Math.min(140, alpha)));
            ctx.fill(cursorX, y, cursorX + 2, y + tagH,
                    ColorUtil.withAlpha(rim, alpha));
            String label = effectLabel(fx);
            Fonts.draw(ctx, Fonts.MONO, label, cursorX + 4, y + 1,
                    ColorUtil.withAlpha(Palette.TEXT_PRIMARY, alpha));
            cursorX += tagW + 2;
        }
    }

    private static int colorFor(StatusEffectCategory category) {
        return switch (category) {
            case BENEFICIAL -> 0xFF44E08A;
            case NEUTRAL    -> 0xFF6BB6FF;
            case HARMFUL    -> Palette.RED;
        };
    }

    private static String effectLabel(StatusEffectInstance fx) {
        String key = fx.getEffectType().value().getTranslationKey();
        // strip "effect.minecraft." prefix and capitalise first letter
        int dot = key.lastIndexOf('.');
        String name = dot >= 0 ? key.substring(dot + 1) : key;
        if (name.isEmpty()) return "?";
        name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        int amp = fx.getAmplifier();
        return amp > 0 ? name + " " + roman(amp + 1) : name;
    }

    private static String roman(int n) {
        return switch (n) {
            case 1 -> "I"; case 2 -> "II"; case 3 -> "III"; case 4 -> "IV";
            case 5 -> "V"; case 6 -> "VI"; case 7 -> "VII"; case 8 -> "VIII";
            default -> Integer.toString(n);
        };
    }

    private static String displayName(LivingEntity e) {
        return e instanceof PlayerEntity p
                ? p.getGameProfile().getName()
                : Text.translatable(e.getType().getTranslationKey()).getString();
    }
}
