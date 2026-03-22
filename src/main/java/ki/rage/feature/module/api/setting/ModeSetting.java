package ki.rage.feature.module.api.setting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModeSetting extends Setting<String> {
    private final List<String> modes;

    public ModeSetting(String name, String value, List<String> modes) {
        super(name, value);
        this.modes = Collections.unmodifiableList(new ArrayList<>(modes));
        if (!this.modes.contains(value) && !this.modes.isEmpty()) {
            setValue(this.modes.getFirst());
        }
    }

    public List<String> modes() {
        return modes;
    }

    public void next() {
        if (modes.isEmpty()) {
            return;
        }
        int idx = modes.indexOf(value());
        if (idx < 0) {
            setValue(modes.getFirst());
            return;
        }
        setValue(modes.get((idx + 1) % modes.size()));
    }
}

