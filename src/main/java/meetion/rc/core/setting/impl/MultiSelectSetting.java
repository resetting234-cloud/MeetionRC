package meetion.rc.core.setting.impl;

import meetion.rc.core.setting.Setting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MultiSelectSetting extends Setting<Set<String>> {
    private final List<String> options;

    public MultiSelectSetting(String name, List<String> defaults, String... options) {
        super(name, new HashSet<>(defaults));
        this.options = Arrays.asList(options);
    }

    public List<String> getOptions() { return options; }

    public boolean isSelected(String option) { return value.contains(option); }

    public void toggle(String option) {
        if (value.contains(option)) value.remove(option);
        else value.add(option);
    }

    public List<String> asList() { return new ArrayList<>(value); }
}
