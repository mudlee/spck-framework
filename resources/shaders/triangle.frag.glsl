#version 410 core

layout (location = 0) out vec4 FINAL_COLOR;

layout (location = 0) in struct {
    vec4 color;
} VERTEX_OUTPUT;

void main()
{
    FINAL_COLOR = VERTEX_OUTPUT.color;
}