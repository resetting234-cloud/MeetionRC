package meetion.rc.ui.animation;

/**
 * One-shot timed transition from {@link #from} to {@link #to} over {@link #durationMs}
 * applying an {@link Easing}. Strictly time-based — uses {@link System#nanoTime()} so
 * the animation duration is independent of frame-rate (240 fps and 30 fps look identical).
 *
 * <p>Use {@link Animator} instead when you need a value that smoothly chases a moving
 * target (hover state, slide-out panel, selected-tab indicator).
 */
public final class Animation {

    private final double from;
    private final double to;
    private final double durationMs;
    private final Easing easing;
    private long startNanos;

    public Animation(double from, double to, double durationMs, Easing easing) {
        this.from = from;
        this.to = to;
        this.durationMs = Math.max(1, durationMs);
        this.easing = easing == null ? Easing.LINEAR : easing;
        this.startNanos = System.nanoTime();
    }

    /** Restart the animation from the beginning (does not change endpoints or duration). */
    public Animation restart() {
        this.startNanos = System.nanoTime();
        return this;
    }

    /** Current eased value at this exact moment. */
    public double getValue() {
        return from + (to - from) * easedProgress();
    }

    /** Raw normalised progress in [0, 1]. */
    public double progress() {
        double elapsed = (System.nanoTime() - startNanos) / 1_000_000.0;
        if (elapsed <= 0) return 0;
        if (elapsed >= durationMs) return 1;
        return elapsed / durationMs;
    }

    /** Eased progress in [0, 1]. */
    public double easedProgress() {
        return easing.apply(progress());
    }

    public boolean isFinished() {
        return (System.nanoTime() - startNanos) / 1_000_000.0 >= durationMs;
    }

    public double getFrom() { return from; }
    public double getTo() { return to; }
    public double getDurationMs() { return durationMs; }
}
