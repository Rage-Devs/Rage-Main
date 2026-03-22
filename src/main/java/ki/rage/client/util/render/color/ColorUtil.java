package ki.rage.client.util.render.color;

import ki.rage.client.Client;
import ki.rage.feature.module.impl.render.Interface;

public final class ColorUtil {
    private static final ColorUtil DEFAULT_THEME = rgba(120, 160, 255, 255);

    private final float r;
    private final float g;
    private final float b;
    private final float a;

    private ColorUtil(float r, float g, float b, float a) {
        this.r = clamp(r);
        this.g = clamp(g);
        this.b = clamp(b);
        this.a = clamp(a);
    }

    public float r() { return r; }
    public float g() { return g; }
    public float b() { return b; }
    public float a() { return a; }

    public static ColorUtil rgba(int r, int g, int b, int a) {
        return new ColorUtil(r / 255f, g / 255f, b / 255f, a / 255f);
    }

    public static ColorUtil rgba(int r, int g, int b) {
        return rgba(r, g, b, 255);
    }

    public static ColorUtil rgba(float r, float g, float b, float a) {
        return new ColorUtil(r, g, b, a);
    }

    public static ColorUtil rgba(float r, float g, float b) {
        return new ColorUtil(r, g, b, 1f);
    }

    public static ColorUtil fromARGB(int argb) {
        return rgba(
            (argb >> 16) & 0xFF,
            (argb >> 8) & 0xFF,
            argb & 0xFF,
            (argb >> 24) & 0xFF
        );
    }

    public static ColorUtil hsv(float h, float s, float v, float a) {
        float hue = h - (float) Math.floor(h);
        float sat = clamp(s);
        float val = clamp(v);

        float c = val * sat;
        float x = c * (1f - Math.abs((hue * 6f) % 2f - 1f));
        float m = val - c;

        float r, g, b;
        float hp = hue * 6f;

        if (hp < 1f) {
            r = c; g = x; b = 0f;
        } else if (hp < 2f) {
            r = x; g = c; b = 0f;
        } else if (hp < 3f) {
            r = 0f; g = c; b = x;
        } else if (hp < 4f) {
            r = 0f; g = x; b = c;
        } else if (hp < 5f) {
            r = x; g = 0f; b = c;
        } else {
            r = c; g = 0f; b = x;
        }

        return new ColorUtil(r + m, g + m, b + m, a);
    }

    public static ColorUtil hsv(float h, float s, float v) {
        return hsv(h, s, v, 1f);
    }

    public static ColorUtil themeColor() {
        Interface interfaceModule = Client.getInstance().getModuleManager().get(Interface.class);
        if (interfaceModule != null) {
            return interfaceModule.firstColor.color();
        }
        return DEFAULT_THEME;
    }

    public static ColorUtil lerp(ColorUtil a, ColorUtil b, float t) {
        float clamped = clamp(t);
        return new ColorUtil(
            a.r + (b.r - a.r) * clamped,
            a.g + (b.g - a.g) * clamped,
            a.b + (b.b - a.b) * clamped,
            a.a + (b.a - a.a) * clamped
        );
    }

    public ColorUtil withAlpha(float alpha) {
        return new ColorUtil(r, g, b, alpha);
    }

    public ColorUtil withAlpha(int alpha) {
        return withAlpha(alpha / 255f);
    }

    public ColorUtil brighter(float factor) {
        float f = clamp(factor);
        return new ColorUtil(
            r + (1f - r) * f,
            g + (1f - g) * f,
            b + (1f - b) * f,
            a
        );
    }

    public ColorUtil darker(float factor) {
        float f = clamp(factor);
        return new ColorUtil(r * (1f - f), g * (1f - f), b * (1f - f), a);
    }

    public int toARGB() {
        return ((int)(a * 255) << 24) |
               ((int)(r * 255) << 16) |
               ((int)(g * 255) << 8) |
               (int)(b * 255);
    }

    public int toRGBA() {
        return ((int)(r * 255) << 24) |
               ((int)(g * 255) << 16) |
               ((int)(b * 255) << 8) |
               (int)(a * 255);
    }

    public int rInt() { return (int)(r * 255); }
    public int gInt() { return (int)(g * 255); }
    public int bInt() { return (int)(b * 255); }
    public int aInt() { return (int)(a * 255); }

    private static float clamp(float value) {
        return Math.max(0f, Math.min(1f, value));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ColorUtil other)) return false;
        return Float.compare(r, other.r) == 0 &&
               Float.compare(g, other.g) == 0 &&
               Float.compare(b, other.b) == 0 &&
               Float.compare(a, other.a) == 0;
    }

    @Override
    public int hashCode() {
        int result = Float.hashCode(r);
        result = 31 * result + Float.hashCode(g);
        result = 31 * result + Float.hashCode(b);
        result = 31 * result + Float.hashCode(a);
        return result;
    }

    @Override
    public String toString() {
        return String.format("ColorUtil[r=%.2f, g=%.2f, b=%.2f, a=%.2f]", r, g, b, a);
    }
}
