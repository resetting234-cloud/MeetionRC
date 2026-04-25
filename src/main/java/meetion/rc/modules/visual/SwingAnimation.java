package meetion.rc.modules.visual;

import meetion.rc.core.module.Category;
import meetion.rc.core.module.Module;
import meetion.rc.core.setting.impl.ModeSetting;

public class SwingAnimation extends Module {

    private final ModeSetting style = register(new ModeSetting("Style", "Smooth", "Smooth", "Slide", "OldSwing", "Vanilla"));

    public SwingAnimation() {
        super("SwingAnimation", "Custom hand swing animation style", Category.VISUAL);
    }

    public String getStyle() { return style.getValue(); }
}
