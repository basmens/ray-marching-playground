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

// Camera uniforms
uniform vec3 u_cameraPosition;
uniform vec3 u_cameraDirection;
uniform float u_cameraFOV;

uniform mat4 u_pointCameraMatrix;
uniform mat3 u_vectorCameraMatrix;

// Structures
struct Material {
  vec3 color;

  float gloss;
  float diffuse;
  float albedo;

  //float roughness;
  float reflectiveness;
};

struct RayHit {
  vec3 position;
  vec3 normal;
  vec3 direction;
  Material material;

  bool hitSky;
};

struct Light {
  vec3 position;
  float radius;  // In case of skylight, define as if the light is 1 unit away from the object

  vec3 color;
  float intensity;

  bool isDistantLight;
};
const Light lights[6] = Light[6](
  Light(vec3(0, 1, 0), 1, vec3(1, 1, 1), 3, true),
//  Light(vec3(3, 1, 0), 1, vec3(1, 0, 0), 200, false)
  Light(vec3(0, -1, 0), 1, vec3(1, 1, 1), 1, true),
  Light(vec3(1, 0, 0), 1, vec3(1, 1, 1), 1, true),
  Light(vec3(-1, 0, 0), 1, vec3(1, 1, 1), 1, true),
  Light(vec3(0, 0, 1), 1, vec3(1, 1, 1), 1, true),
  Light(vec3(0, 0, -1), 1, vec3(1, 1, 1), 1, true)
);

// Settings
const int recursionDepth = 1;
const int reflectionRayCount = 1;
const int ambientLightRayCount = 0;
const int lightRayCount = 0;

const float maxStepSize = 1000000;
const float maxSteps = 10000;

const float shadow_bias = 1e-4;

// Constants
const float PI = 3.141592653589793;
const float TAU = 6.283185307179586;
const float ELIPSON = 1e-5;

const int totalRayCount = reflectionRayCount + ambientLightRayCount;

// Output
out vec4 fragColor;



// <scene code here>



// ==================================================================================================================================================
// Ray
// ==================================================================================================================================================
RayHit ray(vec3 position, vec3 direction) {
  Material hitMaterial;
  vec3 normal;
  float distanceToScene = maxStepSize;

  for (int i = 0; i < maxSteps && distanceToScene > ELIPSON; i++) {
    distanceToScene = getDistanceToScene(position, normal, hitMaterial);
    position += direction * distanceToScene;
  }

  RayHit hit = RayHit(position, normal, direction, hitMaterial, false);

  if(distanceToScene > ELIPSON) {
    getSkyHit(hit);
  }

  return hit;
}


// ==================================================================================================================================================
// Send rays
// ==================================================================================================================================================
// Can have children
RayHit sendReflectionRay(RayHit parentRayHit) {
  return ray(parentRayHit.position, parentRayHit.normal);
}


RayHit sendAmbientLightRay(RayHit parentRayHit) {
  return ray(parentRayHit.position, parentRayHit.normal);
}


// Send ray
RayHit sendRay(RayHit parentRayHit, int rayCountIndex) {
  if (rayCountIndex < reflectionRayCount) {
    return sendReflectionRay(parentRayHit);
  } else {
    return sendAmbientLightRay(parentRayHit);
  }
}


// Can not have children
bool sendLightRay(RayHit parentRayHit, int lightIndex, out vec3 direction) {
  Light light = lights[lightIndex];

  vec3 target = light.position;
  vec3 origin = parentRayHit.position + parentRayHit.normal * shadow_bias;
  direction = normalize((light.isDistantLight) ? target : target - origin);

  RayHit hit = ray(origin, direction);

  return distance(origin, hit.position) > distance(origin, target);
}


