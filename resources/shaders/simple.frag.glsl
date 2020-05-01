#version 410 core

layout (location = 0) out vec4 FINAL_COLOR;

in VERTEX_SHADER_OUT {
    vec4 color;
} SHADER_INPUT;

void main()
{
    FINAL_COLOR = SHADER_INPUT.color;
}