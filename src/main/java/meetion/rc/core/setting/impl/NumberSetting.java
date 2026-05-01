package meetion.rc.core.setting.impl;

/**
 * Back-compat alias for {@link DoubleSetting}. New code should use {@code DoubleSetting} directly;
 * this subclass exists only so older module sources keep compiling without a sweep.
 */
public class NumberSetting extends DoubleSetting {
    public NumberSetting(String name, double defaultValue, double min, double max, double step) {
        super(name, defaultValue, min, max, step);
    }
}
