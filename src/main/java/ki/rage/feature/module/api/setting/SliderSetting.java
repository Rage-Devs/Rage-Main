package ki.rage.feature.module.api.setting;

public class SliderSetting extends Setting<Double> {
    private final double min;
    private final double max;
    private final double step;

    public SliderSetting(String name, double value, double min, double max, double step) {
        super(name, value);
        this.min = min;
        this.max = max;
        this.step = step;
        set(value);
    }

    public double min() {
        return min;
    }

    public double max() {
        return max;
    }

    public double step() {
        return step;
    }

    public double get() {
        return value();
    }

    public void set(double value) {
        double clamped = Math.max(min, Math.min(max, value));
        if (step > 0) {
            clamped = Math.round(clamped / step) * step;
        }
        setValue(clamped);
    }

    public double percent() {
        double range = max - min;
        if (range <= 0) {
            return 0;
        }
        return (get() - min) / range;
    }
}

