#version 150

in vec2 vPos;
in vec4 vColor;
in vec4 vRect;

uniform float uRadius;

out vec4 fragColor;

float sdf(vec2 p, vec2 b, float r) {
    vec2 q = abs(p) - b + r;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
}

void main() {
    vec2 local = vPos - vRect.xy;
    vec2 size = vRect.zw;
    vec2 halfSize = size * 0.5;
    float r = min(uRadius, min(size.x, size.y) * 0.5);
    float dist = sdf(local - halfSize, halfSize - 1.0, r);
    float alpha = (1.0 - smoothstep(0.0, 1.0, dist)) * vColor.a;
    fragColor = vec4(vColor.rgb, alpha);
}
