package ki.rage.feature.module.api;

import ki.rage.client.Client;
import ki.rage.client.util.IMinecraft;
import ki.rage.feature.module.api.setting.Setting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Module implements IMinecraft {
    private final String name;
    private final Category category;
    private int key;
    private boolean enabled;
    private final List<Setting<?>> settings = new ArrayList<>();

    protected Module(String name, Category category) {
        this.name = name;
        this.category = category;
    }

    public final String name() {
        return name;
    }

    public final Category category() {
        return category;
    }

    public final int key() {
        return key;
    }

    public final void setKey(int key) {
        this.key = key;
    }

    public final boolean enabled() {
        return enabled;
    }

    public final void toggle() {
        setEnabled(!enabled);
    }

    public final void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }
        this.enabled = enabled;
        if (enabled) {
            Client.getInstance().getEventBus().register(this);
            onEnable();
        } else {
            Client.getInstance().getEventBus().unregister(this);
            onDisable();
        }
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    protected final <T extends Setting<?>> T register(T setting) {
        settings.add(setting);
        return setting;
    }

    public final List<Setting<?>> settings() {
        return Collections.unmodifiableList(settings);
    }
}

