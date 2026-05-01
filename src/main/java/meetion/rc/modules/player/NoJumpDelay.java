package meetion.rc.modules.player;
import meetion.rc.core.module.AutoModule;

import meetion.rc.core.module.Category;
import meetion.rc.core.module.Module;

@AutoModule
public class NoJumpDelay extends Module {

    public NoJumpDelay() {
        super("NoJumpDelay", "Removes jump cooldown (handled via mixin)", Category.PLAYER);
    }
}
