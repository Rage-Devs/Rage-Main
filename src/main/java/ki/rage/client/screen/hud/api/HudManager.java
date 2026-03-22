package ki.rage.client.screen.hud.api;

import ki.rage.client.Client;
import ki.rage.client.event.api.EventTarget;
import ki.rage.client.event.impl.Render2DEvent;
import ki.rage.client.screen.hud.impl.ArrayList;
import ki.rage.client.screen.hud.impl.Info;
import ki.rage.client.screen.hud.impl.Watermark;
import ki.rage.feature.module.impl.render.Interface;

import java.util.List;

public class HudManager {
    private final List<HudElement> elements;

    public HudManager() {
        Client.getInstance().getEventBus().register(this);
        elements = List.of(
                new Watermark(),
                new ArrayList(),
                new Info()
        );
    }

    @EventTarget
    public void onRender2D(Render2DEvent e) {
        Interface interfaceModule = Client.getInstance().getModuleManager().get(Interface.class);
        if (interfaceModule == null || !interfaceModule.enabled()) {
            return;
        }

        for (HudElement element : elements) {
            if (interfaceModule.hudElements.isEnabled(element.name())) {
                element.render();
            }
        }
    }
}
