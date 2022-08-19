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
struct RayHit {
  vec3 position;
  vec3 normal;
  vec3 direction;

  vec3 albedo;

  float distanceTraveled;
  int stepsTaken;
  float lastDistance;

  bool hitSky;
};
RayHit createRayHit() {
  return RayHit(vec3(0), vec3(0, 1, 0), vec3(0, -1, 0), vec3(0), 0, 0, 0, true);
}


struct Light {
  vec3 position;
  float radius;  // In case of skylight, define as if the light is 1 unit away from the object

  vec3 color;
  float intensity;

  bool isDistantLight;
  bool hasShadows;
};
const Light lights[1] = Light[1](
  // Light(vec3(0, 1, 0), 1, vec3(1, 1, 1), 3, true, false),
  // Light(vec3(0, -1, 0), 1, vec3(1, 1, 1), 1, true, false),
  // Light(vec3(1, 0, 0), 1, vec3(1, 1, 1), 1, true, false),
  // Light(vec3(-1, 0, 0), 1, vec3(1, 1, 1), 1, true, false),
  // Light(vec3(0, 0, 1), 1, vec3(1, 1, 1), 1, true, false),
  // Light(vec3(0, 0, -1), 1, vec3(1, 1, 1), 1, true, false)
  Light(vec3(0, 0, 0), 1, vec3(1, 1, 1), 10000, false, true)
);


// Settings
const float maxSteps = 1200;
const float shadow_bias = 1e-4;

const bool useAmbientLight = true;
const bool useLightSources = true;
const bool useDarkenEffect = true;
const bool useFog = false;

const float darkenEffect = 0.96;
const float fogFactor = 0.94;
const float maxFog = 0.6;

const vec3 ambientLightColor = vec3(1);
const vec3 fogColor = vec3(0, 0.5, 1);

// Constants
const float PI = 3.141592653589793;
const float TAU = 6.283185307179586;
const float ELIPSON = 1e-5;


// Output
out vec4 fragColor;



// <scene code here>



// ==================================================================================================================================================
// Ray
// ==================================================================================================================================================
RayHit ray(vec3 position, vec3 direction) {
  RayHit hit = createRayHit();
  
  vec3 normal;
  vec3 albedo;

  float distanceToScene = ELIPSON + 1;

  for (int i = 0; i < maxSteps && distanceToScene > ELIPSON; i++) {
    distanceToScene = getDistanceToScene(position, normal, albedo);
    position += direction * distanceToScene;

    hit.distanceTraveled += distanceToScene;
    hit.stepsTaken = i;
  }

  hit.position = position;
  hit.normal = normal;
  hit.direction = direction;
  hit.albedo = albedo;
  hit.lastDistance = distanceToScene;
  hit.hitSky = distanceToScene > ELIPSON;

  return hit;
}


// ==================================================================================================================================================
// Ray march
// ==================================================================================================================================================
vec3 rayMarch(vec3 rayOrigin, vec3 rayDirection) {
  RayHit hit = ray(rayOrigin, rayDirection);

  vec3 color = vec3(0);


  // Lighting
  if (hit.hitSky) {
    hit = getSkyHit(hit);
    color = hit.albedo;
  } else {

    // Ambient light
    if (useAmbientLight) {
      color += hit.albedo * ambientLightColor;
    }

    // Light sources
    if (useLightSources) {
      for (int i = 0; i < lights.length; i++) {
        Light light = lights[i];

        vec3 target = light.position;
        vec3 origin = hit.position + hit.normal * shadow_bias;
        float distanceToLight = distance(origin, target);
        vec3 lightDirection = normalize((light.isDistantLight) ? target : target - origin);

        bool succes = !light.hasShadows || ray(origin, lightDirection).distanceTraveled > distanceToLight;

        if (succes) {
          vec3 diffuseColor = hit.albedo / PI * lights[i].intensity * lights[i].color * clamp(dot(hit.normal, lightDirection), 0, 1);

          if (light.isDistantLight) {
            color += diffuseColor;
          } else {
            color += diffuseColor / (4 * PI * distanceToLight * distanceToLight);
          }
        }
      }
    }
    //color *= vec3(abs(dot(-hit.direction, hit.normal)));

    if (useDarkenEffect) {
      float interpolate = (hit.lastDistance / ELIPSON - 1) / dot(-hit.direction, hit.normal) + 1;
      interpolate = sqrt(clamp(interpolate, 0, 1));
      color *= pow(darkenEffect, hit.stepsTaken + interpolate);
    }
  }


  // Fog
  if (useFog) {
    float fog = 1 - pow(fogFactor, hit.distanceTraveled);
    fog = min(fog, maxFog);

    color = fogColor * fog + color * (1 - fog);
  }

  // Return result
  return color;
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