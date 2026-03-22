package ki.rage.client.screen.gui.component.setting;

import ki.rage.client.screen.gui.component.AbstractComponent;
import ki.rage.client.util.render.Render2D;
import ki.rage.client.util.render.animation.Animation;
import ki.rage.client.util.render.color.ColorUtil;
import ki.rage.feature.module.api.setting.ColorSetting;
import org.lwjgl.glfw.GLFW;

public class ColorComponent extends AbstractComponent {
    private static final String HUE_TEXTURE = "assets/rage/images/hue.png";
    private static final float ROW_HEIGHT = 16f;
    private static final float HUE_BAR_HEIGHT = 8f;
    private static final float PICKER_PADDING = 4f;
    
    private final ColorSetting setting;
    private final Animation expandAnimation;
    private final Animation hoverAnimation;
    
    private boolean expanded;
    private DragMode dragMode = DragMode.NONE;
    
    private float hueBarX, hueBarY, hueBarWidth, hueBarHeight;
    private float svSquareX, svSquareY, svSquareWidth, svSquareHeight;
    
    private float cachedHue;
    private float cachedSaturation;
    private float cachedBrightness;
    
    public ColorComponent(ColorSetting setting) {
        this.setting = setting;
        this.expandAnimation = new Animation(0f);
        this.hoverAnimation = new Animation(0f);
        updateHsvCache();
    }
    
    @Override
    public void update(float dt) {
        expandAnimation.setTarget(expanded ? 1f : 0f);
        expandAnimation.update(dt);
        hoverAnimation.update(dt);
    }
    
    @Override
    public void render(float x, float y, float width, float height, float mouseX, float mouseY, ColorUtil theme, float alpha) {
        renderMainRow(x, y, width, mouseX, mouseY, theme, alpha);
        
        if (expandAnimation.value() > 0.001f) {
            renderColorPicker(x, y + ROW_HEIGHT, width, mouseX, mouseY, alpha);
        }
    }
    
    @Override
    public boolean mouseClicked(float x, float y, float width, float height, float mouseX, float mouseY, int button) {
        if (isHovered(x, y, width, ROW_HEIGHT, mouseX, mouseY) && button == 1) {
            expanded = !expanded;
            dragMode = DragMode.NONE;
            return true;
        }
        
        if (expandAnimation.value() > 0.01f && button == 0) {
            return handlePickerClick(mouseX, mouseY);
        }
        
        return false;
    }
    
    @Override
    public boolean mouseReleased(int button) {
        if (button == 0 && dragMode != DragMode.NONE) {
            dragMode = DragMode.NONE;
            return true;
        }
        return false;
    }
    
