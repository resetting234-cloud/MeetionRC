package meetion.rc.modules.player;

import meetion.rc.core.event.EventHandler;
import meetion.rc.core.event.events.TickEvent;
import meetion.rc.core.module.Category;
import meetion.rc.core.module.Module;
import meetion.rc.core.setting.impl.BooleanSetting;
import meetion.rc.core.setting.impl.NumberSetting;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AutoPotion extends Module {

    private final NumberSetting healthThreshold = register(new NumberSetting("HealthThreshold", 14.0, 1.0, 20.0, 1.0));
    private final BooleanSetting onlySplash = register(new BooleanSetting("OnlySplash", true));
    private final NumberSetting delay = register(new NumberSetting("Delay", 350.0, 50.0, 2000.0, 25.0));

    private long lastUse;

    public AutoPotion() {
        super("AutoPotion", "Throws healing potions when low health", Category.PLAYER);
    }

    @EventHandler
    public void onTick(TickEvent ev) {
        if (ev.getEra() != meetion.rc.core.event.Event.Era.PRE) return;
        if (mc().player == null) return;
        if (mc().player.getHealth() > healthThreshold.getValue()) return;
        if (System.currentTimeMillis() - lastUse < delay.getValue()) return;

        int slot = findHealing();
        if (slot < 0) return;

        int prev = mc().player.getInventory().getSelectedSlot();
        mc().player.getInventory().setSelectedSlot(slot);
        mc().getNetworkHandler().sendPacket(new net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket(slot));

        // Look down for splash
        float prevPitch = mc().player.getPitch();
        mc().player.setPitch(90f);
        mc().interactionManager.interactItem(mc().player, Hand.MAIN_HAND);
        mc().player.setPitch(prevPitch);

        mc().player.getInventory().setSelectedSlot(prev);
        mc().getNetworkHandler().sendPacket(new net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket(prev));
        lastUse = System.currentTimeMillis();
    }

    private int findHealing() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc().player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            boolean isSplash = stack.isOf(Items.SPLASH_POTION) || stack.isOf(Items.LINGERING_POTION);
            if (!isSplash && onlySplash.getValue()) continue;
            if (!stack.isOf(Items.POTION) && !isSplash) continue;
            PotionContentsComponent contents = stack.get(DataComponentTypes.POTION_CONTENTS);
            if (contents == null) continue;
            for (var inst : contents.getEffects()) {
                if (inst.getEffectType().value() == StatusEffects.INSTANT_HEALTH.value()) return i;
                if (inst.getEffectType().value() == StatusEffects.REGENERATION.value()) return i;
            }
        }
        return -1;
    }
}
