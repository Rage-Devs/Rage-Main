package ki.rage.client.util.render.manager;

import ki.rage.client.util.render.font.TtfFont;

import java.util.HashMap;
import java.util.Map;

public class FontManager {
    private final Map<String, FontCache> caches = new HashMap<>();

    public FontManager() {
        caches.put("opensans", new FontCache("assets/rage/fonts/opensans.ttf"));
        caches.put("montserrat", new FontCache("assets/rage/fonts/montserrat.ttf"));
        caches.put("pixel", new FontCache("assets/rage/fonts/pixel.ttf"));
    }

    public TtfFont opensans(float size, int scaleFactor) {
        int pixelHeight = Math.max(8, Math.round(size * scaleFactor));
        return caches.get("opensans").get(pixelHeight);
    }

    public TtfFont montserrat(float size, int scaleFactor) {
        int pixelHeight = Math.max(8, Math.round(size * scaleFactor));
        return caches.get("montserrat").get(pixelHeight);
    }

    public TtfFont pixel(float size, int scaleFactor) {
        int pixelHeight = Math.max(8, Math.round(size * scaleFactor));
        return caches.get("pixel").get(pixelHeight);
    }

    private static class FontCache {
        private final String resourcePath;
        private final Map<Integer, TtfFont> fonts = new HashMap<>();

        FontCache(String resourcePath) {
            this.resourcePath = resourcePath;
        }

        TtfFont get(int pixelHeight) {
            return fonts.computeIfAbsent(pixelHeight, h -> TtfFont.fromResource(resourcePath, h));
        }
    }
}
