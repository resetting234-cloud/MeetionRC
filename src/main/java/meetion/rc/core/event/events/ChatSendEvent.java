package meetion.rc.core.event.events;

import meetion.rc.core.event.Event;

public class ChatSendEvent extends Event {
    private String message;

    public ChatSendEvent(String message) {
        this.message = message;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
