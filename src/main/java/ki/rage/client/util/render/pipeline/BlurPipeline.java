package ki.rage.client.util.render.pipeline;

import ki.rage.client.util.render.gl.Framebuffer;
import ki.rage.client.util.render.gl.QuadMesh;
import ki.rage.client.util.render.gl.ShaderProgram;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;

public class BlurPipeline {
    private final ShaderProgram blitShader;
    private final ShaderProgram blurShader;
    private final QuadMesh quad;
    private Framebuffer bufferA;
    private Framebuffer bufferB;
    private int captureTexture;
    private int captureWidth;
    private int captureHeight;
    private boolean prepared;
    private float preparedRadius;

    public BlurPipeline(ShaderProgram blitShader, ShaderProgram blurShader) {
        this.blitShader = blitShader;
        this.blurShader = blurShader;
        this.quad = new QuadMesh(4, 6);
        quad.setupAttribute(0, 2, 4, 0);
        quad.setupAttribute(1, 2, 4, 2);
    }

    public void prepare(int fbWidth, int fbHeight, float radius, int scaleFactor, int drawFb, int readFb, int vpX, int vpY, int vpW, int vpH) {
        if (prepared && Float.compare(preparedRadius, radius) == 0) return;

        ensureCaptureTexture(fbWidth, fbHeight);
        ensureBuffers(fbWidth, fbHeight);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, captureTexture);
        GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, fbWidth, fbHeight);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        float radiusPx = Math.max(0f, radius) * scaleFactor;

        bufferA.bind();
        blitShader.use();
        blitShader.uniform2f("uResolution", bufferA.width(), bufferA.height());
        blitShader.uniform4f("uRect", 0f, 0f, bufferA.width(), bufferA.height());
        blitShader.uniform1i("uTexture", 0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, captureTexture);
        quad.clear();
        quad.vertex(0f, 0f, 0f, 0f);
        quad.vertex(bufferA.width(), 0f, 1f, 0f);
        quad.vertex(bufferA.width(), bufferA.height(), 1f, 1f);
        quad.vertex(0f, 0f, 0f, 0f);
        quad.vertex(bufferA.width(), bufferA.height(), 1f, 1f);
        quad.vertex(0f, bufferA.height(), 0f, 1f);
        quad.upload();
        quad.bind();
        quad.draw();
        quad.unbind();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        ShaderProgram.stop();

        bufferB.bind();
        blurShader.use();
        blurShader.uniform2f("uResolution", bufferB.width(), bufferB.height());
        blurShader.uniform4f("uRect", 0f, 0f, bufferB.width(), bufferB.height());
        blurShader.uniform1i("uTexture", 0);
        blurShader.uniform2f("uTextureResolution", bufferA.width(), bufferA.height());
        blurShader.uniform2f("uDirection", 1f, 0f);
        blurShader.uniform1f("uRadius", radiusPx);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, bufferA.textureId());
        quad.clear();
        quad.vertex(0f, 0f, 0f, 0f);
        quad.vertex(bufferB.width(), 0f, 1f, 0f);
        quad.vertex(bufferB.width(), bufferB.height(), 1f, 1f);
        quad.vertex(0f, 0f, 0f, 0f);
        quad.vertex(bufferB.width(), bufferB.height(), 1f, 1f);
        quad.vertex(0f, bufferB.height(), 0f, 1f);
        quad.upload();
        quad.bind();
        quad.draw();
        quad.unbind();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        ShaderProgram.stop();

        bufferA.bind();
        blurShader.use();
        blurShader.uniform2f("uResolution", bufferA.width(), bufferA.height());
        blurShader.uniform4f("uRect", 0f, 0f, bufferA.width(), bufferA.height());
        blurShader.uniform1i("uTexture", 0);
        blurShader.uniform2f("uTextureResolution", bufferB.width(), bufferB.height());
        blurShader.uniform2f("uDirection", 0f, 1f);
        blurShader.uniform1f("uRadius", radiusPx);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, bufferB.textureId());
        quad.clear();
        quad.vertex(0f, 0f, 0f, 0f);
        quad.vertex(bufferA.width(), 0f, 1f, 0f);
        quad.vertex(bufferA.width(), bufferA.height(), 1f, 1f);
        quad.vertex(0f, 0f, 0f, 0f);
        quad.vertex(bufferA.width(), bufferA.height(), 1f, 1f);
        quad.vertex(0f, bufferA.height(), 0f, 1f);
        quad.upload();
        quad.bind();
        quad.draw();
        quad.unbind();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        ShaderProgram.stop();

        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, drawFb);
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, readFb);
        GL11.glViewport(vpX, vpY, vpW, vpH);

        prepared = true;
        preparedRadius = radius;
    }

    public int blurredTexture() {
        return bufferA != null ? bufferA.textureId() : 0;
    }

    public void reset() {
        prepared = false;
    }

    public void delete() {
        quad.delete();
        if (bufferA != null) bufferA.delete();
        if (bufferB != null) bufferB.delete();
        if (captureTexture != 0) GL11.glDeleteTextures(captureTexture);
    }

    private void ensureCaptureTexture(int width, int height) {
        int w = Math.max(1, width);
        int h = Math.max(1, height);
        
        if (captureTexture == 0) {
            captureTexture = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, captureTexture);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        }
        
        if (captureWidth != w || captureHeight != h) {
            captureWidth = w;
            captureHeight = h;
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, captureTexture);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, w, h, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, 0L);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        }
    }

    private void ensureBuffers(int fbWidth, int fbHeight) {
        int w = Math.max(1, fbWidth);
        int h = Math.max(1, fbHeight);
        
        if (bufferA == null) {
            bufferA = new Framebuffer(w, h);
            bufferB = new Framebuffer(w, h);
            return;
        }
        
        if (bufferA.width() != w || bufferA.height() != h) {
            bufferA.resize(w, h);
            bufferB.resize(w, h);
        }
    }
}
