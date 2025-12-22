#type fragment
const int MENGER_SPONGE_ITERATIONS = 9;

mat3 generateReflectionMatrix(vec3 n) {
  return mat3(
      1 - 2 * n.x * n.x,    -2 * n.x * n.y,    -2 * n.x * n.z,
         -2 * n.y * n.x, 1 - 2 * n.y * n.y,    -2 * n.y * n.z,
         -2 * n.z * n.x,    -2 * n.z * n.y, 1 - 2 * n.z * n.z
    );
}


vec3 applyMirror(vec3 point, vec3 pos, vec3 normal, inout mat3 normalTransform) {
  vec3 p = point - pos;
  float dist = dot(p, normal);
  
  if (dist < 0) {
    dist *= -2;

    normalTransform = generateReflectionMatrix(normal) * normalTransform;
  } else {
    dist = 0;
  }

  return point + dist * normal;
}


vec3 applyScale(vec3 point, vec3 pos, vec3 scale) {
  return (point - pos) * scale + pos;
}


// https://iquilezles.org/articles/distfunctions/
float getDistanceToScene(in vec3 rayPos, out vec3 normal, out vec3 albedo) {
  float scale = 0.1;
  rayPos += vec3(0, 0, 0);
  rayPos *= scale;
  vec3 point = rayPos;

  mat3 normalTransform = mat3(1);

  // Transform the space around the cube
  // Approximate distance as a cube, no transformations
  if(length(point) < 5) {
    // All operations have to be done in reverse order
    for(int i = 0; i < MENGER_SPONGE_ITERATIONS; i++) { 
      point = applyScale(point, vec3(-1), vec3(3));
      scale *= 3;

      point = applyMirror(point, vec3(0, 4, 0), normalize(vec3(0, -1, -1)), normalTransform);
      point = applyMirror(point, vec3(0, 0, 0), normalize(vec3(0, 1, -1)), normalTransform);

      point = applyMirror(point, vec3(0, 0, 0), normalize(vec3(1, -1, 0)), normalTransform);
      point = applyMirror(point, vec3(4, 0, 0), normalize(vec3(-1, -1, 0)), normalTransform);

      point = applyMirror(point, vec3(2, 0, 0), normalize(vec3(-1, 0, 0)), normalTransform);
      point = applyMirror(point, vec3(1, 0, 0), normalize(vec3(-1, 0, 0)), normalTransform);
    }
  }

  // Calculate the distance to the cube
  vec3 q = abs(point) - 1;
  float distance = length(max(q,0.0)) + min(max(q.x,max(q.y,q.z)),0.0);
  distance /= scale;

  // Calculate the normal of the cube
  float maxQ = max(max(q.x, q.y), q.z);
  if(q.x == maxQ) {
    normal = vec3(sign(point.x), 0, 0);
  } else if(q.y  == maxQ) {
    normal = vec3(0, sign(point.y), 0);
  } else {
    normal = vec3(0, 0, sign(point.z));
  }
  normal *= normalTransform;

  // Return color
  //albedo = normal;
  //albedo = normalize(abs(point));
  albedo = abs(rayPos);

  return distance;
}


RayHit getSkyHit(RayHit hit) {
  RayHit result = hit;

  result.albedo = vec3(0, 0.3, 0.8);
  result.hitSky = true;

  return result;
}