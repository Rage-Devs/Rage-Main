package ki.rage.client.util.render.gl;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

public class QuadMesh {
    private final int vao;
    private final int vbo;
    private final int vertexSize;
    private FloatBuffer buffer;
    private int vertexCount;
    private int capacity;

    public QuadMesh() {
        this(10, 4096);
    }

    public QuadMesh(int vertexSize, int initialCapacity) {
        this.vertexSize = vertexSize;
        this.capacity = initialCapacity;
        this.buffer = MemoryUtil.memAllocFloat(capacity * vertexSize);
        this.vao = GL30.glGenVertexArrays();
        this.vbo = GL15.glGenBuffers();
        
        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, (long) capacity * vertexSize * Float.BYTES, GL15.GL_DYNAMIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    public void vertex(float... data) {
        if (vertexCount >= capacity) {
            grow();
        }
        buffer.put(data);
        vertexCount++;
    }

    public void upload() {
        if (vertexCount == 0) return;
        buffer.flip();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, buffer);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        buffer.clear();
    }

    public void bind() {
        GL30.glBindVertexArray(vao);
    }

    public void unbind() {
        GL30.glBindVertexArray(0);
    }

    public void draw() {
        if (vertexCount == 0) return;
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertexCount);
    }

    public void clear() {
        buffer.clear();
        vertexCount = 0;
    }

    public int vertexCount() {
        return vertexCount;
    }

    public void setupAttribute(int index, int size, int stride, int offset) {
        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL20.glEnableVertexAttribArray(index);
        GL20.glVertexAttribPointer(index, size, GL11.GL_FLOAT, false, stride * Float.BYTES, (long) offset * Float.BYTES);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    public void delete() {
        MemoryUtil.memFree(buffer);
        GL30.glDeleteVertexArrays(vao);
        GL15.glDeleteBuffers(vbo);
    }

    private void grow() {
        capacity *= 2;
        FloatBuffer newBuffer = MemoryUtil.memAllocFloat(capacity * vertexSize);
        buffer.flip();
        newBuffer.put(buffer);
        MemoryUtil.memFree(buffer);
        buffer = newBuffer;
        
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, (long) capacity * vertexSize * Float.BYTES, GL15.GL_DYNAMIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }
}
