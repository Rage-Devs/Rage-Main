package ki.rage.client.screen.gui.panel;

import ki.rage.client.Client;
import ki.rage.client.screen.gui.component.module.ModuleComponent;
import ki.rage.client.util.render.Render2D;
import ki.rage.client.util.render.color.ColorUtil;
import ki.rage.feature.module.api.Category;
import ki.rage.feature.module.api.Module;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class CategoryPanel {
    private final String name;
    private final EnumSet<Category> categories;
    private final List<ModuleComponent> moduleComponents;
    private float x;
    private float y;
    private final float width;
    private boolean dragging;
    private float dragOffsetX;
    private float dragOffsetY;
    
    public CategoryPanel(String name, EnumSet<Category> categories, float x, float y, float width) {
        this.name = name;
        this.categories = categories;
        this.x = x;
        this.y = y;
        this.width = width;
        this.moduleComponents = new ArrayList<>();
        rebuildModules();
    }
    
    public void update(float dt) {
        if (moduleComponents.isEmpty()) {
            rebuildModules();
        }
        for (ModuleComponent component : moduleComponents) {
            component.update(dt);
        }
    }
    
    public void render(float mouseX, float mouseY, ColorUtil theme, float scrollOffset) {
        float renderY = y + scrollOffset;
        
        float bodyHeight = getBodyHeight();
        float innerX = x + 1f;
        float innerWidth = width - 2f;
        
        Render2D.blurredRoundedRect(innerX, renderY + 13, innerWidth, bodyHeight + 2, 0f, 0.5f, ColorUtil.rgba(0, 0, 0, 10));
        Render2D.roundedRect(innerX, renderY + 13, innerWidth, bodyHeight + 1, 0f, ColorUtil.rgba(0, 0, 0, 70));
        Render2D.roundedRect(x, renderY, width, 14f, 0f, theme);
        Render2D.roundedRectOutline(x, renderY, width, 14f + bodyHeight, 0f, 0.5f, theme);
        
        float textWidth = Render2D.textWidth(Render2D.opensans(), name, 9f);
        float textX = x + (width - textWidth) / 2f;
        Render2D.text(Render2D.opensans(), name, textX, renderY + 0.5f, 9f, ColorUtil.rgba(255, 255, 255, 255));
        
        float currentY = renderY + 12f;
        for (ModuleComponent component : moduleComponents) {
            float h = component.getHeight(innerWidth);
            component.render(innerX, currentY, innerWidth, h, mouseX, mouseY, theme, 1f);
            currentY += h;
        }
    }
    
    public boolean mouseClicked(float mouseX, float mouseY, int button, float scrollOffset) {
        float renderY = y + scrollOffset;
        
        if (isHeaderHovered(mouseX, mouseY, scrollOffset) && button == 0) {
            dragging = true;
            dragOffsetX = mouseX - x;
            dragOffsetY = mouseY - renderY;
            return true;
        }
        
        if (!isInBounds(mouseX, mouseY, scrollOffset)) {
            return false;
        }
        
        float innerX = x + 1f;
        float innerWidth = width - 2f;
        float currentY = renderY + 14f;
        
        for (ModuleComponent component : moduleComponents) {
            float h = component.getHeight(innerWidth);
            if (mouseY >= currentY && mouseY <= currentY + h) {
                if (component.mouseClicked(innerX, currentY, innerWidth, h, mouseX, mouseY, button)) {
                    return true;
                }
            }
            currentY += h;
        }
        
        return false;
    }
    
    public boolean mouseReleased(float mouseX, float mouseY, int button, float scrollOffset) {
        if (button == 0 && dragging) {
            dragging = false;
            return true;
        }
        for (ModuleComponent component : moduleComponents) {
            if (component.mouseReleased(button)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean mouseDragged(float mouseX, float mouseY, int button, float scrollOffset) {
        if (button == 0 && dragging) {
            float renderY = y + scrollOffset;
            x = mouseX - dragOffsetX;
            y = mouseY - dragOffsetY - scrollOffset;
            return true;
        }
        for (ModuleComponent component : moduleComponents) {
            if (component.mouseDragged(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean keyPressed(int keyCode) {
        for (ModuleComponent component : moduleComponents) {
            if (component.keyPressed(keyCode)) {
                return true;
            }
        }
        return false;
    }
    
    public void close() {
        for (ModuleComponent component : moduleComponents) {
            component.close();
        }
        dragging = false;
    }
    
    public boolean isHeaderHovered(float mouseX, float mouseY, float scrollOffset) {
        float renderY = y + scrollOffset;
        return mouseX >= x && mouseX <= x + width && mouseY >= renderY && mouseY <= renderY + 14f;
    }
    
    private boolean isInBounds(float mouseX, float mouseY, float scrollOffset) {
        float renderY = y + scrollOffset;
        float bodyHeight = getBodyHeight();
        return mouseX >= x && mouseX <= x + width && mouseY >= renderY && mouseY <= renderY + 14f + bodyHeight;
    }
    
    private float getBodyHeight() {
        float h = 0f;
        float innerWidth = width - 2f;
        for (ModuleComponent component : moduleComponents) {
            h += component.getHeight(innerWidth);
        }
        return h;
    }
    
    private void rebuildModules() {
        moduleComponents.clear();
        for (Module module : Client.getInstance().getModuleManager().all()) {
            if (categories.contains(module.category())) {
                moduleComponents.add(new ModuleComponent(module));
            }
        }
    }
    
    public String getName() {
        return name;
    }
    
    public float getX() {
        return x;
    }
    
    public float getY() {
        return y;
    }
    
    public void setX(float x) {
        this.x = x;
    }
    
    public void setY(float y) {
        this.y = y;
    }
    
    public List<ModuleComponent> getModuleComponents() {
        return moduleComponents;
    }
}
