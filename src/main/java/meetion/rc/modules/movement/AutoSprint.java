package meetion.rc.modules.movement;

import meetion.rc.core.event.EventHandler;
import meetion.rc.core.event.events.TickEvent;
import meetion.rc.core.module.Category;
import meetion.rc.core.module.Module;
import meetion.rc.core.setting.impl.BooleanSetting;
import meetion.rc.core.setting.impl.ModeSetting;

public class AutoSprint extends Module {

    private final ModeSetting mode = register(new ModeSetting("Mode", "Always", "Always", "Forward", "Omni"));
    private final BooleanSetting keepSneak = register(new BooleanSetting("KeepWhileSneak", false));

    public AutoSprint() {
        super("AutoSprint", "Automatically sprints when moving", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(TickEvent ev) {
        if (ev.getEra() != meetion.rc.core.event.Event.Era.PRE) return;
        if (mc().player == null) return;
        if (mc().player.isSneaking() && !keepSneak.getValue()) return;
        if (mc().player.getHungerManager().getFoodLevel() <= 6) return;

        boolean canSprint = switch (mode.getValue()) {
            case "Always" -> hasAnyInput();
            case "Forward" -> mc().player.input.movementForward > 0;
            case "Omni" -> hasAnyInput();
            default -> false;
        };
        if (canSprint) mc().player.setSprinting(true);
    }

    private boolean hasAnyInput() {
        if (mc().player == null || mc().player.input == null) return false;
        return mc().player.input.movementForward != 0 || mc().player.input.movementSideways != 0;
    }
}
