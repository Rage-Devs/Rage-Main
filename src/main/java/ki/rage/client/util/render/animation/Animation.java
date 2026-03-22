package ki.rage.client.util.render.animation;

public class Animation {
    private float value;
    private float target;
    private float speed = 16f;

    public Animation(float value) {
        this.value = value;
        this.target = value;
    }

    public float value() {
        return value;
    }

    public float target() {
        return target;
    }

    public void setTarget(float target) {
        this.target = target;
    }

    public void snap(float value) {
        this.value = value;
        this.target = value;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void update(float dtSeconds) {
        float dt = Math.max(0f, Math.min(0.1f, dtSeconds));
        float k = 1f - (float) Math.exp(-speed * dt);
        value += (target - value) * k;
        if (Math.abs(target - value) < 0.0005f) {
            value = target;
        }
    }
}

