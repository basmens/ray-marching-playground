#type vertex
#version 330 core

layout(location = 0) in vec3 a_pos;

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
struct Triangle {
  vec3 v0, v1, v2;
  vec2 texC0, texC1, texC2;
  vec3 normal;

  vec3 albedo;
};
// Triangle createTriangle() {
//   return Triangle(vec3(0, 0, 0), vec3(1, 0, 0), vec3(0, 0, 1), vec3(0), 0, vec3(0));
// }

struct RayHit {
  vec3 position;
  vec3 normal;
  vec3 direction;

  vec3 albedo;

  float dist;
};
RayHit createRayHit() {
  return RayHit(vec3(0), vec3(0, 1, 0), vec3(0, -1, 0), vec3(0), 0);
}

struct Light {
  vec3 position;  // If distant light, normalize
  float radius;  // If distant light, define as if the light is 1 unit away from the object

  vec3 color;
  float intensity;

  bool isDistantLight;
};

// Settings
const float maxDist = 1200;
const float shadow_bias = 1e-1;

const vec3 skyColor = vec3(0, 0, 0);

// Constants
const float PI = 3.141592653589793;
const float TAU = 6.283185307179586;
const float EPSILON = 1e-4;

// Output
out vec4 fragColor;

// Scene
// Triangle[1] triangles = Triangle[1](
//   Triangle(
//     vec3(0, 0, 0), vec3(0, 0, 3), vec3(3, 0, 0),
//     vec2(0, 0), vec2(0, 1), vec2(1, 0),
//     vec3(0), vec3(1, 0, 1))
// );
Triangle[1] triangles = Triangle[1](
  Triangle(
    vec3(-1, -1, -5), vec3(1, -1, -5), vec3(0, 1, -5),
    vec2(0, 0), vec2(1, 0), vec2(0.5, 1),
    vec3(0), vec3(1))
);

const Light lights[1] = Light[1](
  Light(vec3(0, 3, 0), 1, vec3(1, 1, 1), 90, false)
);

// ==================================================================================================================================================
// Texture
// ==================================================================================================================================================
vec3 getTexture(vec3 color, vec2 texCoord) {
  color *= ((mod(texCoord.x * 10, 2) < 1 ^^ mod(texCoord.y * 10, 2) < 1) ? 0.5 : 1);

  float dist = length(texCoord) / sqrt(2);
  return color * dist * dist;
}


// ==================================================================================================================================================
// Intersect sphere
// ==================================================================================================================================================
bool intersectSphere(in vec3 center, in float radius2, in vec3 origin, in vec3 direction, out float t) {
  float t0, t1;

  vec3 l = center - origin;

  float tca = dot(l, direction);
  if(tca < 0)
    return false;

  float d2 = dot(l, l) - tca * tca;
  if(d2 > radius2)
    return false;

  float thc = sqrt(radius2 - d2);
  t0 = tca - thc;
  t1 = tca + thc;

  if(t0 < 0)
    t0 = t1;

  if(t1 < 0) {
    t1 = t0;

    if(t1 < 0)
      return false;
  }

  t = min(t0, t1);

  return true;
}

// ==================================================================================================================================================
// Intersect triangle
// ==================================================================================================================================================
bool intersectTriangle(in vec3 v0, in vec3 v1, in vec3 v2, in vec3 origin, in vec3 direction, out float t, out float u, out float v) {
  vec3 v2v0 = v0 - v2;
  vec3 v2v1 = v1 - v2;
  vec3 pvec = cross(direction, v2v1);
  float det = dot(v2v0, pvec);

  if(det < EPSILON)
    return false;

  float invDet = 1 / det; 
 
  vec3 tvec = origin - v2; 
  u = dot(tvec, pvec) * invDet; 
  if (u < 0 || u > 1) return false; 

  vec3 qvec = cross(tvec, v2v0);
  v = dot(direction, qvec) * invDet; 
  if (v < 0 || u + v > 1) return false; 

  t = dot(v2v1, qvec) * invDet;

  return true;
}

// ==================================================================================================================================================
// Intersect
// ==================================================================================================================================================
bool intersect(in Triangle object, in vec3 origin, in vec3 direction, out float t, out float u, out float v) {
  return intersectTriangle(object.v0, object.v1, object.v2, origin, direction, t, u, v);
}

// ==================================================================================================================================================
// Ray
// ==================================================================================================================================================
bool ray(in vec3 origin, in vec3 direction, in float tNear, out RayHit hit) {
  Triangle objectHit;
  bool hasHit = false;
  float u, v;

  for(int i = 0; i < triangles.length; i++) {
    float t;

    if(intersect(triangles[i], origin, direction, t, u, v) && t < tNear) {
      objectHit = triangles[i];
      tNear = t;
      hasHit = true;
    }
  }

  vec2 texCoord = objectHit.texC0 * u + objectHit.texC1 * v + objectHit.texC2 * (1 - u - v);

  hit = createRayHit();
  hit.position = origin + tNear * direction;
  hit.normal = objectHit.normal;
  hit.direction = direction;
  //hit.albedo = getTexture(objectHit.albedo, texCoord);
  hit.albedo = vec3(u, v, 1 - u - v);
  hit.dist = tNear;

  return hasHit;
}

// ==================================================================================================================================================
// Main
// ==================================================================================================================================================
void main() {
  for(int i = 0; i < triangles.length; i++) {
    Triangle t = triangles[i];
    vec3 ab = t.v0 - t.v1;
    vec3 ac = t.v0 - t.v2;
    triangles[i].normal = normalize(cross(ab, ac));
  }

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
    vec3 albedo = hit.albedo;
    color = vec3(0);

    // ========================================================================================
    // Calculate lighting
    // ========================================================================================
    for(int i = 0; i < lights.length; i++) {
      Light light = lights[i];

      vec3 origin = hit.position + hit.normal * shadow_bias;
      RayHit lightHit;

      if(light.isDistantLight) {
        // Light source is a distant light
        vec3 direction = light.position;

        if(!ray(origin, direction, maxDist, lightHit)) {
          vec3 diffuseColor = albedo / PI * lights[i].intensity * lights[i].color * clamp(dot(hit.normal, direction), 0, 1);
          color += diffuseColor;
        }
      } else {
        // Light source is a spherical light
        vec3 direction = normalize(light.position - origin);
        float lightDist = distance(light.position, origin);

        if(!ray(origin, direction, lightDist, lightHit)) {
          vec3 diffuseColor = albedo / PI * lights[i].intensity * lights[i].color * clamp(dot(hit.normal, direction), 0, 1);
          color += diffuseColor / (4 * PI * lightDist * lightDist);
        }
      }
    }
    //color = albedo * abs(dot(-hit.direction, hit.normal));

    color = albedo;
  }

  fragColor = vec4(color, 1);
}
