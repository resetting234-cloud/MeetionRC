package meetion.rc.modules.combat;
import meetion.rc.core.module.AutoModule;

import meetion.rc.core.module.Category;
import meetion.rc.core.module.Module;
import meetion.rc.core.setting.impl.ModeSetting;
import meetion.rc.core.setting.impl.NumberSetting;
import meetion.rc.util.bypass.Anticheat;

@AutoModule
public class Velocity extends Module {

    private final NumberSetting horizontal = register(new NumberSetting("Horizontal%", 0.0, 0.0, 100.0, 1.0));
    private final NumberSetting vertical = register(new NumberSetting("Vertical%", 0.0, 0.0, 100.0, 1.0));
    private final ModeSetting bypass = register(new ModeSetting("Bypass",
            Anticheat.GRIM,
            Anticheat.GRIM, Anticheat.VERUS, Anticheat.VULCAN, Anticheat.MATRIX,
            Anticheat.THEMIS, Anticheat.SPARTAN, Anticheat.NEGATIVITY, Anticheat.OTHER));

    public Velocity() {
        super("Velocity", "Reduces or cancels knockback (anticheat-tunable)", Category.COMBAT);
    }

    public double horizontalMultiplier() { return horizontal.getValue() / 100.0; }
    public double verticalMultiplier() { return vertical.getValue() / 100.0; }
    public String getBypass() { return bypass.getValue(); }
}
