package ki.rage.client.util.render.pipeline;

import ki.rage.client.util.render.color.ColorUtil;
import ki.rage.client.util.render.gl.ShaderProgram;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public class ImagePipeline extends RenderPipeline {
    private int currentTexture;
    private float currentRadius;

    public ImagePipeline(ShaderProgram shader) {
        super(shader, 14, 1024);
        batch.setupAttribute(0, 2, 14, 0);
        batch.setupAttribute(1, 2, 14, 2);
        batch.setupAttribute(2, 4, 14, 4);
        batch.setupAttribute(3, 4, 14, 8);
        batch.setupAttribute(4, 2, 14, 12);
    }

    public void image(int textureId, float x, float y, float width, float height, float radius, ColorUtil tint) {
        if (batch.vertexCount() > 0 && (currentTexture != textureId || currentRadius != radius)) {
            flush();
        }
        currentTexture = textureId;
        currentRadius = radius;

        float x1 = x + width;
        float y1 = y + height;

        batch.vertex(x, y, 0f, 0f, tint.r(), tint.g(), tint.b(), tint.a(), x, y, width, height, radius, 0f);
        batch.vertex(x1, y, 1f, 0f, tint.r(), tint.g(), tint.b(), tint.a(), x, y, width, height, radius, 0f);
        batch.vertex(x1, y1, 1f, 1f, tint.r(), tint.g(), tint.b(), tint.a(), x, y, width, height, radius, 0f);
        
        batch.vertex(x, y, 0f, 0f, tint.r(), tint.g(), tint.b(), tint.a(), x, y, width, height, radius, 0f);
        batch.vertex(x1, y1, 1f, 1f, tint.r(), tint.g(), tint.b(), tint.a(), x, y, width, height, radius, 0f);
        batch.vertex(x, y1, 0f, 1f, tint.r(), tint.g(), tint.b(), tint.a(), x, y, width, height, radius, 0f);
    }

    @Override
    public void flush() {
        if (batch.vertexCount() == 0) return;
        
        batch.upload();
        shader.use();
        shader.uniform2f("uResolution", resolutionX, resolutionY);
        shader.uniform1i("uTexture", 0);
        shader.uniform1f("uRadius", currentRadius);
        
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture);
        
        batch.bind();
        batch.draw();
        batch.unbind();
        
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        ShaderProgram.stop();
        batch.clear();
        currentTexture = 0;
    }
}
