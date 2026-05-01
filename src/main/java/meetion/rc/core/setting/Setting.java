package meetion.rc.core.setting;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Generic, type-safe setting carrier used by every Module.
 * <p>
 * - {@link #description} — short tooltip shown in ClickGUI under the setting name.
 * - {@link #visibility} — predicate the GUI uses to skip drawing this setting (e.g. hide
 *   "Bypass" mode-specific tweaks when a different bypass is selected).
 * - {@link #listeners} — callbacks notified whenever the value changes; UI components
 *   subscribe here to refresh themselves without needing per-frame polling.
 */
public abstract class Setting<T> {

    protected final String name;
    protected String description = "";
    protected T value;
    protected final T defaultValue;
    protected BooleanSupplier visibility = () -> true;
    private final List<Consumer<T>> listeners = new ArrayList<>();

    protected Setting(String name, T defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public T getValue() { return value; }
    public T getDefaultValue() { return defaultValue; }

    @SuppressWarnings("unchecked")
    public void setValue(Object newValue) {
        T cast = (T) newValue;
        if (cast == null ? value == null : cast.equals(value)) return;
        this.value = cast;
        for (Consumer<T> l : listeners) l.accept(cast);
    }

    public boolean isVisible() { return visibility.getAsBoolean(); }

    /** Fluent: hide this setting when the predicate returns false. */
    public Setting<T> visibleWhen(BooleanSupplier visibility) {
        this.visibility = visibility;
        return this;
    }

    /** Fluent: attach a short tooltip / description shown under the setting name. */
    public Setting<T> describe(String description) {
        this.description = description;
        return this;
    }

    /** Subscribe to value changes. */
    public Setting<T> onChange(Consumer<T> listener) {
        listeners.add(listener);
        return this;
    }

    /** Back-compat alias kept so older call-sites that used the old fluent name keep compiling. */
    public Setting<T> setVisibility(BooleanSupplier visibility) { return visibleWhen(visibility); }
}
