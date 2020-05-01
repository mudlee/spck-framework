#version 410 core

layout (location=0) in vec3 position;
layout (location=1) in vec4 color;

out gl_PerVertex {
    vec4 gl_Position;
};

out VERTEX_SHADER_OUT {
    vec4 color;
} SHADER_OUTPUT;

void main()
{
    gl_Position = vec4(position, 1.0);
    SHADER_OUTPUT.color = color;
}