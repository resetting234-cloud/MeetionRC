package meetion.rc.core.event.events;

import meetion.rc.core.event.Event;

public class MotionEvent extends Event {
    private float yaw;
    private float pitch;
    private boolean onGround;

    public MotionEvent(float yaw, float pitch, boolean onGround, Era era) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
        setEra(era);
    }

    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
    public boolean isOnGround() { return onGround; }
    public void setYaw(float yaw) { this.yaw = yaw; }
    public void setPitch(float pitch) { this.pitch = pitch; }
    public void setOnGround(boolean onGround) { this.onGround = onGround; }
}
