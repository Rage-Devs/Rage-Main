package ki.rage.client.util.render.pipeline;

import ki.rage.client.util.render.color.ColorUtil;
import ki.rage.client.util.render.font.TtfFont;
import ki.rage.client.util.render.gl.ShaderProgram;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public class TextPipeline extends RenderPipeline {
    private int currentTexture;
    private float scaleFactor;

    public TextPipeline(ShaderProgram shader) {
        super(shader, 10, 4096);
        batch.setupAttribute(0, 2, 10, 0);
        batch.setupAttribute(1, 2, 10, 2);
        batch.setupAttribute(2, 4, 10, 4);
        batch.setupAttribute(3, 2, 10, 8);
    }

    public void setScaleFactor(float scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    public void text(TtfFont font, String text, float x, float y, ColorUtil color) {
        if (currentTexture != 0 && currentTexture != font.textureId()) {
            flush();
        }
        currentTexture = font.textureId();

        float invScale = 1f / scaleFactor;
        float cursorX = x;
        float baselineY = y + font.ascentPx() * invScale;
        float cursorY = baselineY;
        float lineHeight = font.lineHeightPx() * invScale;

        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            i += Character.charCount(cp);

            if (cp == '\n') {
                cursorX = x;
                cursorY += lineHeight;
                continue;
            }

            var ch = font.glyph(cp);
            float x0 = cursorX + ch.xoff() * invScale;
            float y0 = cursorY + ch.yoff() * invScale;
            float x1 = cursorX + ch.xoff2() * invScale;
            float y1 = cursorY + ch.yoff2() * invScale;
            float w = x1 - x0;
            float h = y1 - y0;

            if (w > 0f && h > 0f) {
                float u0 = ch.x0() / (float) font.atlasWidth();
                float v0 = 1f - (ch.y0() / (float) font.atlasHeight());
                float u1 = ch.x1() / (float) font.atlasWidth();
                float v1 = 1f - (ch.y1() / (float) font.atlasHeight());

                batch.vertex(x0, y0, u0, v0, color.r(), color.g(), color.b(), color.a(), u0, v0);
                batch.vertex(x1, y0, u1, v0, color.r(), color.g(), color.b(), color.a(), u1, v0);
                batch.vertex(x1, y1, u1, v1, color.r(), color.g(), color.b(), color.a(), u1, v1);
                
                batch.vertex(x0, y0, u0, v0, color.r(), color.g(), color.b(), color.a(), u0, v0);
                batch.vertex(x1, y1, u1, v1, color.r(), color.g(), color.b(), color.a(), u1, v1);
                batch.vertex(x0, y1, u0, v1, color.r(), color.g(), color.b(), color.a(), u0, v1);
            }

            cursorX += ch.xadvance() * invScale;
        }
    }

    @Override
    public void flush() {
        if (batch.vertexCount() == 0) return;
        
        batch.upload();
        shader.use();
        shader.uniform2f("uResolution", resolutionX, resolutionY);
        shader.uniform1i("uTexture", 0);
        
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
