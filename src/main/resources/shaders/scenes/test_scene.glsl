#type fragment
// Materials
const Material WOOD = Material(vec3(.75, .45, .04), .7, .6, 1, .1);
const Material STONE = Material(vec3(.49, .43, .41), .9, .2, 1, .05);
const Material METAL = Material(vec3(.59, .61, .63), .4, .1, 1, .7);


// https://iquilezles.org/articles/distfunctions/
float sdPlane(in vec3 pos, in vec3 normal, in vec3 rayPos) {
    return dot(rayPos - pos, normal);
}

float sdSphere(in vec3 pos, in vec3 radius, in vec3 rayPos) {
  vec3 p = rayPos - pos;
  float k0 = length(p / radius);
  float k1 = length(p / (radius * radius));
  return k0 * (k0 - 1) / k1;
}
vec3 normalSphere(in vec3 pos, in vec3 radius, in vec3 rayPos) {
  return normalize(rayPos - pos);
}



float getDistanceToScene(in vec3 rayPos, out vec3 normal, out Material materialHit) {
    float smallestDistance = maxStepSize;

    float distPlane = sdPlane(vec3(0, 0, 0), vec3(0, 1, 0), rayPos);
    float distSphere1 = sdSphere(vec3(0, 1, -3), vec3(1, 1, 1), rayPos);
    float distSphere2 = sdSphere(vec3(-3, 1, 0), vec3(1, 1, 1), rayPos);

    if (smallestDistance > distPlane) {
      smallestDistance = distPlane;
      normal = vec3(0, 1, 0);
      materialHit = WOOD;
    }
    if (smallestDistance > distSphere1) {
      smallestDistance = distSphere1;
      normal = normalSphere(vec3(0, 1, -3), vec3(1, 1, 1), rayPos);
      materialHit = STONE;
    }
    if (smallestDistance > distSphere2) {
      smallestDistance = distSphere2;
      normal = normalSphere(vec3(-3, 1, 0), vec3(1, 1, 1), rayPos);
      materialHit = METAL;
    }
    
    return smallestDistance;
}


void getSkyHit(inout RayHit hit) {
  Material material = Material(vec3(0, 0, 1), 0, 0, 0, 0);
  hit = RayHit(hit.position, hit.direction, vec3(0, -1, 0), material, true);
}