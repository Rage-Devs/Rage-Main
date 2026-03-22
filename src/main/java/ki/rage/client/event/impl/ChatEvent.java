package ki.rage.client.event.impl;

import ki.rage.client.event.api.Event;
import lombok.Getter;

@Getter
public class ChatEvent extends Event {
    private final String message;

    public ChatEvent(String message) {
        this.message = message;
    }
}
