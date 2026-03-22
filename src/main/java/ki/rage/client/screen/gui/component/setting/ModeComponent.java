package ki.rage.client.screen.gui.component.setting;

import ki.rage.client.screen.gui.component.AbstractComponent;
import ki.rage.client.util.render.Render2D;
import ki.rage.client.util.render.animation.Animation;
import ki.rage.client.util.render.color.ColorUtil;
import ki.rage.feature.module.api.setting.ModeSetting;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class ModeComponent extends AbstractComponent {
    private final ModeSetting setting;
    private final Animation hoverAnimation = new Animation(0f);
    private final Animation expandAnimation = new Animation(0f);
    private boolean expanded;
    
    public ModeComponent(ModeSetting setting) {
        this.setting = setting;
    }
    
    @Override
    public void update(float dt) {
        hoverAnimation.update(dt);
        expandAnimation.setTarget(expanded ? 1f : 0f);
        expandAnimation.update(dt);
    }
    
    @Override
    public void render(float x, float y, float width, float height, float mouseX, float mouseY, ColorUtil theme, float alpha) {
        boolean hovered = isHovered(x, y, width, 14f, mouseX, mouseY);
        hoverAnimation.setTarget(hovered ? 1f : 0f);
        
        float hoverAlpha = 80f * hoverAnimation.value();
        float themeAlpha = (100f + hoverAlpha) / 255f * alpha;
        Render2D.roundedRect(x, y, width, 14f, 0f, 
            ColorUtil.rgba(theme.r(), theme.g(), theme.b(), themeAlpha));
        
        Render2D.text(Render2D.opensans(), setting.name(), x + PADDING, y + TEXT_OFFSET_Y - 3f, TEXT_SIZE,
            ColorUtil.rgba(255, 255, 255, (int) (255 * alpha)));
        
        float labelWidth = Render2D.textWidth(Render2D.opensans(), setting.name(), TEXT_SIZE);
        String value = setting.value();
        String abbreviation = abbreviate(value);
        String displayText = fitToWidth(abbreviation, width - labelWidth - PADDING * 3, TEXT_SIZE);
        float textWidth = Render2D.textWidth(Render2D.opensans(), displayText, TEXT_SIZE);
        float textX = x + width - PADDING - textWidth;
        
        if (!displayText.isEmpty()) {
            Render2D.text(Render2D.opensans(), displayText, textX + 1, y + TEXT_OFFSET_Y - 3f, TEXT_SIZE, 
                ColorUtil.rgba(170, 170, 170, (int) (255 * alpha)));
        }
        
        float expansionProgress = expandAnimation.value();
        if (expansionProgress <= 0.001f) {
            return;
        }
        
        List<String> modes = setting.modes();
        float currentY = y + 14f;
        float maxRows = modes.size() * expansionProgress;
        int fullRows = Math.min(modes.size(), (int) Math.floor(maxRows + 0.0001f));
        float partialRow = maxRows - fullRows;
        
        for (int i = 0; i < fullRows; i++) {
            renderOption(modes.get(i), x, currentY, width, 14f, mouseX, mouseY, theme, alpha);
            currentY += 14f;
        }
        
        if (fullRows < modes.size() && partialRow > 0.001f) {
            float partialHeight = 14f * partialRow;
            renderPartialOption(modes.get(fullRows), x, currentY, width, partialHeight, 14f, theme, alpha);
        }
    }
    
    private void renderOption(String mode, float x, float y, float width, float height, float mouseX, float mouseY, ColorUtil theme, float alpha) {
        boolean hovered = isHovered(x, y, width, height, mouseX, mouseY);
        boolean selected = mode.equals(setting.value());
        
        int baseAlpha = hovered ? 70 : 55;
        Render2D.roundedRect(x, y, width, height, 0f, ColorUtil.rgba(0, 0, 0, (int) (baseAlpha * alpha)));
        
        if (selected) {
            Render2D.roundedRect(x, y, width, height, 0f, 
                ColorUtil.rgba(theme.r(), theme.g(), theme.b(), 0.45f * alpha));
        }
        
        Render2D.text(Render2D.opensans(), mode, x + PADDING, y + TEXT_OFFSET_Y - 2.5f, TEXT_SIZE,
            ColorUtil.rgba(255, 255, 255, (int) (255 * alpha)));
    }
    
    private void renderPartialOption(String mode, float x, float y, float width, float height, float fullHeight, ColorUtil theme, float alpha) {
        boolean selected = mode.equals(setting.value());
        
        Render2D.roundedRect(x, y, width, height, 0f, ColorUtil.rgba(0, 0, 0, (int) (55 * alpha)));
        
        if (selected) {
            Render2D.roundedRect(x, y, width, height, 0f, 
                ColorUtil.rgba(theme.r(), theme.g(), theme.b(), 0.45f * alpha));
        }
        
        float textAlpha = Math.min(1f, height / fullHeight);
        Render2D.text(Render2D.opensans(), mode, x + PADDING - 2, y + TEXT_OFFSET_Y - 2.5f, TEXT_SIZE, 
            ColorUtil.rgba(255, 255, 255, (int) (255 * alpha * textAlpha)));
    }
    
    @Override
    public boolean mouseClicked(float x, float y, float width, float height, float mouseX, float mouseY, int button) {
        if (isHovered(x, y, width, 14f, mouseX, mouseY)) {
            if (button == 1) {
                expanded = !expanded;
                return true;
            }
            return true;
        }
        
        if (expandAnimation.value() > 0.01f && button == 0) {
            List<String> modes = setting.modes();
            float currentY = y + 14f;
            
            for (String mode : modes) {
                if (isHovered(x, currentY, width, 14f, mouseX, mouseY)) {
                    setting.setValue(mode);
                    expanded = false;
                    return true;
                }
                currentY += 14f;
            }
        }
        
        if (expanded && button == 1) {
            expanded = false;
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean keyPressed(int keyCode) {
        if (expanded && keyCode == GLFW.GLFW_KEY_ESCAPE) {
            expanded = false;
            return true;
        }
        return false;
    }
    
    @Override
    public float getHeight(float width) {
        int optionCount = setting.modes().size();
        return 14f + (optionCount * 14f * expandAnimation.value());
    }
    
    @Override
    public boolean isVisible() {
        return setting.visible();
    }
    
    private String abbreviate(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        String trimmed = text.trim();
        return trimmed.length() <= 3 ? trimmed : trimmed.substring(0, 3);
    }
    
    private String fitToWidth(String text, float maxWidth, float size) {
        if (text == null) {
            return "";
        }
        String result = text;
        while (!result.isEmpty() && Render2D.textWidth(Render2D.opensans(), result, size) > maxWidth) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }
}
