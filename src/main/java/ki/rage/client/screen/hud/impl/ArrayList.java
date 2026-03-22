package ki.rage.client.screen.hud.impl;

import ki.rage.client.Client;
import ki.rage.client.screen.hud.api.HudElement;
import ki.rage.client.util.render.Render2D;
import ki.rage.client.util.render.color.ColorUtil;
import ki.rage.feature.module.api.Module;
import ki.rage.feature.module.impl.Interface;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ArrayList extends HudElement {
    public ArrayList() {
        super("ArrayList");
    }

    @Override
    public void render() {
        Interface interfaceModule = Client.getInstance().getModuleManager().get(Interface.class);
        if (interfaceModule == null) {
            return;
        }

        List<Module> enabledModules = Client.getInstance().getModuleManager().all().stream()
                .filter(Module::enabled)
                .sorted(Comparator.comparingDouble(m -> -Render2D.textWidth(Render2D.opensans(9), m.name(), 9)))
                .collect(Collectors.toList());

        float y = 0;
        float screenWidth = Render2D.width();
        ColorUtil color = interfaceModule.firstColor.color();

        for (Module module : enabledModules) {
            String text = module.name();
            float textWidth = Render2D.textWidth(Render2D.opensans(9), text, 9);
            float x = screenWidth - textWidth - 5;

            Render2D.text(Render2D.opensans(9), text, x, y, 9, color);

            y += 10;
        }
    }
}