    @Override
    public boolean mouseDragged(float mouseX, float mouseY, int button) {
        if (button == 0 && dragMode != DragMode.NONE) {
            handlePickerDrag(mouseX, mouseY);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean keyPressed(int keyCode) {
        if (expanded && keyCode == GLFW.GLFW_KEY_ESCAPE) {
            expanded = false;
            dragMode = DragMode.NONE;
            return true;
        }
        return false;
    }
    
    @Override
    public float getHeight(float width) {
        float pickerWidth = width - PICKER_PADDING * 2f;
        float pickerFullHeight = PICKER_PADDING + HUE_BAR_HEIGHT + PICKER_PADDING + pickerWidth + PICKER_PADDING;
        return ROW_HEIGHT + (pickerFullHeight * expandAnimation.value());
    }
    
    @Override
    public boolean isVisible() {
        return setting.visible();
    }
    
    private void renderMainRow(float x, float y, float width, float mouseX, float mouseY, ColorUtil theme, float alpha) {
        boolean hovered = isHovered(x, y, width, ROW_HEIGHT, mouseX, mouseY);
        hoverAnimation.setTarget(hovered ? 1f : 0f);
        
        float hoverBoost = 80f * hoverAnimation.value();
        ColorUtil baseColor = ColorUtil.rgba(
            (int) hoverBoost, 
            (int) hoverBoost, 
            (int) hoverBoost, 
            (int) ((51 + hoverBoost) * alpha)
        );
        Render2D.roundedRect(x, y, width, ROW_HEIGHT, 0f, baseColor);
        
        Render2D.text(Render2D.opensans(), setting.name(), x + PADDING, y + TEXT_OFFSET_Y - 2, TEXT_SIZE - 2,
            ColorUtil.rgba(255, 255, 255, (int) (255 * alpha)));
        
        renderColorPreview(x, y, width, alpha);
    }
    
    private void renderColorPreview(float x, float y, float width, float alpha) {
        float previewSize = 10f;
        ColorUtil color = setting.color();
        float previewX = x + width - previewSize - 4f;
        float previewY = y + 3f;
        
        Render2D.roundedRect(previewX, previewY, previewSize, previewSize, 0f, 
            ColorUtil.rgba(color.r(), color.g(), color.b(), alpha));
    }
    
    private void renderColorPicker(float x, float startY, float width, float mouseX, float mouseY, float alpha) {
        float pickerX = x + PICKER_PADDING;
        float pickerWidth = width - PICKER_PADDING * 2f;
        float renderAlpha = alpha * expandAnimation.value();
        
        float currentY = startY + PICKER_PADDING;
        
        renderHueBar(pickerX, currentY, pickerWidth, renderAlpha);
        currentY += HUE_BAR_HEIGHT + PICKER_PADDING;
        
        renderSaturationBrightnessSquare(pickerX, currentY, pickerWidth, renderAlpha);
    }
    
    private void renderHueBar(float x, float y, float width, float alpha) {
        hueBarX = x;
        hueBarY = y;
        hueBarWidth = width;
        hueBarHeight = HUE_BAR_HEIGHT;
        
        Render2D.imageRounded(HUE_TEXTURE, hueBarX, hueBarY, hueBarWidth, hueBarHeight, 0f,
            ColorUtil.rgba(255, 255, 255, (int) (255 * alpha)));
        
        float hueIndicatorX = hueBarX + hueBarWidth * cachedHue;
        Render2D.roundedRectOutline(hueIndicatorX - 1f, hueBarY - 1f, 2f, hueBarHeight + 2f, 0f, 1f, 
            ColorUtil.rgba(0, 0, 0, (int) (255 * alpha)));
    }
    
    private void renderSaturationBrightnessSquare(float x, float y, float width, float alpha) {
        svSquareX = x;
        svSquareY = y;
        svSquareWidth = width;
        svSquareHeight = width;
        
        Render2D.hsvSquare(svSquareX, svSquareY, svSquareWidth, svSquareHeight, cachedHue, alpha);
        Render2D.roundedRectOutline(svSquareX, svSquareY, svSquareWidth, svSquareHeight, 0f, 1f, 
            ColorUtil.rgba(0, 0, 0, (int) (200 * alpha)));
        
        float svIndicatorX = svSquareX + svSquareWidth * cachedSaturation;
        float svIndicatorY = svSquareY + svSquareHeight * (1f - cachedBrightness);
        
        Render2D.roundedRectOutline(svIndicatorX - 2f, svIndicatorY - 2f, 4f, 4f, 0f, 1f, 
            ColorUtil.rgba(0, 0, 0, (int) (255 * alpha)));
        Render2D.roundedRectOutline(svIndicatorX - 1f, svIndicatorY - 1f, 2f, 2f, 0f, 1f, 
            ColorUtil.rgba(255, 255, 255, (int) (255 * alpha)));
    }
    
    private boolean handlePickerClick(float mouseX, float mouseY) {
        if (isHovered(hueBarX, hueBarY, hueBarWidth, hueBarHeight, mouseX, mouseY)) {
            dragMode = DragMode.HUE;
            updateHue(mouseX);
            return true;
        }
        
        if (isHovered(svSquareX, svSquareY, svSquareWidth, svSquareHeight, mouseX, mouseY)) {
            dragMode = DragMode.SATURATION_BRIGHTNESS;
            updateSaturationBrightness(mouseX, mouseY);
            return true;
        }
        
        return false;
    }
    
    private void handlePickerDrag(float mouseX, float mouseY) {
        if (dragMode == DragMode.HUE) {
            updateHue(mouseX);
        } else if (dragMode == DragMode.SATURATION_BRIGHTNESS) {
            updateSaturationBrightness(mouseX, mouseY);
        }
    }
    
    private void updateHue(float mouseX) {
        cachedHue = clamp((mouseX - hueBarX) / hueBarWidth);
        applyHsvToRgb();
    }
    
    private void updateSaturationBrightness(float mouseX, float mouseY) {
        cachedSaturation = clamp((mouseX - svSquareX) / svSquareWidth);
        cachedBrightness = 1f - clamp((mouseY - svSquareY) / svSquareHeight);
        applyHsvToRgb();
    }
    
    private void updateHsvCache() {
        int r = setting.r();
        int g = setting.g();
        int b = setting.b();
        
        float rf = r / 255f;
        float gf = g / 255f;
        float bf = b / 255f;
        
        float max = Math.max(rf, Math.max(gf, bf));
        float min = Math.min(rf, Math.min(gf, bf));
        float delta = max - min;
        
        cachedBrightness = max;
        cachedSaturation = max == 0f ? 0f : delta / max;
        
        if (delta == 0f) {
            cachedHue = 0f;
        } else {
            if (max == rf) {
                cachedHue = ((gf - bf) / delta) / 6f;
            } else if (max == gf) {
                cachedHue = (2f + (bf - rf) / delta) / 6f;
            } else {
                cachedHue = (4f + (rf - gf) / delta) / 6f;
            }
            if (cachedHue < 0f) cachedHue += 1f;
        }
    }
    
    private void applyHsvToRgb() {
        float h = cachedHue;
        float s = cachedSaturation;
        float v = cachedBrightness;
        
        float c = v * s;
        float x = c * (1f - Math.abs((h * 6f) % 2f - 1f));
        float m = v - c;
        
        float r, g, b;
        float hp = h * 6f;
        
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
        
        setting.setRgba(
            (int)((r + m) * 255),
            (int)((g + m) * 255),
            (int)((b + m) * 255),
            setting.a()
        );
    }
    
    private float clamp(float value) {
        return Math.max(0f, Math.min(1f, value));
    }
    
    private enum DragMode {
        NONE,
        HUE,
        SATURATION_BRIGHTNESS
    }
}
