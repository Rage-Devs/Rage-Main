#version 150

in vec2 vPos;
in vec2 vUv;
in vec4 vColor;
in vec2 vTexCoord;

uniform sampler2D uTexture;

out vec4 fragColor;

void main() {
    float alpha = texture(uTexture, vTexCoord).r;
    fragColor = vec4(vColor.rgb, vColor.a * alpha);
}
