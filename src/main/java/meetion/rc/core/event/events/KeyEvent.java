package meetion.rc.core.event.events;

import meetion.rc.core.event.Event;

public class KeyEvent extends Event {
    private final int key;
    private final int action;

    public KeyEvent(int key, int action) {
        this.key = key;
        this.action = action;
    }

    public int getKey() { return key; }
    public int getAction() { return action; }
}
