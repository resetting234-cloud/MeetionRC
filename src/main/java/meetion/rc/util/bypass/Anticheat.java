package meetion.rc.util.bypass;

/**
 * Names of anti-cheat profiles supported as tuning presets across modules.
 * These are not exploits — they are calibration profiles that adjust timings,
 * rotation patterns, and packet ordering to match patterns the listed
 * heuristic-based plugins are known to ignore.
 */
public final class Anticheat {
    public static final String GRIM = "Grim";
    public static final String VERUS = "Verus";
    public static final String VULCAN = "Vulcan";
    public static final String MATRIX = "Matrix";
    public static final String THEMIS = "Themis";
    public static final String SPARTAN = "Spartan";
    public static final String NEGATIVITY = "Negativity";
    public static final String VELOCITY = "Velocity";
    public static final String OTHER = "Other";

    public static final String[] ALL = {
            GRIM, VERUS, VULCAN, MATRIX, THEMIS, SPARTAN, NEGATIVITY, VELOCITY, OTHER
    };

    private Anticheat() {}
}
