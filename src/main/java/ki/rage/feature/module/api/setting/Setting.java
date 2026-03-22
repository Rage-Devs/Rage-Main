package ki.rage.feature.module.api.setting;

import java.util.function.BooleanSupplier;

public abstract class Setting<T> {
    private final String name;
    private T value;
    private boolean visible = true;
    private BooleanSupplier visibilitySupplier;

    protected Setting(String name, T value) {
        this.name = name;
        this.value = value;
    }

    public final String name() {
        return name;
    }

    public final T value() {
        return value;
    }

    public final void setValue(T value) {
        this.value = value;
    }

    public final boolean visible() {
        if (visibilitySupplier != null) {
            return visibilitySupplier.getAsBoolean();
        }
        return visible;
    }

    @SuppressWarnings("unchecked")
    public final <S extends Setting<T>> S setVisible(boolean visible) {
        this.visible = visible;
        this.visibilitySupplier = null;
        return (S) this;
    }

    @SuppressWarnings("unchecked")
    public final <S extends Setting<T>> S setVisible(BooleanSupplier supplier) {
        this.visibilitySupplier = supplier;
        return (S) this;
    }
}

