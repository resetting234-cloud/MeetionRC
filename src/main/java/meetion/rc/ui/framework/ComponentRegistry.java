package meetion.rc.ui.framework;

import meetion.rc.core.setting.Setting;
import meetion.rc.core.setting.impl.BooleanSetting;
import meetion.rc.core.setting.impl.DoubleSetting;
import meetion.rc.core.setting.impl.NumberSetting;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Maps {@link Setting} subtypes to {@link Component} factories.
 * <p>
 * The {@code SettingsPanel} (Step 5) iterates {@code module.getSettings()} and asks
 * the registry for an appropriate UI widget. Adding a new setting type later (e.g.
 * a {@code ColorSetting}) is a one-liner — register the factory once at startup and
 * every module's settings list automatically renders it.
 *
 * <p>The lookup walks the class hierarchy (so {@code NumberSetting extends DoubleSetting}
 * resolves to the same factory without an explicit registration), making it tolerant
 * of subclasses introduced later.
 */
public final class ComponentRegistry {

    private ComponentRegistry() {}

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final Map<Class<? extends Setting>, Function<Setting, Component>> FACTORIES =
            new LinkedHashMap<>();

    static {
        register(BooleanSetting.class, s -> ToggleComponent.ofSetting((BooleanSetting) s));
        register(DoubleSetting.class,  s -> new SliderComponent((DoubleSetting) s));
        // alias subclass kept for back-compat
        register(NumberSetting.class,  s -> new SliderComponent((NumberSetting) s));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <S extends Setting<?>> void register(Class<S> type, Function<Setting, Component> factory) {
        FACTORIES.put((Class<? extends Setting>) type, (Function) factory);
    }

    /**
     * Build a component for the given setting, walking the class hierarchy. Returns
     * {@code null} if no factory is registered (the SettingsPanel can choose to skip
     * or render a placeholder label in that case).
     */
    public static Component create(Setting<?> setting) {
        if (setting == null) return null;
        Class<?> c = setting.getClass();
        while (c != null && c != Object.class) {
            @SuppressWarnings({"rawtypes", "unchecked"})
            Function<Setting, Component> f = (Function) FACTORIES.get(c);
            if (f != null) return f.apply(setting);
            c = c.getSuperclass();
        }
        return null;
    }

    public static boolean isSupported(Class<? extends Setting<?>> type) {
        Class<?> c = type;
        while (c != null && c != Object.class) {
            if (FACTORIES.containsKey(c)) return true;
            c = c.getSuperclass();
        }
        return false;
    }
}
