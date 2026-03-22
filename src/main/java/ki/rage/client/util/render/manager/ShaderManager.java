package ki.rage.client.util.render.manager;

import ki.rage.client.util.render.gl.ShaderProgram;

public class ShaderManager {
    public final ShaderProgram roundedRect;
    public final ShaderProgram roundedRectOutline;
    public final ShaderProgram text;
    public final ShaderProgram imageRounded;
    public final ShaderProgram blit;
    public final ShaderProgram blur;
    public final ShaderProgram texturedRoundedRect;
    public final ShaderProgram hsvSquare;

    public ShaderManager() {
        this.roundedRect = new ShaderProgram("assets/rage/shaders/rect.vert", "assets/rage/shaders/rounded_rect.frag");
        this.roundedRectOutline = new ShaderProgram("assets/rage/shaders/rect.vert", "assets/rage/shaders/rounded_rect_outline.frag");
        this.text = new ShaderProgram("assets/rage/shaders/text.vert", "assets/rage/shaders/text.frag");
        this.imageRounded = new ShaderProgram("assets/rage/shaders/image.vert", "assets/rage/shaders/image_rounded.frag");
        this.blit = new ShaderProgram("assets/rage/shaders/quad.vert", "assets/rage/shaders/blit.frag");
        this.blur = new ShaderProgram("assets/rage/shaders/quad.vert", "assets/rage/shaders/blur.frag");
        this.texturedRoundedRect = new ShaderProgram("assets/rage/shaders/quad.vert", "assets/rage/shaders/textured_round_rect.frag");
        this.hsvSquare = new ShaderProgram("assets/rage/shaders/quad.vert", "assets/rage/shaders/hsv_square.frag");
    }

    public void delete() {
        roundedRect.delete();
        roundedRectOutline.delete();
        text.delete();
        imageRounded.delete();
        blit.delete();
        blur.delete();
        texturedRoundedRect.delete();
        hsvSquare.delete();
    }
}
