package ki.rage.client.screen.gui.component.setting;

import ki.rage.client.screen.gui.component.Component;
import ki.rage.feature.module.api.setting.BooleanSetting;
import ki.rage.feature.module.api.setting.ColorSetting;
import ki.rage.feature.module.api.setting.ModeSetting;
import ki.rage.feature.module.api.setting.MultiModeSetting;
import ki.rage.feature.module.api.setting.SliderSetting;
import ki.rage.feature.module.api.setting.Setting;

public class SettingComponentFactory {
    private SettingComponentFactory() {
    }
    
    public static Component create(Setting<?> setting) {
        if (setting instanceof BooleanSetting booleanSetting) {
            return new BooleanComponent(booleanSetting);
        }
        if (setting instanceof SliderSetting sliderSetting) {
            return new SliderComponent(sliderSetting);
        }
        if (setting instanceof ModeSetting modeSetting) {
            return new ModeComponent(modeSetting);
        }
        if (setting instanceof ColorSetting colorSetting) {
            return new ColorComponent(colorSetting);
        }
        if (setting instanceof MultiModeSetting multiModeSetting) {
            return new MultiModeComponent(multiModeSetting);
        }
        return new UnknownComponent(setting);
    }
}
