package meetion.rc.core.module;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a {@link Module} subclass for automatic discovery & registration on startup.
 * <p>
 * The {@link meetion.rc.manager.AutoModuleScanner} walks the mod's resource roots,
 * finds every {@code .class} under {@link #scanRoot()}, instantiates classes that
 * carry this annotation and extend {@code Module}, and feeds them into the
 * {@link meetion.rc.manager.ModuleManager}.
 * <p>
 * Adding a new module is now a single step: drop a class annotated with
 * {@code @AutoModule} extending {@code Module} into {@code meetion.rc.modules.**}
 * — no manual registry edits.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AutoModule {

    /** Root package the scanner searches. Subpackages are walked recursively. */
    String scanRoot() default "meetion.rc.modules";

    /** Whether this module should be enabled by default after first startup. */
    boolean enabledByDefault() default false;
}
