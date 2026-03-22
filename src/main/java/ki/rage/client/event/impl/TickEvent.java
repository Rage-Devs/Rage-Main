package ki.rage.client.event.impl;

import ki.rage.client.event.api.Event;

public class TickEvent extends Event {
    public static class Pre extends TickEvent {}
    public static class Post extends TickEvent {}
}
