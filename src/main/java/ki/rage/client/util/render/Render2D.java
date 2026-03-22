package ki.rage.client.util.render;

import ki.rage.client.util.render.color.ColorUtil;
import ki.rage.client.util.render.gl.QuadMesh;
import ki.rage.client.util.render.gl.ShaderProgram;
import ki.rage.client.util.render.gl.Texture2D;
import ki.rage.client.util.render.font.TtfFont;
import ki.rage.client.util.render.manager.FontManager;
import ki.rage.client.util.render.manager.ShaderManager;
import ki.rage.client.util.render.pipeline.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

public class Render2D {
    private static boolean initialized;

    private static ShaderManager shaders;
    private static FontManager fonts;
    
    private static RectPipeline rectPipeline;
    private static RectPipeline outlinePipeline;
    private static TextPipeline textPipeline;
    private static ImagePipeline imagePipeline;
    private static BlurPipeline blurPipeline;
    
    private static QuadMesh utilQuad;

    private static float resolutionX;
    private static float resolutionY;
    private static int framebufferWidth;
    private static int framebufferHeight;
    private static int scaleFactor;

    private static GlStateBackup stateBackup;

    private static final Map<String, Texture2D> textureCache = new HashMap<>();

    private Render2D() {
    }

    public static void beginFrame(int framebufferWidth, int framebufferHeight, int scaleFactor) {
        ensureInit();
        Render2D.stateBackup = GlStateBackup.capture();
        Render2D.scaleFactor = Math.max(1, scaleFactor);
        int vpW = stateBackup.viewportW();
        int vpH = stateBackup.viewportH();
        Render2D.framebufferWidth = vpW > 0 ? vpW : framebufferWidth;
        Render2D.framebufferHeight = vpH > 0 ? vpH : framebufferHeight;
        Render2D.resolutionX = Render2D.framebufferWidth / (float) Render2D.scaleFactor;
        Render2D.resolutionY = Render2D.framebufferHeight / (float) Render2D.scaleFactor;
        
        rectPipeline.setResolution(resolutionX, resolutionY);
        outlinePipeline.setResolution(resolutionX, resolutionY);
        textPipeline.setResolution(resolutionX, resolutionY);
        textPipeline.setScaleFactor(Render2D.scaleFactor);
        imagePipeline.setResolution(resolutionX, resolutionY);
        blurPipeline.reset();

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glDisable(GL11.GL_STENCIL_TEST);
        GL11.glColorMask(true, true, true, true);
        GL11.glEnable(GL11.GL_BLEND);
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
    }

    public static void endFrame() {
        rectPipeline.flush();
        outlinePipeline.flush();
        textPipeline.flush();
        imagePipeline.flush();
        
        if (stateBackup != null) {
            stateBackup.restore();
            stateBackup = null;
        }
    }

    public static float width() {
        return resolutionX;
    }

    public static float height() {
        return resolutionY;
    }

    public static TtfFont opensans() {
        return opensans(10f);
    }

    public static TtfFont opensans(float size) {
        ensureInit();
        return fonts.opensans(size, scaleFactor);
    }

    public static TtfFont montserrat() {
        return montserrat(10f);
    }

    public static TtfFont montserrat(float size) {
        ensureInit();
        return fonts.montserrat(size, scaleFactor);
    }

    public static TtfFont pixel() {
        return pixel(10f);
    }

    public static TtfFont pixel(float size) {
        ensureInit();
        return fonts.pixel(size, scaleFactor);
    }

    public static void roundedRect(float x, float y, float width, float height, float radius, ColorUtil color) {
        ensureInit();
        rectPipeline.rect(x, y, width, height, radius, color);
    }

    public static void roundedRectOutline(float x, float y, float width, float height, float radius, float thickness, ColorUtil color) {
        ensureInit();
        outlinePipeline.rect(x, y, width, height, radius, thickness, color);
    }

