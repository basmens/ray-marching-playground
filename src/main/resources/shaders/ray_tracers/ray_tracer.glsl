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
struct Sphere {
  vec3 position;
  float radius;

  vec3 albedo;
};
const Sphere[1] spheres = Sphere[1](
  Sphere(vec3(0), 1, vec3(1, 0, 1))
);

struct RayHit {
  vec3 position;
  vec3 normal;
  vec3 direction;

  float dist;

  Sphere object;
};
RayHit createRayHit() {
  return RayHit(vec3(0), vec3(0, 1, 0), vec3(0, -1, 0), 0, Sphere(vec3(0), 0, vec3(0)));
}

struct Light {
  vec3 position;  // If distant light, normalize
  float radius;  // If distant light, define as if the light is 1 unit away from the object

  vec3 color;
  float intensity;

  bool isDistantLight;
};
const Light lights[1] = Light[1](
  Light(vec3(0, 3, 0), 1, vec3(1, 1, 1), 90, false)
);


// Settings
const float maxDist = 1200;
const float shadow_bias = 1e-4;

const vec3 skyColor = vec3(0, 0.3, 0.8);

// Constants
const float PI = 3.141592653589793;
const float TAU = 6.283185307179586;


// Output
out vec4 fragColor;


// ==================================================================================================================================================
// Intersect
// ==================================================================================================================================================
bool intersect(in Sphere object, in vec3 origin, in vec3 direction, out float t) {
  float t0, t1;
  
  vec3 l = object.position - origin;
  float radius2 = object.radius * object.radius;

  float tca = dot(l, direction);
  if (tca < 0) return false; 

  float d2 = dot(l, l) - tca * tca; 
  if (d2 > radius2) return false;

  float thc = sqrt(radius2 - d2); 
  t0 = tca - thc; 
  t1 = tca + thc;

  if (t0 < 0) t0 = t1;

  if (t1 < 0) {
    t1 = t0;

    if (t1 < 0) return false;
  }

  t = min(t0, t1); 

  return true;
}


// ==================================================================================================================================================
// Ray
// ==================================================================================================================================================
bool ray(in vec3 origin, in vec3 direction, in float tNear, out RayHit hit) {
  Sphere objectHit;
  bool hasHit = false;

  for (int i = 0; i < spheres.length; i++) {
    float t;

    if (intersect(spheres[i], origin, direction, t) && t < tNear) {
      objectHit = spheres[i];
      tNear = t;
      hasHit = true;
    }
  }

  hit = createRayHit();
  hit.position = origin + tNear * direction;
  hit.normal = normalize(hit.position - objectHit.position);
  hit.direction = direction;
  hit.dist = tNear;
  hit.object = objectHit;

  return hasHit;
}



// ==================================================================================================================================================
// Main
// ==================================================================================================================================================
void main() {

  // ===================================================================================================================
  // Calculate origin and direction of the camera ray
  // ===================================================================================================================

  // https://www.scratchapixel.com/lessons/3d-basic-rendering/ray-tracing-generating-camera-rays/generating-camera-rays
  vec2 PixelCoordScreenSpace = (2 * gl_FragCoord.xy - u_resolution) / u_resolution.y;
  vec2 PixelCoordImageSpace = PixelCoordScreenSpace * tan(u_cameraFOV / 2);
  vec3 PixelCoordCameraSpace = vec3(PixelCoordImageSpace.xy, -1);

  vec3 rayOrigin = u_cameraPosition;
  vec3 rayDirection = u_vectorCameraMatrix * normalize(PixelCoordCameraSpace);

  // ===================================================================================================================
  // Calculate fragment color
  // ===================================================================================================================
  vec3 color = skyColor;
  RayHit hit;

  if(ray(rayOrigin, rayDirection, maxDist, hit)) {
    vec3 albedo = hit.object.albedo;
    color= vec3(0);
  
    // ========================================================================================
    // Calculate lighting
    // ========================================================================================
    for (int i = 0; i < lights.length; i++) {
      Light light = lights[i];

      vec3 origin = hit.position + hit.normal * shadow_bias;
      RayHit lightHit;

      if(light.isDistantLight) {
        // Light source is a distant light
        vec3 direction = light.position;

        if (!ray(origin, direction, maxDist, lightHit)) {
          vec3 diffuseColor = albedo / PI * lights[i].intensity * lights[i].color * clamp(dot(hit.normal, direction), 0, 1);
          color += diffuseColor;
        }
      } else {
        // Light source is a spherical light
        vec3 direction = normalize(light.position - origin);
        float lightDist = distance(light.position, origin);

        if (!ray(origin, direction, lightDist, lightHit)) {
          vec3 diffuseColor = albedo / PI * lights[i].intensity * lights[i].color * clamp(dot(hit.normal, direction), 0, 1);
          color += diffuseColor / (4 * PI * lightDist * lightDist);
        }
      }
    }
    //color = albedo * abs(dot(-hit.direction, hit.normal));
  }

  fragColor = vec4(color, 1);
}
