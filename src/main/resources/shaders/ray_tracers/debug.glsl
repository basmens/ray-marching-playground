bool rayTriangleIntersect(
    vec3 orig,      // [-1, -1,  0]
    vec3 dir,       // [ 0,  0, -1]
    vec3 v0,        // [-1, -1, -5]
    vec3 v1,        // [ 1, -1, -5]
    vec3 v2,        // [ 0,  1, -5]
    float &t, 
    float &u, 
    float &v
){ 
  






    vec3 v0v1 = v1 - v0;                      // [ 2,  0,  0]
    vec3 v0v2 = v2 - v0;                      // [ 1,  2,  0]
    vec3 pvec = dir.crossProduct(v0v2);       // [ 2, -1,  0]
    float det = v0v1.dotProduct(pvec);        // 4
    
    if (det < kEpsilon) return false;         // false
    
    float invDet = 1 / det;                   // 0.25
 
    vec3 tvec = orig - v0;                    // [ 0,  0,  5]
    u = tvec.dotProduct(pvec) * invDet;       // 0
    if (u < 0 || u > 1) return false;         // false
 
    vec3 qvec = tvec.crossProduct(v0v1);      // [ 0,  10,  0]
    v = dir.dotProduct(qvec) * invDet;        // 0
    if (v < 0 || u + v > 1) return false;     // false
 
    t = v0v2.dotProduct(qvec) * invDet;       // 5
 
    return true;
} 