package meetion.rc.core.setting.impl;

import meetion.rc.core.setting.Setting;

import java.util.Arrays;
import java.util.List;

public class ModeSetting extends Setting<String> {
    private final List<String> modes;

    public ModeSetting(String name, String defaultValue, String... modes) {
        super(name, defaultValue);
        this.modes = Arrays.asList(modes);
    }

    public List<String> getModes() { return modes; }

    public boolean is(String mode) { return value.equalsIgnoreCase(mode); }

    public void cycle() {
        int idx = modes.indexOf(value);
        value = modes.get((idx + 1) % modes.size());
    }
}
