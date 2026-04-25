package meetion.rc.modules.player;

import meetion.rc.core.module.Category;
import meetion.rc.core.module.Module;

public class NoJumpDelay extends Module {

    public NoJumpDelay() {
        super("NoJumpDelay", "Removes jump cooldown (handled via mixin)", Category.PLAYER);
    }
}
