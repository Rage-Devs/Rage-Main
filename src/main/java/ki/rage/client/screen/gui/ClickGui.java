package ki.rage.client.screen.gui;

import ki.rage.client.screen.gui.panel.CategoryPanel;
import ki.rage.client.util.render.color.ColorUtil;
import ki.rage.feature.module.api.Category;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class ClickGui extends Screen {
    private final List<CategoryPanel> panels = new ArrayList<>();
    private long lastFrameTime;
    private float scrollOffset = 0f;
    private float targetScrollOffset = 0f;
    private boolean initialized = false;

    public ClickGui() {
        super(Text.literal("ClickGUI"));
    }

    @Override
    protected void init() {
        if (initialized) {
            return;
        }
        
        panels.clear();
        
        if (client == null || client.getWindow() == null) {
            return;
        }
        
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        
        float panelWidth = 100f;
        float spacing = 6;
        int categoryCount = Category.values().length;
        float totalWidth = categoryCount * panelWidth + (categoryCount - 1) * spacing;
        
        float startX = (screenWidth - totalWidth) / 2f;
        float startY = 30f;
        
        float x = startX;
        
        for (Category category : Category.values()) {
            panels.add(new CategoryPanel(category.name(), EnumSet.of(category), x, startY, panelWidth));
            x += panelWidth + spacing;
        }

        lastFrameTime = System.nanoTime();
        initialized = true;
    }

    @Override
    public void render(net.minecraft.client.gui.DrawContext context, int mouseX, int mouseY, float delta) {
    }

    public void renderGui(double mouseX, double mouseY) {
        if (panels.isEmpty()) {
            init();
        }

        long now = System.nanoTime();
        float deltaTime = lastFrameTime == 0 ? 0f : (now - lastFrameTime) / 1_000_000_000f;
        lastFrameTime = now;

        scrollOffset += (targetScrollOffset - scrollOffset) * Math.min(deltaTime * 10f, 1f);

        ColorUtil theme = ColorUtil.themeColor();
        for (CategoryPanel panel : panels) {
            panel.update(deltaTime);
            panel.render((float) mouseX, (float) mouseY, theme, scrollOffset);
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        float mx = (float) click.x();
        float my = (float) click.y();
        int button = click.button();

        for (int i = panels.size() - 1; i >= 0; i--) {
            CategoryPanel panel = panels.get(i);
            if (panel.mouseClicked(mx, my, button, scrollOffset)) {
                if (panel.isHeaderHovered(mx, my, scrollOffset)) {
                    panels.remove(i);
                    panels.add(panel);
                }
                return true;
            }
        }

        return super.mouseClicked(click, bl);
    }

    @Override
    public boolean mouseReleased(Click click) {
        float mx = (float) click.x();
        float my = (float) click.y();
        int button = click.button();

        for (CategoryPanel panel : panels) {
            if (panel.mouseReleased(mx, my, button, scrollOffset)) {
                return true;
            }
        }

        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        float mx = (float) click.x();
        float my = (float) click.y();
        int button = click.button();

        for (CategoryPanel panel : panels) {
            if (panel.mouseDragged(mx, my, button, scrollOffset)) {
                return true;
            }
        }

        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        targetScrollOffset += (float) verticalAmount * 20f;
        return true;
    }

    @Override
    public boolean keyPressed(KeyInput keyInput) {
        int keyCode = keyInput.key();

        for (CategoryPanel panel : panels) {
            if (panel.keyPressed(keyCode)) {
                return true;
            }
        }

        return super.keyPressed(keyInput);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        super.close();
    }
}