#version 330

uniform sampler2D InSampler;

layout(std140) uniform BlurParams {
    float Strength;
};

in vec2 texCoord;
out vec4 fragColor;

void main() {
    // Radial / zoom blur from the screen centre.
    // We take samples along the line from the current texel towards the centre
    // and average them.  Strength (0-1) controls the distance of the outermost
    // sample from the original position.

    vec2 centre = vec2(0.5);
    vec2 dir = texCoord - centre;          // direction away from centre
    float dist = length(dir);

    const int SAMPLES = 16;
    float scale = Strength * 0.15;         // keep the max offset subtle

    vec4 color = vec4(0.0);
    for (int i = 0; i < SAMPLES; i++) {
        float t = float(i) / float(SAMPLES - 1) - 0.5; // -0.5 .. +0.5
        vec2 offset = dir * t * scale;
        color += texture(InSampler, texCoord + offset);
    }
    fragColor = color / float(SAMPLES);
}
