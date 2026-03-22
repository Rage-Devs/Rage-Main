package ki.rage.client.screen.hud.impl;

import ki.rage.client.Client;
import ki.rage.client.screen.hud.api.HudElement;
import ki.rage.client.util.render.Render2D;
import ki.rage.client.util.render.color.ColorUtil;
import ki.rage.feature.module.impl.Interface;

public class Info extends HudElement {
    public Info() {
        super("Info");
    }

    @Override
    public void render() {
        Interface interfaceModule = Client.getInstance().getModuleManager().get(Interface.class);
        if (interfaceModule == null || mc.player == null) {
            return;
        }

        ColorUtil color = interfaceModule.firstColor.color();
        float y = Render2D.height() - 38;

        int fps = mc.getCurrentFps();
        String fpsText = String.format("FPS: %d", fps);
        Render2D.text(Render2D.opensans(9), fpsText, 5, y, 9, color);

        y += 10;

        int x = (int) mc.player.getX();
        int yPos = (int) mc.player.getY();
        int z = (int) mc.player.getZ();
        String xyz = String.format("XYZ: %d, %d, %d", x, yPos, z);
        Render2D.text(Render2D.opensans(9), xyz, 5, y, 9, color);
    }
}
