package meetion.rc.core.event;

public class Event {

    private boolean cancelled;
    private Era era = Era.PRE;

    public boolean isCancelled() { return cancelled; }
    public void cancel() { this.cancelled = true; }

    public Era getEra() { return era; }
    public void setEra(Era era) { this.era = era; }

    public enum Era {
        PRE, POST
    }
}
