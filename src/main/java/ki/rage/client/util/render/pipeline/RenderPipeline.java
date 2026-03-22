package ki.rage.client.util.render.pipeline;

import ki.rage.client.util.render.gl.ShaderProgram;
import ki.rage.client.util.render.gl.QuadMesh;

public abstract class RenderPipeline {
    protected final ShaderProgram shader;
    protected final QuadMesh batch;
    protected float resolutionX;
    protected float resolutionY;

    protected RenderPipeline(ShaderProgram shader, int vertexSize, int capacity) {
        this.shader = shader;
        this.batch = new QuadMesh(vertexSize, capacity);
    }

    public void setResolution(float width, float height) {
        this.resolutionX = width;
        this.resolutionY = height;
    }

    public abstract void flush();

    public void delete() {
        batch.delete();
    }
}
