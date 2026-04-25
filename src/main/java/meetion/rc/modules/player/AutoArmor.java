package meetion.rc.modules.player;

import meetion.rc.core.event.EventHandler;
import meetion.rc.core.event.events.TickEvent;
import meetion.rc.core.module.Category;
import meetion.rc.core.module.Module;
import meetion.rc.core.setting.impl.BooleanSetting;
import meetion.rc.core.setting.impl.NumberSetting;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public class AutoArmor extends Module {

    private final NumberSetting delay = register(new NumberSetting("Delay", 100.0, 0.0, 1000.0, 10.0));
    private final BooleanSetting inventoryOnly = register(new BooleanSetting("InventoryOnly", false));

    private long nextSwap;

    public AutoArmor() {
        super("AutoArmor", "Automatically equips the best armor pieces", Category.PLAYER);
    }

    @EventHandler
    public void onTick(TickEvent ev) {
        if (ev.getEra() != meetion.rc.core.event.Event.Era.PRE) return;
        if (mc().player == null || mc().interactionManager == null) return;
        if (mc().player.currentScreenHandler != mc().player.playerScreenHandler) {
            if (inventoryOnly.getValue()) return;
        }
        if (System.currentTimeMillis() < nextSwap) return;

        for (EquipmentSlot slot : new EquipmentSlot[]{ EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET }) {
            int armorIndex = slotToIndex(slot);
            ItemStack current = mc().player.getInventory().getStack(armorIndex);
            int bestSlot = findBestSlot(slot, current);
            if (bestSlot >= 0) {
                swap(bestSlot, armorIndex);
                nextSwap = System.currentTimeMillis() + (long) delay.getValue();
                return;
            }
        }
    }

    private int slotToIndex(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> 39;
            case CHEST -> 38;
            case LEGS -> 37;
            case FEET -> 36;
            default -> -1;
        };
    }

    private int findBestSlot(EquipmentSlot slot, ItemStack current) {
        int bestSlot = -1;
        int bestScore = scoreOf(current, slot);
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc().player.getInventory().getStack(i);
            int score = scoreOf(stack, slot);
            if (score > bestScore) { bestScore = score; bestSlot = i; }
        }
        return bestSlot;
    }

    private int scoreOf(ItemStack stack, EquipmentSlot slot) {
        if (stack.isEmpty()) return -1;
        if (!(stack.getItem() instanceof ArmorItem armor)) return -1;
        if (!stack.canEquip(slot, mc().player)) return -1;
        return armor.getMaterial().value().protection().getOrDefault(slot, 0) * 10
                + (int) armor.getMaterial().value().toughness();
    }

    private void swap(int from, int to) {
        ClientPlayerInteractionManager im = mc().interactionManager;
        int containerSlot = from < 9 ? from + 36 : from;
        im.clickSlot(mc().player.playerScreenHandler.syncId, containerSlot, 0, SlotActionType.PICKUP, mc().player);
        im.clickSlot(mc().player.playerScreenHandler.syncId, to, 0, SlotActionType.PICKUP, mc().player);
        if (!mc().player.getInventory().getStack(from).isEmpty()) {
            im.clickSlot(mc().player.playerScreenHandler.syncId, containerSlot, 0, SlotActionType.PICKUP, mc().player);
        }
    }
}
