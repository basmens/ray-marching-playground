# About
This is a little playground project for using ray tracing and ray marching shaders, as well as an option for using Shadertoy shaders. It id an OpenGL project through the use of LWJGL. Shaders are located in the `resources` folder. When applicable, you can fly around the scene. The shaders come in two steps, the shader itself and a scene, allowing you to use different scenes for the same shader. The project started with just a ray marcher for fractals, inspired by this video: https://www.youtube.com/watch?v=svLzmFuSBhk.

# How to use
In `engine/Scene.java` you can set your shader and scene path. Due to questionable design resulting from the nature of an iterative experimental playground project, the scene always has to be set, even if it is not applicable. There are a few scenes already given:
* The first ray tracer displays a simple sphere with a light above, the second two trace three triangles with a black-pink square grid pattern.
* The fractal renderer renders the Menger Sponge using ray marching.
* The old ray marcher seems to be broken.
* The over engineered spheres scene displays the normals of an infinite grid of spheres. The other scene seems broken.
* The Shadertoy scene just displays three test bands.

## Controls
Most scenes allow you to move around. You can use WASD to fly forward, left, back and right, and you can use space and shift to go up and down. Furthermore, you can use your scroll wheel to change the speed of flight.

# How to run
This project should hopefully run out of the box by simply cloning and running `Main.java`. However, I have had weird OpenGL issues that as far as I know are due to intel driver bugs. In case this happens, forcing the program in high performance node, for example using the Nvidia control panel for me, fixes it for me.

# Improvements
The way I am using scenes here is not very good. 16 year old me did not understand buffers very well yet. Though for the fractal ray marcher scenes are often programmatic and so not easily expressed in buffers, so at least there exists some argument to be made there. Furthermore putting the fragment and vertex shader in the same file is also somewhat questionable. Finally the project architecture has some ways to go.