// ==================================================================================================================================================
// Calculate ray hit color
// ==================================================================================================================================================
void calculateRayHitColor(in RayHit rayHit, in vec3 colors[totalRayCount], out vec3 resultColor) {
  int i = 0;

  // Reflection
  vec3 reflectionColor = vec3(0);
  for(int j = 0; j < reflectionRayCount; i,j++) {
    reflectionColor += colors[i] /= colors.length();
  }
  reflectionColor /= reflectionRayCount;

  // Ambient light
  vec3 ambientLightColor = vec3(0);
  for(int j = 0; j < ambientLightRayCount; i,j++) {
    ambientLightColor += colors[i] /= colors.length();
  }
  ambientLightColor /= ambientLightRayCount;

  // Light
  vec3 lightColor = vec3(0);
  if(lightRayCount == 0) {
    lightColor = vec3(1);
  } else {
    for(i = 0; i < lights.length; i++) {
      for(int j = 0; j < lightRayCount; j++) {
        Light light = lights[i];

        vec3 target = light.position;
        vec3 origin = rayHit.position + rayHit.normal * shadow_bias;
        float distanceToLight = distance(origin, target);
        vec3 lightDirection = normalize((light.isDistantLight) ? target : target - origin);

        bool succes = distance(origin, ray(origin, lightDirection).position) > distanceToLight;

        if (true) {
          Light light = lights[i];
          float angleWithSurface = max(dot(rayHit.normal, lightDirection), 0);

          vec3 diffuseColor = rayHit.material.albedo / PI * lights[i].intensity * lights[i].color * angleWithSurface;

          if(light.isDistantLight) {
            lightColor += diffuseColor;
          } else {
            lightColor += diffuseColor / (4 * PI * distanceToLight * distanceToLight);
          }
        }
      }
    }
    lightColor /= lightRayCount;
    //lightColor = vec3(dot(-rayHit.direction, rayHit.normal));
  }
  
  float reflectiveness = rayHit.material.reflectiveness;
  resultColor = rayHit.material.color;
  //resultColor = reflectionColor * reflectiveness + rayHit.material.color * (1 - reflectiveness);
  resultColor *= lightColor;
}


// ==================================================================================================================================================
// New branch
// ==================================================================================================================================================
void newBranch(inout int indexArray[recursionDepth + 1], inout RayHit rayHitList[recursionDepth + 1], int start) {
  for (int i = start; i < recursionDepth; i++) {
    indexArray[i] = totalRayCount - 1;
    rayHitList[i] = sendRay(rayHitList[i - 1], indexArray[i]);
  }
}


// ==================================================================================================================================================
// Ray march
// ==================================================================================================================================================
vec3 rayMarch(vec3 rayOrigin, vec3 rayDirection) {
  // Algorithm works like countring backwards in a 'totalRayCount' numeral number system with 'recursionDepth + 1'(last one is used as an exit condition) digits
  RayHit rayHitList[recursionDepth + 1];  // +1 because the compiler gets angry when recursionDepth = 0
  vec3 colorList[recursionDepth * (totalRayCount - 1) + 1];
  int indexArray[recursionDepth + 1];

  indexArray[0] = 1;
  rayHitList[0] = ray(rayOrigin, rayDirection);

  if(recursionDepth == 0) {
    return rayHitList[0].material.color;
  }

  newBranch(indexArray, rayHitList, 1);
  while (indexArray[0] != 0) {
    // Compute rays
    for (indexArray[recursionDepth] = totalRayCount - 1; indexArray[recursionDepth] >= 0; indexArray[recursionDepth]--) {
      RayHit hit = sendRay(rayHitList[max(recursionDepth - 1, 0)], indexArray[recursionDepth]);  // Max because the compiler gets angry when recursionDepth = 0
      int index = recursionDepth * (totalRayCount - 1) - indexArray[recursionDepth];
      colorList[index] = hit.material.color;
    }
    indexArray[recursionDepth]++;

    // Mix colors
    int i;
    for (i = recursionDepth; indexArray[i] == 0; i--) {
      int index = (totalRayCount - 1) * (i - 1);

      vec3 colors[totalRayCount];
      for (int j = 0; j < totalRayCount; j++) {
        colors[j] = colorList[index + j];
      }

      index = max(index - indexArray[i - 1], 0);
      calculateRayHitColor(rayHitList[i - 1], colors, colorList[index]);
    }

    // Reconstruct new branch
    indexArray[i]--;
    newBranch(indexArray, rayHitList, i + 1);
  }

  return colorList[0];
}


// ==================================================================================================================================================
// Main
// ==================================================================================================================================================
void main() {
  // Calculating ray origin and direction of the camera ray
  // https://www.scratchapixel.com/lessons/3d-basic-rendering/ray-tracing-generating-camera-rays/generating-camera-rays
  vec2 PixelCoordScreenSpace = (2 * gl_FragCoord.xy - u_resolution) / u_resolution.y;
  vec2 PixelCoordImageSpace = PixelCoordScreenSpace * tan(u_cameraFOV / 2);
  vec3 PixelCoordCameraSpace = vec3(PixelCoordImageSpace.xy, -1);

  vec3 rayOrigin = u_cameraPosition;
  vec3 rayDirection = u_vectorCameraMatrix * normalize(PixelCoordCameraSpace);

  fragColor = vec4(rayMarch(rayOrigin, rayDirection), 1);
}