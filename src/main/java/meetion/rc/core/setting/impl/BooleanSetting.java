package meetion.rc.core.setting.impl;

import meetion.rc.core.setting.Setting;

/** Boolean toggle. Rendered as the rectangular toggle pill in ClickGUI. */
public class BooleanSetting extends Setting<Boolean> {

    public BooleanSetting(String name, Boolean defaultValue) {
        super(name, defaultValue);
    }

    public boolean toggle() {
        boolean next = !value;
        setValue(next);
        return next;
    }
}
