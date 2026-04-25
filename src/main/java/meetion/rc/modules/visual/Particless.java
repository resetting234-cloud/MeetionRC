package meetion.rc.modules.visual;

import meetion.rc.core.module.Category;
import meetion.rc.core.module.Module;
import meetion.rc.core.setting.impl.MultiSelectSetting;

import java.util.List;

public class Particless extends Module {

    private final MultiSelectSetting hide = register(new MultiSelectSetting(
            "Hide", List.of("Damage", "Crit", "Smoke", "Block"),
            "Damage", "Crit", "Smoke", "Block", "Lava", "Water", "Splash", "All"));

    public Particless() {
        super("Particless", "Hides selected particle effects", Category.VISUAL);
    }

    public boolean shouldHide(String type) {
        return isEnabled() && (hide.isSelected("All") || hide.isSelected(type));
    }
}
