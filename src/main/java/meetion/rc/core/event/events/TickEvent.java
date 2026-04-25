package meetion.rc.core.event.events;

import meetion.rc.core.event.Event;

public class TickEvent extends Event {
    public TickEvent(Era era) {
        setEra(era);
    }
}
