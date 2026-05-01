package meetion.rc.manager;

import meetion.rc.core.module.Category;
import meetion.rc.core.module.Module;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Holds every loaded {@link Module}. Population is automatic via {@link AutoModuleScanner};
 * call sites just need to invoke {@link #init()} once on client start.
 *
 * <p>Note: the old manual {@code add(new KillAura())} list is gone — drop a new
 * {@code @AutoModule} class into {@code meetion.rc.modules.**} and it will appear
 * in the GUI/ArrayList without further code changes.</p>
 */
public class ModuleManager {

    private static final String MOD_ID = "meetionrc";
    private static final String SCAN_ROOT = "meetion.rc.modules";

    private final List<Module> modules = new ArrayList<>();

    public void init() {
        modules.clear();
        modules.addAll(AutoModuleScanner.scan(MOD_ID, SCAN_ROOT));
    }

    public List<Module> getModules() { return modules; }

    public List<Module> getByCategory(Category category) {
        return modules.stream()
                .filter(m -> m.getCategory() == category)
                .sorted(Comparator.comparing(Module::getName))
                .toList();
    }

    @SuppressWarnings("unchecked")
    public <T extends Module> T get(Class<T> clazz) {
        for (Module m : modules) if (clazz.isInstance(m)) return (T) m;
        return null;
    }

    public Module getByName(String name) {
        for (Module m : modules) if (m.getName().equalsIgnoreCase(name)) return m;
        return null;
    }

    public void onKey(int key, int action) {
        if (action != 1) return; // GLFW_PRESS
        for (Module m : modules) {
            if (m.getKey() == key) m.toggle();
        }
    }
}
