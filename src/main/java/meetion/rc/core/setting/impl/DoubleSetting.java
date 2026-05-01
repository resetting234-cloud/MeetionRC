package meetion.rc.core.setting.impl;

import meetion.rc.core.setting.Setting;

/**
 * A continuous numeric setting (rendered as a slider in ClickGUI).
 *
 * <ul>
 *   <li>{@link #min} / {@link #max} — slider range</li>
 *   <li>{@link #step} — quantization step (e.g. 0.5 means slider snaps in half-units)</li>
 *   <li>{@link #decimals} — how many decimal digits the slider label should show.
 *     Auto-derived from {@code step}: a step of {@code 1.0} → 0 decimals, {@code 0.1} → 1, etc.</li>
 * </ul>
 *
 * Replaces the older {@code NumberSetting} (kept as a thin alias for back-compat).
 */
public class DoubleSetting extends Setting<Double> {

    private final double min;
    private final double max;
    private final double step;
    private final int decimals;

    public DoubleSetting(String name, double defaultValue, double min, double max, double step) {
        super(name, defaultValue);
        this.min = min;
        this.max = max;
        this.step = step;
        this.decimals = decimalsFromStep(step);
    }

    public double getMin() { return min; }
    public double getMax() { return max; }
    public double getStep() { return step; }
    public int getDecimals() { return decimals; }

    public int asInt() { return value.intValue(); }
    public float asFloat() { return value.floatValue(); }

    /** Normalised position on the slider (0..1). */
    public double normalised() {
        if (max <= min) return 0;
        return Math.max(0, Math.min(1, (value - min) / (max - min)));
    }

    /** Set value from a normalised slider position (0..1), respecting {@code step}. */
    public void setFromNormalised(double t) {
        double clamped = Math.max(0, Math.min(1, t));
        double raw = min + (max - min) * clamped;
        double snapped = Math.round(raw / step) * step;
        setValue(Math.max(min, Math.min(max, snapped)));
    }

    /** Format the current value for display (uses {@link #decimals}). */
    public String format() {
        return String.format("%." + decimals + "f", value);
    }

    private static int decimalsFromStep(double step) {
        if (step >= 1.0) return 0;
        if (step >= 0.1) return 1;
        if (step >= 0.01) return 2;
        return 3;
    }
}
