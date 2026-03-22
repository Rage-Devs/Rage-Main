#version 150

in vec2 aPos;
in vec2 aUv;
in vec4 aColor;
in vec4 aRect;
in vec2 aData;

uniform vec2 uResolution;

out vec2 vPos;
out vec2 vUv;
out vec4 vColor;
out vec4 vRect;
out vec2 vData;

void main() {
    vPos = aPos;
    vUv = aUv;
    vColor = aColor;
    vRect = aRect;
    vData = aData;
    vec2 ndc = vec2(aPos.x / uResolution.x * 2.0 - 1.0, 1.0 - aPos.y / uResolution.y * 2.0);
    gl_Position = vec4(ndc, 0.0, 1.0);
}
