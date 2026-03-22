package ki.rage.client.screen.gui.component;

import ki.rage.client.util.render.color.ColorUtil;

public abstract class AbstractComponent implements Component {
    protected static final float PADDING = 4f;
    protected static final float TEXT_OFFSET_Y = 3.5f;
    protected static final float TEXT_SIZE = 10f;
    
    @Override
    public void update(float dt) {
    }
    
    @Override
    public boolean mouseClicked(float x, float y, float width, float height, float mouseX, float mouseY, int button) {
        return false;
    }
    
    @Override
    public boolean mouseReleased(int button) {
        return false;
    }
    
    @Override
    public boolean mouseDragged(float mouseX, float mouseY, int button) {
        return false;
    }
    
    @Override
    public boolean keyPressed(int keyCode) {
        return false;
    }
    
    protected boolean isHovered(float x, float y, float width, float height, float mouseX, float mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
