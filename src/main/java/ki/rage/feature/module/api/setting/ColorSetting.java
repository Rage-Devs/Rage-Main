package ki.rage.feature.module.api.setting;

import ki.rage.client.util.render.color.ColorUtil;

public class ColorSetting extends Setting<ColorUtil> {
    private int r;
    private int g;
    private int b;
    private int a;

    public ColorSetting(String name, int r, int g, int b, int a) {
        super(name, ColorUtil.rgba(r, g, b, a));
        setRgba(r, g, b, a);
    }

    public ColorSetting(String name, int r, int g, int b) {
        this(name, r, g, b, 255);
    }

    public int r() {
        return r;
    }

    public int g() {
        return g;
    }

    public int b() {
        return b;
    }

    public int a() {
        return a;
    }

    public void setR(int r) {
        setRgba(r, g, b, a);
    }

    public void setG(int g) {
        setRgba(r, g, b, a);
    }

    public void setB(int b) {
        setRgba(r, g, b, a);
    }

    public void setA(int a) {
        setRgba(r, g, b, a);
    }

    public void setRgba(int r, int g, int b, int a) {
        this.r = clamp255(r);
        this.g = clamp255(g);
        this.b = clamp255(b);
        this.a = clamp255(a);
        setValue(ColorUtil.rgba(this.r, this.g, this.b, this.a));
    }

    public ColorUtil color() {
        return value();
    }

    private static int clamp255(int v) {
        return Math.max(0, Math.min(255, v));
    }
}

