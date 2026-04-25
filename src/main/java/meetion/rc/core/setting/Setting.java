package meetion.rc.core.setting;

import java.util.function.BooleanSupplier;

public abstract class Setting<T> {

    protected final String name;
    protected T value;
    protected final T defaultValue;
    protected BooleanSupplier visibility = () -> true;

    protected Setting(String name, T defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public String getName() { return name; }
    public T getValue() { return value; }
    public T getDefaultValue() { return defaultValue; }

    @SuppressWarnings("unchecked")
    public void setValue(Object value) { this.value = (T) value; }

    public boolean isVisible() { return visibility.getAsBoolean(); }

    public Setting<T> setVisibility(BooleanSupplier visibility) {
        this.visibility = visibility;
        return this;
    }
}
