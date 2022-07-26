#type vertex
#version 330 core

layout (location = 0) in vec3 a_pos;

void main() {
  gl_Position = vec4(a_pos, 1);
}



#type fragment
#version 330 core

uniform ivec2 u_resolution;
uniform float u_time;

uniform float u_cameraFOV;
uniform vec3 u_cameraPos;
uniform vec3 u_cameraDir;

out vec4 color;


const float PI = 3.141592653589793;
const float TAU = 6.283185307179586;


struct Shape {
    int shapeType;
    vec3 pos;
    float[3] dimensions;
    
    vec3 color;
    float luminous;
    float glow;
};


Shape shapes[3] = Shape[3](
    Shape(0, vec3(0, 0, 0), float[3](0., 1., 0.), vec3(.4, .4, .5), 0., 0.),
    Shape(1, vec3(0, 1, -3), float[3](1., 1., 1.), vec3(1, 0, 0), 2.5, 0.),
    Shape(1, vec3(-3, 1, 0), float[3](1., 1., 1.), vec3(1, 0, 0), 0., 0.)
);

const float minDistToSurface = 0.01;
const float maxDistToScene = 1000.;
const int maxSteps = 10000;
const float elipson = 1e-5;

const bool lighting = true;
const bool shadows = true;
const bool softShadows = true;
const bool fog = true;

vec3 fogColor = vec3(.49, .93, .93);
float noLightFogDistaince = 12.;
float fogDistainceLightReduction = .01;





vec3 rotateX(in vec3 vector, in float angle) {
    mat3 matrix = mat3(
        1., 0.        ,  0.        ,
        0., cos(angle), -sin(angle),
        0., sin(angle),  cos(angle));
    
    return vector * matrix;
}
vec3 rotateY(in vec3 vector, in float angle) {
    mat3 matrix = mat3(
         cos(angle), 0., sin(angle),
         0.        , 1., 0.        ,
        -sin(angle), 0., cos(angle));
    
    return vector * matrix;
}
vec3 rotateZ(in vec3 vector, in float angle) {
    mat3 matrix = mat3(
        cos(angle), -sin(angle), 0.,
        sin(angle),  cos(angle), 0.,
        0.        ,  0.        , 1.);
    
    return vector * matrix;
}


// <scene code here>


void ray(in vec3 rayOrigin, in vec3 rayDir, out float distToScene, out float distainces[maxSteps], out int hitObjectIndex) {
    distToScene = 0.;
    vec3 rayPos = rayOrigin;
    
    int i = 0;
    while(true) {
        int closestObjectIndex;
        float dist = getDistToScene(rayPos, closestObjectIndex);
        
        distToScene += dist;
        
        if(distToScene >= maxDistToScene || i > maxSteps) {
            hitObjectIndex = -1;
        	break;
        } else if(dist < minDistToSurface) {
            hitObjectIndex = closestObjectIndex;
        	break;
        }
        
        rayPos = rayOrigin + distToScene * rayDir;
        i++;
    }
}



vec4 rayMarch(in vec3 rayOrigin, in vec3 rayDir) {
    float distToScene;
    float distainces[maxSteps];
    int hitObjectIndex;
    ray(rayOrigin, rayDir, distToScene, distainces, hitObjectIndex);
    
    vec3 color;

    if(hitObjectIndex == -1) {
        color = fogColor; 
    } else {
        Shape hitObject = shapes[hitObjectIndex];
        color = hitObject.color;
        
        if(fog) {
	        float fogDistaince = noLightFogDistaince + hitObject.luminous * fogDistainceLightReduction;
 	        color = mix(color, fogColor, clamp(distToScene / fogDistaince, 0., 1.));
        }
    }
    
    return vec4(color, 1.);
}


void main() {
  // https://www.scratchapixel.com/lessons/3d-basic-rendering/ray-tracing-generating-camera-rays/generating-camera-rays
  vec2 PixelCoordScreenSpace = (2 * gl_FragCoord.xy - u_resolution) / u_resolution.y;
  vec2 PixelCoordImageSpace = PixelCoordScreenSpace * tan(u_cameraFOV / 2);
  vec3 PixelCoordCameraSpace = vec3(PixelCoordImageSpace.xy, -1);

  vec3 rayOrigin = u_cameraPos;
  vec3 rayDirection = normalize(PixelCoordCameraSpace);
  
  color = rayMarch(rayOrigin, rayDirection);
}