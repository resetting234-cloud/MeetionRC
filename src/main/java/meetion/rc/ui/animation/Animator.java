package meetion.rc.ui.animation;

/**
 * Smooth target-tracking animator.
 *
 * <p>Pattern: you call {@link #setTarget(double)} whenever the desired value changes
 * (e.g. on hover-in / hover-out, on tab change, on panel show/hide), and ask
 * {@link #getValue()} every frame. The animator interpolates from whatever value it
 * was at when the target last changed, towards the new target, over {@link #durationMs}
 * using the supplied {@link Easing}.
 *
 * <p>Compared to a fixed {@link Animation}, this gracefully handles target changes
 * mid-animation: a new {@code setTarget} captures the current eased value as the new
 * "from" and starts a fresh easing curve to the new target — so panel that was 60%
 * slid-out smoothly retracts when the user clicks away, instead of teleporting to 0
 * then animating back.
 *
 * <p>Time base is {@link System#nanoTime()} → frame-rate independent.
 */
public final class Animator {

    private double snapshot;
    private double target;
    private double durationMs;
    private Easing easing;
    private long startNanos;

    public Animator(double initial, double durationMs, Easing easing) {
        this.snapshot = initial;
        this.target = initial;
        this.durationMs = Math.max(1, durationMs);
        this.easing = easing == null ? Easing.LINEAR : easing;
        this.startNanos = System.nanoTime();
    }

    /** Convenience: animator that defaults to ease-out-expo at 250ms (the project standard). */
    public static Animator standard(double initial) {
        return new Animator(initial, 250, Easing.EASE_OUT_EXPO);
    }

    public Animator setTarget(double newTarget) {
        if (Double.compare(newTarget, this.target) == 0) return this;
        this.snapshot = getValue();   // capture current animated position
        this.target = newTarget;
        this.startNanos = System.nanoTime();
        return this;
    }

    /** Jump immediately to the target without easing. Useful on first show. */
    public Animator snapTo(double v) {
        this.snapshot = v;
        this.target = v;
        this.startNanos = System.nanoTime();
        return this;
    }

    public Animator setDuration(double durationMs) {
        this.durationMs = Math.max(1, durationMs);
        return this;
    }

    public Animator setEasing(Easing easing) {
        this.easing = easing == null ? Easing.LINEAR : easing;
        return this;
    }

    /** Eased current value (call once per frame). */
    public double getValue() {
        double elapsed = (System.nanoTime() - startNanos) / 1_000_000.0;
        if (elapsed >= durationMs) return target;
        double t = elapsed / durationMs;
        return snapshot + (target - snapshot) * easing.apply(t);
    }

    public double getTarget() { return target; }

    public boolean isAnimating() {
        return Math.abs(getValue() - target) > 0.001;
    }

    /** Cast to float for OpenGL / pixel maths. */
    public float floatValue() { return (float) getValue(); }
}
