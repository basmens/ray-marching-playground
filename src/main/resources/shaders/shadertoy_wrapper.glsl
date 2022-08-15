#type vertex
#version 330 core

layout (location = 0) in vec3 a_pos;

void main() {
  gl_Position = vec4(a_pos, 1);
}



#type fragment
#version 330 core

// General uniforms
uniform ivec2 u_resolution;
uniform float u_time;

// Output
out vec4 color;

// Wrapper variables
ivec2 iResolution = u_resolution;
float iTime = u_time;


// <scene code here>


void main() {
  mainImage(color, gl_FragCoord.xy);
}