package ki.rage.feature.module.api.setting;

import java.util.*;

public class MultiModeSetting extends Setting<Set<String>> {
    private final List<String> modes;

    public MultiModeSetting(String name, List<String> modes, String... defaultEnabled) {
        super(name, new HashSet<>(Arrays.asList(defaultEnabled)));
        this.modes = Collections.unmodifiableList(new ArrayList<>(modes));
    }

    public List<String> modes() {
        return modes;
    }

    public boolean isEnabled(String mode) {
        return value().contains(mode);
    }

    public void toggle(String mode) {
        if (value().contains(mode)) {
            value().remove(mode);
        } else {
            value().add(mode);
        }
    }
}
