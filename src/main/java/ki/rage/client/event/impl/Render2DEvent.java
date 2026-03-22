package ki.rage.client.event.impl;

import ki.rage.client.event.api.Event;

public class Render2DEvent extends Event {
    private final int framebufferWidth;
    private final int framebufferHeight;
    private final int scaleFactor;

    public Render2DEvent(int framebufferWidth, int framebufferHeight, int scaleFactor) {
        this.framebufferWidth = framebufferWidth;
        this.framebufferHeight = framebufferHeight;
        this.scaleFactor = scaleFactor;
    }

    public int framebufferWidth() {
        return framebufferWidth;
    }

    public int framebufferHeight() {
        return framebufferHeight;
    }

    public int scaleFactor() {
        return scaleFactor;
    }
}
