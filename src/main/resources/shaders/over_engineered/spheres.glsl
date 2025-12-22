#type fragment
// Materials
const Material METAL = Material(vec3(.59, .61, .63), .4, .1, 1, .7);



float getDistanceToScene(in vec3 rayPos, out vec3 normal, out Material materialHit) {
  rayPos = mod(rayPos, 6);

  float distance = length(rayPos - 3) - 1;
  normal = normalize(rayPos - 3);

  materialHit = METAL;
  materialHit.color = normal;
  
  return distance;
}


void getSkyHit(inout RayHit hit) {
  Material material = Material(vec3(0, 0, 1), 0, 0, 0, 0);
  material.color = vec3(0, 0, 1);
  hit.material = material;
  hit.hitSky = true;
}