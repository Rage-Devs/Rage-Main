package ki.rage.client.screen.gui.component.setting;

import ki.rage.client.screen.gui.component.AbstractComponent;
import ki.rage.client.util.render.Render2D;
import ki.rage.client.util.render.animation.Animation;
import ki.rage.client.util.render.color.ColorUtil;
import ki.rage.feature.module.api.setting.SliderSetting;

import java.util.Locale;

public class SliderComponent extends AbstractComponent {
    private final SliderSetting setting;
    private final Animation hoverAnimation = new Animation(0f);
    private boolean dragging;
    private float sliderX;
    private float sliderWidth;
    
    public SliderComponent(SliderSetting setting) {
        this.setting = setting;
    }
    
    @Override
    public void update(float dt) {
        hoverAnimation.update(dt);
    }
    
    @Override
    public void render(float x, float y, float width, float height, float mouseX, float mouseY, ColorUtil theme, float alpha) {
        boolean hovered = isHovered(x, y, width, height, mouseX, mouseY);
        hoverAnimation.setTarget(hovered ? 1f : 0f);
        
        float percentage = (float) setting.percent();
        String valueText = formatValue(setting.get());
        float labelWidth = Render2D.textWidth(Render2D.opensans(), setting.name(), TEXT_SIZE);
        
        float hoverAlpha = 80f * hoverAnimation.value();
        ColorUtil baseColor = ColorUtil.rgba((int) hoverAlpha, (int) hoverAlpha, (int) hoverAlpha, 
            (int) ((51 + hoverAlpha) * alpha));
        Render2D.roundedRect(x, y, width, height, 0f, baseColor);
        
        float filledWidth = width * percentage;
        if (filledWidth > 0.5f) {
            float themeAlpha = (100f + hoverAlpha) / 255f * alpha;
            Render2D.roundedRect(x, y, filledWidth, height, 0f, 
                ColorUtil.rgba(theme.r(), theme.g(), theme.b(), themeAlpha));
        }
        
        Render2D.text(Render2D.opensans(), setting.name(), x + PADDING - 2, y + TEXT_OFFSET_Y - 2.5f, TEXT_SIZE, 
            ColorUtil.rgba(255, 255, 255, (int) (255 * alpha)));
        Render2D.text(Render2D.opensans(), " " + valueText, x + PADDING + labelWidth, y + TEXT_OFFSET_Y - 2.5f, TEXT_SIZE, 
            ColorUtil.rgba(170, 170, 170, (int) (255 * alpha)));
        
        sliderX = x;
        sliderWidth = width;
    }
    
    @Override
    public boolean mouseClicked(float x, float y, float width, float height, float mouseX, float mouseY, int button) {
        if (button == 0 && isHovered(x, y, width, height, mouseX, mouseY)) {
            dragging = true;
            updateValue(mouseX);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean mouseReleased(int button) {
        if (button == 0 && dragging) {
            dragging = false;
            return true;
        }
        return false;
    }
    
    @Override
    public boolean mouseDragged(float mouseX, float mouseY, int button) {
        if (button == 0 && dragging) {
            updateValue(mouseX);
            return true;
        }
        return false;
    }
    
    private void updateValue(float mouseX) {
        float percentage = (mouseX - sliderX) / sliderWidth;
        percentage = Math.max(0f, Math.min(1f, percentage));
        setting.set(setting.min() + (setting.max() - setting.min()) * percentage);
    }
    
    private String formatValue(double value) {
        if (Math.abs(value - Math.round(value)) < 1e-6) {
            return Long.toString(Math.round(value));
        }
        return String.format(Locale.ROOT, "%.2f", value);
    }
    
    @Override
    public float getHeight(float width) {
        return 14f;
    }
    
    @Override
    public boolean isVisible() {
        return setting.visible();
    }
}
