#type fragment
// Materials
const Material METAL = Material(vec3(.59, .61, .63), .4, .1, 1, .7);


vec3 applyMirror(vec3 point, vec3 pos, vec3 normal) {
  vec3 p = point - pos;
  float dist = dot(p, normal);
  dist = abs(dist - abs(dist));

  return point + dist * normal;
}


vec3 applyScale(vec3 point, vec3 pos, vec3 scale) {
  return (point - pos) * scale + pos;
}


// https://iquilezles.org/articles/distfunctions/
float getDistanceToScene(in vec3 rayPos, out vec3 normal, out Material materialHit) {
  vec3 point = rayPos + vec3(0, -1, 0);

  // All operations have to be done in reverse order
  float scale = 1;
  for(int i = 0; i < 10; i++) { 
    point = applyScale(point, vec3(-1), vec3(3));
    scale *= 3;

    point = applyMirror(point, vec3(0, 4, 0), normalize(vec3(0, -1, -1)));
    point = applyMirror(point, vec3(0, 0, 0), normalize(vec3(0, 1, -1)));

    point = applyMirror(point, vec3(0, 0, 0), normalize(vec3(1, -1, 0)));
    point = applyMirror(point, vec3(4, 0, 0), normalize(vec3(-1, -1, 0)));

    point = applyMirror(point, vec3(2, 0, 0), normalize(vec3(-1, 0, 0)));
    point = applyMirror(point, vec3(1, 0, 0), normalize(vec3(-1, 0, 0)));
  }

  vec3 q = abs(point) - 1;
  float distance = length(max(q,0.0)) + min(max(q.x,max(q.y,q.z)),0.0);
  distance /= scale;

  float maxQ = max(max(q.x, q.y), q.z);
  if(q.x == maxQ) {
    normal = vec3(sign(point.x), 0, 0);
  } else if(q.y  == maxQ) {
    normal = vec3(0, sign(point.y), 0);
  } else {
    normal = vec3(0, 0, sign(point.z));
  }

  materialHit = METAL;
  //materialHit.color = normal;
  //materialHit.color = normalize(abs(point));
  materialHit.color = abs(rayPos + vec3(0, -1, 0));
  
  return distance;
}


void getSkyHit(inout RayHit hit) {
  Material material = createMaterial();
  material.color = vec3(0, 0, 1);
  hit.material = material;
  hit.hitSky = true;
}