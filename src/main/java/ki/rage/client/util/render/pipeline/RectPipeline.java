package ki.rage.client.util.render.pipeline;

import ki.rage.client.util.render.color.ColorUtil;
import ki.rage.client.util.render.gl.ShaderProgram;

public class RectPipeline extends RenderPipeline {
    private final boolean outline;
    private float currentRadius;
    private float currentThickness;

    public RectPipeline(ShaderProgram shader, boolean outline) {
        super(shader, 10, 2048);
        this.outline = outline;
        batch.setupAttribute(0, 2, 10, 0);
        batch.setupAttribute(1, 4, 10, 2);
        batch.setupAttribute(2, 4, 10, 6);
    }

    public void rect(float x, float y, float width, float height, float radius, ColorUtil color) {
        rect(x, y, width, height, radius, 0f, color);
    }

    public void rect(float x, float y, float width, float height, float radius, float thickness, ColorUtil color) {
        if (batch.vertexCount() > 0 && (currentRadius != radius || (outline && currentThickness != thickness))) {
            flush();
        }
        
        currentRadius = radius;
        currentThickness = thickness;
        
        float x1 = x + width;
        float y1 = y + height;
        
        batch.vertex(x, y, color.r(), color.g(), color.b(), color.a(), x, y, width, height);
        batch.vertex(x1, y, color.r(), color.g(), color.b(), color.a(), x, y, width, height);
        batch.vertex(x1, y1, color.r(), color.g(), color.b(), color.a(), x, y, width, height);
        
        batch.vertex(x, y, color.r(), color.g(), color.b(), color.a(), x, y, width, height);
        batch.vertex(x1, y1, color.r(), color.g(), color.b(), color.a(), x, y, width, height);
        batch.vertex(x, y1, color.r(), color.g(), color.b(), color.a(), x, y, width, height);
    }

    @Override
    public void flush() {
        if (batch.vertexCount() == 0) return;
        
        batch.upload();
        shader.use();
        shader.uniform2f("uResolution", resolutionX, resolutionY);
        shader.uniform1f("uRadius", currentRadius);
        if (outline) {
            shader.uniform1f("uThickness", currentThickness);
        }
        batch.bind();
        batch.draw();
        batch.unbind();
        ShaderProgram.stop();
        batch.clear();
    }
}
