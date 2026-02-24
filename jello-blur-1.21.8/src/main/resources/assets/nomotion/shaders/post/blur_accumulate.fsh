#version 150

uniform sampler2D InSampler;
uniform sampler2D PrevFrameSampler;

layout(std140) uniform BlurParams {
    float Strength;
};

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 current = texture(InSampler, texCoord);
    vec4 prev = texture(PrevFrameSampler, texCoord);
    // Frame accumulation motion blur:
    // Mix current frame with previous accumulated frame.
    // Strength controls how much of the previous frame persists (motion trail).
    // 0.0 = no blur, 1.0 = full trail (frozen image).
    fragColor = mix(current, prev, Strength);
}
