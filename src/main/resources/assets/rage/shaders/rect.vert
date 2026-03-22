#version 150

in vec2 aPos;
in vec4 aColor;
in vec4 aRect;

uniform vec2 uResolution;

out vec2 vPos;
out vec4 vColor;
out vec4 vRect;

void main() {
    vPos = aPos;
    vColor = aColor;
    vRect = aRect;
    vec2 ndc = vec2(aPos.x / uResolution.x * 2.0 - 1.0, 1.0 - aPos.y / uResolution.y * 2.0);
    gl_Position = vec4(ndc, 0.0, 1.0);
}
