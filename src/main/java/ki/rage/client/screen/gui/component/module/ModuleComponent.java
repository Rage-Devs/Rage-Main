package ki.rage.client.screen.gui.component.module;

import ki.rage.client.screen.gui.component.AbstractComponent;
import ki.rage.client.screen.gui.component.Component;
import ki.rage.client.screen.gui.component.setting.SettingComponentFactory;
import ki.rage.client.util.render.Render2D;
import ki.rage.client.util.render.animation.Animation;
import ki.rage.client.util.render.color.ColorUtil;
import ki.rage.feature.module.api.Module;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ModuleComponent extends AbstractComponent {
    private final Module module;
    private final Animation enabledAnimation = new Animation(0f);
    private final Animation hoverAnimation = new Animation(0f);
    private final Animation expandAnimation = new Animation(0f);
    private final List<Component> settingComponents;
    private boolean expanded;
    private boolean binding;
    
    public ModuleComponent(Module module) {
        this.module = module;
        enabledAnimation.snap(module.enabled() ? 1f : 0f);
        settingComponents = new ArrayList<>();
        module.settings().forEach(s -> settingComponents.add(SettingComponentFactory.create(s)));
    }
    
    @Override
    public void update(float dt) {
        enabledAnimation.setTarget(module.enabled() ? 1f : 0f);
        enabledAnimation.update(dt);
        hoverAnimation.update(dt);
        expandAnimation.setTarget(expanded ? 1f : 0f);
        expandAnimation.update(dt);
        for (Component c : settingComponents) {
            c.update(dt);
        }
    }
    
    @Override
    public void render(float x, float y, float width, float height, float mouseX, float mouseY, ColorUtil theme, float alpha) {
        boolean hovered = isHovered(x, y, width, 14f, mouseX, mouseY);
        hoverAnimation.setTarget(hovered ? 1f : 0f);
        
        float enabledProgress = enabledAnimation.value();
        ColorUtil textColor = enabledProgress > 0.99f ? ColorUtil.rgba(255, 255, 255, 255) : ColorUtil.rgba(170, 170, 170, 255);
        
        String label;
        if (binding) {
            int key = module.key();
            if (key == 0) {
                label = "NONE";
            } else {
                label = org.lwjgl.glfw.GLFW.glfwGetKeyName(key, 0);
                if (label == null) {
                    label = "KEY" + key;
                }
                label = label.toUpperCase();
            }
        } else {
            label = module.name();
        }
        
        float textWidth = Render2D.textWidth(Render2D.opensans(), label, 7.5f);
        float textX = x + (width - textWidth) / 2f;
        
        Render2D.text(Render2D.opensans(), label, textX, y + 1.5f, 7.5f, 
            ColorUtil.rgba(textColor.r(), textColor.g(), textColor.b(), (int) (textColor.a() * alpha)));
        
        float expansionProgress = expandAnimation.value();
        if (expansionProgress <= 0f || settingComponents.isEmpty()) {
            return;
        }
        
        float currentY = y + 14f;
        for (Component c : settingComponents) {
            if (!c.isVisible()) {
                continue;
            }
            float h = c.getHeight(width) * expansionProgress;
            c.render(x, currentY, width, h, mouseX, mouseY, theme, expansionProgress);
            currentY += h;
        }
    }
    
    @Override
    public boolean mouseClicked(float x, float y, float width, float height, float mouseX, float mouseY, int button) {
        if (isHovered(x, y, width, 14f, mouseX, mouseY)) {
            if (button == 0 && !binding) {
                module.toggle();
                return true;
            }
            if (button == 1) {
                expanded = !expanded;
                return true;
            }
            if (button == 2) {
                binding = true;
                return true;
            }
        }
        
        if (expandAnimation.value() > 0.01f) {
            float currentY = y + 14f;
            for (Component c : settingComponents) {
                if (!c.isVisible()) {
                    continue;
                }
                float h = c.getHeight(width) * expandAnimation.value();
                if (c.mouseClicked(x, currentY, width, h, mouseX, mouseY, button)) {
                    return true;
                }
                currentY += h;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean mouseReleased(int button) {
        for (Component c : settingComponents) {
            if (c.mouseReleased(button)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean mouseDragged(float mouseX, float mouseY, int button) {
        for (Component c : settingComponents) {
            if (c.mouseDragged(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean keyPressed(int keyCode) {
        if (binding) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                module.setKey(0);
            } else {
                module.setKey(keyCode);
            }
            binding = false;
            return true;
        }
        for (Component c : settingComponents) {
            if (c.keyPressed(keyCode)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public float getHeight(float width) {
        float h = 14f;
        if (expandAnimation.value() > 0f) {
            for (Component c : settingComponents) {
                if (c.isVisible()) {
                    h += c.getHeight(width) * expandAnimation.value();
                }
            }
        }
        return h;
    }
    
    @Override
    public boolean isVisible() {
        return true;
    }
    
    public void close() {
        binding = false;
    }
    
    public Module getModule() {
        return module;
    }
    
    public boolean isExpanded() {
        return expanded;
    }
    
    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }
}
