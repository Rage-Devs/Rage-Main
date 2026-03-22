package ki.rage.feature.module.impl;

import ki.rage.feature.module.api.Category;
import ki.rage.feature.module.api.Module;
import ki.rage.feature.module.api.setting.ColorSetting;
import ki.rage.feature.module.api.setting.MultiModeSetting;

import java.util.List;

public class Interface extends Module {
    public final ColorSetting firstColor = register(new ColorSetting("First Color", 143, 178, 255, 255));
    public final MultiModeSetting hudElements = register(new MultiModeSetting("Elements",
            List.of("Watermark", "ArrayList", "Info"), 
            "Watermark", "ArrayList", "Info"));

    public Interface() {
        super("Interface", Category.Render);
        setEnabled(true);
    }
}
