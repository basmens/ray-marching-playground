#type fragment

void mainImage(out vec4 fragColor, in vec2 fragCoord) {
    vec2 texCoord = fragCoord / iResolution.xy * 3;

    vec3 color;

    if (texCoord.x < 1) {
        color = vec3(1, 0, 0);
    } else if (texCoord.x < 2) {
        color = vec3(0, 1, 0);
    } else {
        color = vec3(0, 0, 1);
    }
    
    fragColor = vec4(color, 1);
}
