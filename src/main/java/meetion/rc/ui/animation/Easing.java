package meetion.rc.ui.animation;

/**
 * Easing curves for {@link Animation} / {@link Animator}.
 * <p>
 * Functions take a normalised progress {@code t ∈ [0, 1]} and return the eased
 * progress, also in [0, 1]. Implementations are pure and allocation-free so they
 * can be called every frame without GC pressure.
 *
 * <p>Reference: <a href="https://easings.net/">easings.net</a>.
 */
@FunctionalInterface
public interface Easing {

    double apply(double t);

    // ---------------------------------------------------------------------------
    // Curves
    // ---------------------------------------------------------------------------

    Easing LINEAR = t -> t;

    /** Symmetric ease-in-out, gentle. Default for hover transitions. */
    Easing EASE_IN_OUT_QUAD = t -> t < 0.5 ? 2 * t * t : 1 - Math.pow(-2 * t + 2, 2) / 2;

    /** Strong overshoot tail — ideal for "expensive" panel slide-out animations. */
    Easing EASE_OUT_EXPO = t -> t == 1 ? 1 : 1 - Math.pow(2, -10 * t);

    Easing EASE_IN_EXPO  = t -> t == 0 ? 0 : Math.pow(2, 10 * t - 10);

    Easing EASE_OUT_CUBIC = t -> 1 - Math.pow(1 - t, 3);
    Easing EASE_IN_CUBIC  = t -> t * t * t;

    /** Ease-out with a gentle elastic-like overshoot, good for snappy toggles. */
    Easing EASE_OUT_BACK = t -> {
        final double c1 = 1.70158;
        final double c3 = c1 + 1;
        return 1 + c3 * Math.pow(t - 1, 3) + c1 * Math.pow(t - 1, 2);
    };
}
