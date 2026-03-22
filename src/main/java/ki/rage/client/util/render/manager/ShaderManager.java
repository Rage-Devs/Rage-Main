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
        this.roundedRect = new ShaderProgram("assets/rage/shaders/vertex/rect.vert", "assets/rage/shaders/fragment/rounded_rect.frag");
        this.roundedRectOutline = new ShaderProgram("assets/rage/shaders/vertex/rect.vert", "assets/rage/shaders/fragment/rounded_rect_outline.frag");
        this.text = new ShaderProgram("assets/rage/shaders/vertex/text.vert", "assets/rage/shaders/fragment/text.frag");
        this.imageRounded = new ShaderProgram("assets/rage/shaders/vertex/image.vert", "assets/rage/shaders/fragment/image_rounded.frag");
        this.blit = new ShaderProgram("assets/rage/shaders/vertex/quad.vert", "assets/rage/shaders/fragment/blit.frag");
        this.blur = new ShaderProgram("assets/rage/shaders/vertex/quad.vert", "assets/rage/shaders/fragment/blur.frag");
        this.texturedRoundedRect = new ShaderProgram("assets/rage/shaders/vertex/quad.vert", "assets/rage/shaders/fragment/textured_round_rect.frag");
        this.hsvSquare = new ShaderProgram("assets/rage/shaders/vertex/quad.vert", "assets/rage/shaders/fragment/hsv_square.frag");
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
