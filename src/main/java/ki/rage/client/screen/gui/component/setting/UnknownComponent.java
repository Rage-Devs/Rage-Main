package ki.rage.client.screen.gui.component.setting;

import ki.rage.client.screen.gui.component.AbstractComponent;
import ki.rage.client.util.render.Render2D;
import ki.rage.client.util.render.color.ColorUtil;
import ki.rage.feature.module.api.setting.Setting;

public class UnknownComponent extends AbstractComponent {
    private final Setting<?> setting;
    
    public UnknownComponent(Setting<?> setting) {
        this.setting = setting;
    }
    
    @Override
    public void render(float x, float y, float width, float height, float mouseX, float mouseY, ColorUtil theme, float alpha) {
        Render2D.roundedRect(x, y, width, height, 0f, ColorUtil.rgba(0, 0, 0, (int) (55 * alpha)));
        Render2D.text(Render2D.opensans(), setting.name(), x + PADDING, y + TEXT_OFFSET_Y, 9.5f, 
            ColorUtil.rgba(255, 255, 255, (int) (255 * alpha)));
    }
    
    @Override
    public float getHeight(float width) {
        return 16f;
    }
    
    @Override
    public boolean isVisible() {
        return setting.visible();
    }
}
