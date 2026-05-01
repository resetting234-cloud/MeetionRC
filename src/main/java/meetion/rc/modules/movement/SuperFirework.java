package meetion.rc.modules.movement;
import meetion.rc.core.module.AutoModule;

import meetion.rc.core.event.EventHandler;
import meetion.rc.core.event.events.TickEvent;
import meetion.rc.core.module.Category;
import meetion.rc.core.module.Module;
import meetion.rc.core.setting.impl.NumberSetting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;

@AutoModule
public class SuperFirework extends Module {

    private final NumberSetting interval = register(new NumberSetting("IntervalMs", 600.0, 100.0, 3000.0, 25.0));
    private final NumberSetting minSpeed = register(new NumberSetting("MinSpeedMps", 25.0, 0.0, 60.0, 1.0));

    private long nextUse;

    public SuperFirework() {
        super("SuperFirework", "Auto-uses fireworks while gliding to keep top speed", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(TickEvent ev) {
        if (ev.getEra() != meetion.rc.core.event.Event.Era.PRE) return;
        if (mc().player == null || mc().getNetworkHandler() == null) return;
        if (!mc().player.isGliding()) return;
        if (System.currentTimeMillis() < nextUse) return;

        double speed = mc().player.getVelocity().horizontalLength() * 20; // blocks/s
        if (speed >= minSpeed.getValue()) return;

        int slot = findFirework();
        if (slot < 0) return;

        int prev = mc().player.getInventory().getSelectedSlot();
        if (slot < 9) {
            mc().player.getInventory().setSelectedSlot(slot);
            mc().getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
        } else {
            return;
        }

        mc().interactionManager.interactItem(mc().player, Hand.MAIN_HAND);
        mc().player.getInventory().setSelectedSlot(prev);
        mc().getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(prev));
        nextUse = System.currentTimeMillis() + interval.getValue().longValue();
    }

    private int findFirework() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc().player.getInventory().getStack(i);
            if (stack.isOf(Items.FIREWORK_ROCKET)) return i;
        }
        return -1;
    }
}
