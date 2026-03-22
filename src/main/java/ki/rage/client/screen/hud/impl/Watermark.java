package ki.rage.client.screen.hud.impl;

import ki.rage.client.Client;
import ki.rage.client.screen.hud.api.HudElement;
import ki.rage.client.util.render.Render2D;
import ki.rage.client.util.render.color.ColorUtil;
import ki.rage.feature.module.impl.render.Interface;

public class Watermark extends HudElement {
    public Watermark() {
        super("Watermark");
    }

    @Override
    public void render() {
        Interface interfaceModule = Client.getInstance().getModuleManager().get(Interface.class);
        if (interfaceModule == null) {
            return;
        }

        String text = Client.getInstance().getClientName() + " " + Client.getInstance().getClientVersion();
        ColorUtil color = interfaceModule.firstColor.color();

        Render2D.text(Render2D.opensans(12), text, 4, 0, 12, color);
    }
}
