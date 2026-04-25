package meetion.rc.core.setting.impl;

import meetion.rc.core.setting.Setting;
import org.lwjgl.glfw.GLFW;

public class BindSetting extends Setting<Integer> {
    public BindSetting(String name, int defaultKey) {
        super(name, defaultKey);
    }

    public boolean isSet() { return value != GLFW.GLFW_KEY_UNKNOWN; }

    public String keyName() {
        if (!isSet()) return "None";
        String key = GLFW.glfwGetKeyName(value, 0);
        return key != null ? key.toUpperCase() : String.valueOf(value);
    }
}
