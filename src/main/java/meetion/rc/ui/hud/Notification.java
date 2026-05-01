package meetion.rc.ui.hud;

import meetion.rc.ui.animation.Animator;
import meetion.rc.ui.animation.Easing;

/**
 * One toast in the {@link NotificationManager} queue.
 *
 * <p>Lifecycle:
 * <ol>
 *   <li>Constructed at {@code now}; {@link #slide} animates 0 → 1 (slide-in).</li>
 *   <li>Stays visible until {@code now + durationMs}.</li>
 *   <li>200ms before expiry, {@link #slide} starts back to 0 (slide-out).</li>
 *   <li>Once {@link #isFinished()} returns true the manager removes it.</li>
 * </ol>
 */
public class Notification {

    public enum Type { INFO, SUCCESS, WARN, ERROR }

    private final String title;
    private final String body;
    private final Type type;
    private final long createdAt;
    private final long durationMs;
    final Animator slide;

    public Notification(String title, String body, Type type, long durationMs) {
        this.title = title == null ? "" : title;
        this.body  = body  == null ? "" : body;
        this.type  = type  == null ? Type.INFO : type;
        this.createdAt = System.currentTimeMillis();
        this.durationMs = Math.max(400, durationMs);
        this.slide = new Animator(0, 220, Easing.EASE_OUT_CUBIC);
        this.slide.setTarget(1);
    }

    public String getTitle()  { return title; }
    public String getBody()   { return body; }
    public Type   getType()   { return type; }

    /** [0..1] — 1 means "just appeared", 0 means "should expire". */
    public float timeRemaining() {
        long elapsed = System.currentTimeMillis() - createdAt;
        return (float) Math.max(0, Math.min(1, 1.0 - (double) elapsed / durationMs));
    }

    /** Drives slide-out 200ms before the timer fully expires. */
    void tick() {
        long elapsed = System.currentTimeMillis() - createdAt;
        if (elapsed > durationMs - 220 && slide.getTarget() > 0) {
            slide.setTarget(0);
        }
    }

    boolean isFinished() {
        long elapsed = System.currentTimeMillis() - createdAt;
        return elapsed > durationMs && slide.getValue() < 0.005;
    }
}
