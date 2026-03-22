package ki.rage.client.screen.gui.component;

import ki.rage.client.util.render.color.ColorUtil;

public interface Component {
    void update(float dt);
    
    void render(float x, float y, float width, float height, float mouseX, float mouseY, ColorUtil theme, float alpha);
    
    boolean mouseClicked(float x, float y, float width, float height, float mouseX, float mouseY, int button);
    
    boolean mouseReleased(int button);
    
    boolean mouseDragged(float mouseX, float mouseY, int button);
    
    boolean keyPressed(int keyCode);
    
    float getHeight(float width);
    
    boolean isVisible();
}
