package ki.rage.client.screen.hud.api;

import ki.rage.client.util.IMinecraft;

public abstract class HudElement implements IMinecraft {
    private final String name;

    protected HudElement(String name) {
        this.name = name;
    }

    public final String name() {
        return name;
    }

    public abstract void render();
}
