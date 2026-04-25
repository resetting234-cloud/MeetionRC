package meetion.rc.manager;

import org.lwjgl.glfw.GLFW;

public class KeybindManager {

    public int resolve(String name) {
        if (name == null || name.isEmpty()) return GLFW.GLFW_KEY_UNKNOWN;
        String n = name.toUpperCase();
        // single char letter
        if (n.length() == 1) {
            char c = n.charAt(0);
            if (c >= 'A' && c <= 'Z') return GLFW.GLFW_KEY_A + (c - 'A');
            if (c >= '0' && c <= '9') return GLFW.GLFW_KEY_0 + (c - '0');
        }
        return switch (n) {
            case "SPACE" -> GLFW.GLFW_KEY_SPACE;
            case "ENTER", "RETURN" -> GLFW.GLFW_KEY_ENTER;
            case "ESCAPE", "ESC" -> GLFW.GLFW_KEY_ESCAPE;
            case "TAB" -> GLFW.GLFW_KEY_TAB;
            case "LSHIFT", "SHIFT" -> GLFW.GLFW_KEY_LEFT_SHIFT;
            case "LCONTROL", "LCTRL", "CONTROL", "CTRL" -> GLFW.GLFW_KEY_LEFT_CONTROL;
            case "LALT", "ALT" -> GLFW.GLFW_KEY_LEFT_ALT;
            case "F1" -> GLFW.GLFW_KEY_F1;
            case "F2" -> GLFW.GLFW_KEY_F2;
            case "F3" -> GLFW.GLFW_KEY_F3;
            case "F4" -> GLFW.GLFW_KEY_F4;
            case "F5" -> GLFW.GLFW_KEY_F5;
            case "F6" -> GLFW.GLFW_KEY_F6;
            case "F7" -> GLFW.GLFW_KEY_F7;
            case "F8" -> GLFW.GLFW_KEY_F8;
            case "F9" -> GLFW.GLFW_KEY_F9;
            case "F10" -> GLFW.GLFW_KEY_F10;
            case "F11" -> GLFW.GLFW_KEY_F11;
            case "F12" -> GLFW.GLFW_KEY_F12;
            case "RSHIFT" -> GLFW.GLFW_KEY_RIGHT_SHIFT;
            case "NONE" -> GLFW.GLFW_KEY_UNKNOWN;
            default -> GLFW.GLFW_KEY_UNKNOWN;
        };
    }
}
