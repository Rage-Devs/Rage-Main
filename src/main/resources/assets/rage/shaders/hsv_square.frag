#version 150

in vec2 vPos;

uniform vec4 uRect;
uniform float uHue;
uniform float uAlpha;

out vec4 fragColor;

vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

void main() {
    vec2 local = (vPos - uRect.xy) / max(uRect.zw, vec2(1.0));
    float s = clamp(local.x, 0.0, 1.0);
    float v = 1.0 - clamp(local.y, 0.0, 1.0);
    vec3 rgb = hsv2rgb(vec3(fract(uHue), s, v));
    fragColor = vec4(rgb, clamp(uAlpha, 0.0, 1.0));
}

