package ki.rage.feature.module.api.setting;

public class BooleanSetting extends Setting<Boolean> {
    public BooleanSetting(String name, boolean value) {
        super(name, value);
    }

    public boolean enabled() {
        return value();
    }

    public void toggle() {
        setValue(!value());
    }
}

