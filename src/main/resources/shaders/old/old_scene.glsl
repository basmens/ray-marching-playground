#type fragment
float sdPlane(in vec3 pos, in float[3] dimensions, in vec3 rayPos) {
    vec3 n = normalize(vec3(dimensions[0], dimensions[1], dimensions[2]));
    return dot(rayPos - pos, n);
}
float sdSphere(in vec3 pos, in float[3] dimensions, in vec3 rayPos) {
    vec3 size = vec3(dimensions[0], dimensions[1], dimensions[2]);
    return length((rayPos - pos) / size) - 1.;
}



float getDistToScene(in vec3 rayPos, out int closestObjectIndex) {
    float distToScene = maxDistToScene;
    
    for(int i = 0; i < shapes.length(); i++) {
        Shape s = shapes[i];
        float distToShape = maxDistToScene;
        
        if(s.shapeType == 0) {
	        distToShape = sdPlane(s.pos, s.dimensions, rayPos);
        } else if(s.shapeType == 1) {
	        distToShape = sdSphere(s.pos, s.dimensions, rayPos);
        }
        
       if(distToShape < distToScene) {
            distToScene = distToShape;
            closestObjectIndex = i;
        }
    }
    
    return distToScene;
}