    public static void blurredRoundedRect(float x, float y, float width, float height, float radius, float blurRadius, ColorUtil color) {
        ensureInit();
        rectPipeline.flush();
        outlinePipeline.flush();
        textPipeline.flush();
        imagePipeline.flush();

        blurPipeline.prepare(framebufferWidth, framebufferHeight, blurRadius, scaleFactor,
                stateBackup.drawFramebuffer(), stateBackup.readFramebuffer(),
                stateBackup.viewportX(), stateBackup.viewportY(), stateBackup.viewportW(), stateBackup.viewportH());

        shaders.texturedRoundedRect.use();
        shaders.texturedRoundedRect.uniform2f("uResolution", resolutionX, resolutionY);
        shaders.texturedRoundedRect.uniform4f("uRect", x, y, width, height);
        shaders.texturedRoundedRect.uniform1f("uRadius", radius);
        shaders.texturedRoundedRect.uniform4f("uColor", color.r(), color.g(), color.b(), color.a());
        shaders.texturedRoundedRect.uniform1i("uTexture", 0);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, blurPipeline.blurredTexture());
        utilQuad.clear();
        utilQuad.vertex(x, y, 0f, 0f);
        utilQuad.vertex(x + width, y, 1f, 0f);
        utilQuad.vertex(x + width, y + height, 1f, 1f);
        utilQuad.vertex(x, y, 0f, 0f);
        utilQuad.vertex(x + width, y + height, 1f, 1f);
        utilQuad.vertex(x, y + height, 0f, 1f);
        utilQuad.upload();
        utilQuad.bind();
        utilQuad.draw();
        utilQuad.unbind();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        ShaderProgram.stop();
    }

    public static void text(String text, float x, float y, float size, ColorUtil color) {
        text(opensans(size), text, x, y, size, color);
    }

    public static void text(TtfFont font, String text, float x, float y, float size, ColorUtil color) {
        ensureInit();
        textPipeline.text(font, text, x, y, color);
    }

    public static float textWidth(String text, float size) {
        return textWidth(opensans(size), text, size);
    }

    public static float textWidth(TtfFont font, String text, float size) {
        ensureInit();
        float invScale = 1f / scaleFactor;
        float line = 0f;
        float max = 0f;
        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            i += Character.charCount(cp);
            if (cp == '\n') {
                max = Math.max(max, line);
                line = 0f;
                continue;
            }
            var ch = font.glyph(cp);
            line += ch.xadvance() * invScale;
        }
        return Math.max(max, line);
    }

    public static void debugFontAtlas(float x, float y, float width, float height, float size) {
    }

    public static void imageRounded(String resourcePath, float x, float y, float width, float height, float radius, ColorUtil tint) {
        ensureInit();
        Texture2D texture = texture(resourcePath);
        imagePipeline.image(texture.id(), x, y, width, height, radius, tint);
    }

    public static void hsvSquare(float x, float y, float width, float height, float hue, float alpha) {
        ensureInit();
        rectPipeline.flush();
        outlinePipeline.flush();
        textPipeline.flush();
        imagePipeline.flush();

        shaders.hsvSquare.use();
        shaders.hsvSquare.uniform2f("uResolution", resolutionX, resolutionY);
        shaders.hsvSquare.uniform4f("uRect", x, y, width, height);
        shaders.hsvSquare.uniform1f("uHue", hue);
        shaders.hsvSquare.uniform1f("uAlpha", alpha);

        utilQuad.clear();
        utilQuad.vertex(x, y, 0f, 0f);
        utilQuad.vertex(x + width, y, 1f, 0f);
        utilQuad.vertex(x + width, y + height, 1f, 1f);
        utilQuad.vertex(x, y, 0f, 0f);
        utilQuad.vertex(x + width, y + height, 1f, 1f);
        utilQuad.vertex(x, y + height, 0f, 1f);
        utilQuad.upload();
        utilQuad.bind();
        utilQuad.draw();
        utilQuad.unbind();
        ShaderProgram.stop();
    }

    private static Texture2D texture(String resourcePath) {
        Texture2D cached = textureCache.get(resourcePath);
        if (cached != null) {
            return cached;
        }
        Texture2D created = Texture2D.fromPngResource(resourcePath);
        textureCache.put(resourcePath, created);
        return created;
    }

    private static void ensureInit() {
        if (initialized) {
            return;
        }
        
        shaders = new ShaderManager();
        fonts = new FontManager();
        
        rectPipeline = new RectPipeline(shaders.roundedRect, false);
        outlinePipeline = new RectPipeline(shaders.roundedRectOutline, true);
        textPipeline = new TextPipeline(shaders.text);
        imagePipeline = new ImagePipeline(shaders.imageRounded);
        blurPipeline = new BlurPipeline(shaders.blit, shaders.blur);
        
        utilQuad = new QuadMesh(4, 6);
        utilQuad.setupAttribute(0, 2, 4, 0);
        utilQuad.setupAttribute(1, 2, 4, 2);
        
        initialized = true;
    }

    private record GlStateBackup(
            int program,
            int vao,
            int drawFramebuffer,
            int readFramebuffer,
            int activeTexture,
            int texture2D,
            boolean blend,
            boolean depthTest,
            boolean cull,
            boolean scissor,
            boolean stencil,
            int colorMask,
            int blendSrcRgb,
            int blendDstRgb,
            int blendSrcAlpha,
            int blendDstAlpha,
            int viewportX,
            int viewportY,
            int viewportW,
            int viewportH
    ) {
        static GlStateBackup capture() {
            int program = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
            int vao = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
            int drawFramebuffer = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
            int readFramebuffer = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
            int activeTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
            int texture2D = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
            boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
            boolean depthTest = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
            boolean cull = GL11.glIsEnabled(GL11.GL_CULL_FACE);
            boolean scissor = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
            boolean stencil = GL11.glIsEnabled(GL11.GL_STENCIL_TEST);
            int blendSrcRgb = GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB);
            int blendDstRgb = GL11.glGetInteger(GL14.GL_BLEND_DST_RGB);
            int blendSrcAlpha = GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA);
            int blendDstAlpha = GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);

            int viewportX;
            int viewportY;
            int viewportW;
            int viewportH;
            int colorMask;
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer viewport = stack.mallocInt(4);
                GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
                viewportX = viewport.get(0);
                viewportY = viewport.get(1);
                viewportW = viewport.get(2);
                viewportH = viewport.get(3);

                var mask = stack.malloc(4);
                GL11.glGetBooleanv(GL11.GL_COLOR_WRITEMASK, mask);
                colorMask = (mask.get(0) != 0 ? 1 : 0)
                        | (mask.get(1) != 0 ? 2 : 0)
                        | (mask.get(2) != 0 ? 4 : 0)
                        | (mask.get(3) != 0 ? 8 : 0);
            }

            return new GlStateBackup(
                    program,
                    vao,
                    drawFramebuffer,
                    readFramebuffer,
                    activeTexture,
                    texture2D,
                    blend,
                    depthTest,
                    cull,
                    scissor,
                    stencil,
                    colorMask,
                    blendSrcRgb,
                    blendDstRgb,
                    blendSrcAlpha,
                    blendDstAlpha,
                    viewportX,
                    viewportY,
                    viewportW,
                    viewportH
            );
        }

        void restore() {
            GL20.glUseProgram(program);
            GL30.glBindVertexArray(vao);
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, drawFramebuffer);
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, readFramebuffer);
            GL13.glActiveTexture(activeTexture);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture2D);

            if (blend) {
                GL11.glEnable(GL11.GL_BLEND);
            } else {
                GL11.glDisable(GL11.GL_BLEND);
            }
            if (depthTest) {
                GL11.glEnable(GL11.GL_DEPTH_TEST);
            } else {
                GL11.glDisable(GL11.GL_DEPTH_TEST);
            }
            if (cull) {
                GL11.glEnable(GL11.GL_CULL_FACE);
            } else {
                GL11.glDisable(GL11.GL_CULL_FACE);
            }
            if (scissor) {
                GL11.glEnable(GL11.GL_SCISSOR_TEST);
            } else {
                GL11.glDisable(GL11.GL_SCISSOR_TEST);
            }
            if (stencil) {
                GL11.glEnable(GL11.GL_STENCIL_TEST);
            } else {
                GL11.glDisable(GL11.GL_STENCIL_TEST);
            }

            GL11.glColorMask((colorMask & 1) != 0, (colorMask & 2) != 0, (colorMask & 4) != 0, (colorMask & 8) != 0);

            GL14.glBlendFuncSeparate(blendSrcRgb, blendDstRgb, blendSrcAlpha, blendDstAlpha);
            GL11.glViewport(viewportX, viewportY, viewportW, viewportH);
        }
    }
}
