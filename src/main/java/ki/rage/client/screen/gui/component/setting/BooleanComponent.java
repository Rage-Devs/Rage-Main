package ki.rage.client.screen.gui.component.setting;

import ki.rage.client.screen.gui.component.AbstractComponent;
import ki.rage.client.util.render.Render2D;
import ki.rage.client.util.render.animation.Animation;
import ki.rage.client.util.render.color.ColorUtil;
import ki.rage.feature.module.api.setting.BooleanSetting;

public class BooleanComponent extends AbstractComponent {
    private final BooleanSetting setting;
    private final Animation hoverAnimation = new Animation(0f);
    private final Animation enabledAnimation = new Animation(0f);
    
    public BooleanComponent(BooleanSetting setting) {
        this.setting = setting;
        enabledAnimation.snap(setting.enabled() ? 1f : 0f);
    }
    
    @Override
    public void update(float dt) {
        enabledAnimation.setTarget(setting.enabled() ? 1f : 0f);
        enabledAnimation.update(dt);
        hoverAnimation.update(dt);
    }
    
    @Override
    public void render(float x, float y, float width, float height, float mouseX, float mouseY, ColorUtil theme, float alpha) {
        boolean hovered = isHovered(x, y, width, height, mouseX, mouseY);
        hoverAnimation.setTarget(hovered ? 1f : 0f);
        
        float hoverAlpha = 80f * hoverAnimation.value();
        ColorUtil disabledColor = ColorUtil.rgba((int) hoverAlpha, (int) hoverAlpha, (int) hoverAlpha, (int) (hoverAlpha * alpha));
        float themeAlpha = (100f + hoverAlpha) / 255f * alpha;
        ColorUtil enabledColor = ColorUtil.rgba(theme.r(), theme.g(), theme.b(), themeAlpha);
        ColorUtil backgroundColor = ColorUtil.lerp(disabledColor, enabledColor, enabledAnimation.value());
        
        Render2D.roundedRect(x, y, width, height, 0f, backgroundColor);
        Render2D.text(Render2D.opensans(), setting.name(), x + PADDING, y + TEXT_OFFSET_Y - 2.5f, TEXT_SIZE,
            ColorUtil.rgba(255, 255, 255, (int) (255 * alpha)));
    }
    
    @Override
    public boolean mouseClicked(float x, float y, float width, float height, float mouseX, float mouseY, int button) {
        if (button == 0 && isHovered(x, y, width, height, mouseX, mouseY)) {
            setting.toggle();
            return true;
        }
        return false;
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
