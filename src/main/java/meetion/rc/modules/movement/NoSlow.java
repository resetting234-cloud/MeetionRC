package meetion.rc.modules.movement;
import meetion.rc.core.module.AutoModule;

import meetion.rc.core.module.Category;
import meetion.rc.core.module.Module;
import meetion.rc.core.setting.impl.BooleanSetting;
import meetion.rc.core.setting.impl.ModeSetting;
import meetion.rc.util.bypass.Anticheat;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.consume.UseAction;

@AutoModule
public class NoSlow extends Module {

    private final BooleanSetting whileEating = register(new BooleanSetting("WhileEating", true));
    private final BooleanSetting whileBlocking = register(new BooleanSetting("WhileBlocking", true));
    private final BooleanSetting whileBow = register(new BooleanSetting("WhileBow", true));
    private final ModeSetting bypass = register(new ModeSetting("Bypass",
            Anticheat.GRIM,
            Anticheat.GRIM, Anticheat.VERUS, Anticheat.VULCAN, Anticheat.MATRIX,
            Anticheat.THEMIS, Anticheat.SPARTAN, Anticheat.NEGATIVITY, Anticheat.OTHER));

    public NoSlow() {
        super("NoSlow", "Removes movement slowdown while using items", Category.MOVEMENT);
    }

    public boolean shouldCancelSlow(PlayerEntity player) {
        if (!isEnabled()) return false;
        if (!player.isUsingItem()) return false;
        UseAction action = player.getActiveItem().getUseAction();
        return switch (action) {
            case EAT, DRINK -> whileEating.getValue();
            case BLOCK -> whileBlocking.getValue();
            case BOW, SPYGLASS, CROSSBOW, SPEAR -> whileBow.getValue();
            default -> false;
        };
    }
}
