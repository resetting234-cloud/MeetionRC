package meetion.rc.modules.visual;

import meetion.rc.core.module.Category;
import meetion.rc.core.module.Module;
import meetion.rc.core.setting.impl.NumberSetting;

public class AspectRatio extends Module {

    private final NumberSetting fovScale = register(new NumberSetting("FovScale", 1.0, 0.5, 2.0, 0.05));

    public AspectRatio() {
        super("AspectRatio", "Adjusts the rendered FOV multiplier", Category.VISUAL);
    }

    public float adjust(float currentFov) {
        return (float) (currentFov * fovScale.getValue());
    }
}
