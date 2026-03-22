package ki.rage.feature.module.api;

import ki.rage.client.Client;
import ki.rage.client.screen.gui.ClickGui;
import ki.rage.client.event.api.EventTarget;
import ki.rage.client.event.impl.KeyEvent;
import ki.rage.client.util.IMinecraft;
import ki.rage.feature.module.impl.movement.*;
import ki.rage.feature.module.impl.player.*;
import ki.rage.feature.module.impl.render.*;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModuleManager implements IMinecraft {
    private final List<Module> modules = new ArrayList<>();

    public ModuleManager() {
        Client.getInstance().getEventBus().register(this);
        register(
                new Interface(),
                new AutoSprint(),
                new NoSlow(),
                new FastPlace(),
                new Step()
        );
    }

    private void register(Module... modules) {
        Collections.addAll(this.modules, modules);
    }

    public List<Module> all() {
        return Collections.unmodifiableList(modules);
    }

    public List<Module> byCategory(Category category) {
        List<Module> out = new ArrayList<>();
        for (Module module : modules) {
            if (module.category() == category) {
                out.add(module);
            }
        }
        return out;
    }

    public <T extends Module> T get(Class<T> type) {
        for (Module module : modules) {
            if (type.isInstance(module)) {
                return type.cast(module);
            }
        }
        return null;
    }

    @EventTarget
    public void onKey(KeyEvent e) {
        if (e.getAction() != GLFW.GLFW_PRESS || mc.currentScreen != null) {
            return;
        }
        if (e.getKey() == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            mc.setScreen(new ClickGui());
            return;
        }
        for (Module module : modules) {
            if (module.key() == e.getKey()) {
                module.toggle();
            }
        }
    }
}
