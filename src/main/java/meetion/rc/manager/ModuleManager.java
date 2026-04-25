package meetion.rc.manager;

import meetion.rc.core.module.Category;
import meetion.rc.core.module.Module;
import meetion.rc.modules.combat.*;
import meetion.rc.modules.movement.*;
import meetion.rc.modules.player.*;
import meetion.rc.modules.visual.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ModuleManager {

    private final List<Module> modules = new ArrayList<>();

    public void init() {
        // Combat
        add(new KillAura());
        add(new TriggerBot());
        add(new AutoClicker());
        add(new AimAssist());
        add(new ElytraTarget());
        add(new Velocity());

        // Movement
        add(new NoSlow());
        add(new AutoSprint());
        add(new InvMove());
        add(new SuperFirework());
        add(new NoWeb());

        // Player
        add(new AutoArmor());
        add(new AutoPotion());
        add(new NoJumpDelay());

        // Visual
        add(new HudModule());
        add(new SwingAnimation());
        add(new AspectRatio());
        add(new CustomHitSound());
        add(new Particless());
        add(new Fps());
        add(new AutoCommand());
    }

    private void add(Module module) {
        modules.add(module);
    }

    public List<Module> getModules() { return modules; }

    public List<Module> getByCategory(Category category) {
        return modules.stream().filter(m -> m.getCategory() == category).sorted(Comparator.comparing(Module::getName)).toList();
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
