package meetion.rc.manager;

import meetion.rc.core.module.AutoModule;
import meetion.rc.core.module.Module;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Walks the mod's resource roots and instantiates every {@code Module} subclass marked
 * with {@link AutoModule}.
 * <p>
 * In a Fabric dev environment {@link ModContainer#getRootPaths()} returns both
 * {@code build/classes/java/main} and {@code build/resources/main}. Only the first
 * actually contains {@code .class} files; we skip the rest silently.
 * <p>
 * Failures while loading a single class are logged and ignored — they should never
 * abort the rest of the scan, otherwise one bad module in {@code modules/}
 * would break the whole client.
 */
public final class AutoModuleScanner {

    private AutoModuleScanner() {}

    public static List<Module> scan(String modId, String basePackage) {
        ModContainer container = FabricLoader.getInstance().getModContainer(modId).orElse(null);
        if (container == null) {
            System.err.println("[METTRC] AutoModuleScanner: mod container '" + modId + "' not found, no modules will be loaded.");
            return List.of();
        }

        ClassLoader cl = AutoModuleScanner.class.getClassLoader();
        String relative = basePackage.replace('.', '/');
        List<Module> out = new ArrayList<>();

        for (Path root : container.getRootPaths()) {
            Path start = root.resolve(relative);
            if (!Files.isDirectory(start)) continue;

            try (Stream<Path> walk = Files.walk(start)) {
                walk.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".class"))
                    .forEach(p -> tryLoad(root, p, cl, out));
            } catch (IOException e) {
                System.err.println("[METTRC] AutoModuleScanner: failed to walk " + start + ": " + e.getMessage());
            }
        }

        out.sort(Comparator.comparing((Module m) -> m.getCategory().ordinal()).thenComparing(Module::getName));
        return out;
    }

    private static void tryLoad(Path root, Path classFile, ClassLoader cl, List<Module> out) {
        String relative = root.relativize(classFile).toString().replace('\\', '/');
        if (!relative.endsWith(".class")) return;
        String binaryName = relative.substring(0, relative.length() - ".class".length()).replace('/', '.');

        try {
            Class<?> cls = Class.forName(binaryName, false, cl);
            if (cls.isInterface() || Modifier.isAbstract(cls.getModifiers())) return;
            if (!cls.isAnnotationPresent(AutoModule.class)) return;
            if (!Module.class.isAssignableFrom(cls)) return;

            Module instance = (Module) cls.getDeclaredConstructor().newInstance();
            AutoModule meta = cls.getAnnotation(AutoModule.class);
            if (meta.enabledByDefault()) instance.setEnabled(true);
            out.add(instance);
        } catch (Throwable t) {
            System.err.println("[METTRC] AutoModuleScanner: failed to load " + binaryName + ": " + t);
        }
    }
}
