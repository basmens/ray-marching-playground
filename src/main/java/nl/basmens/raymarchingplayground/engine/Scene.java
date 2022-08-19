package nl.basmens.raymarchingplayground.engine;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector2i;
import org.joml.Vector3f;

import nl.basmens.raymarchingplayground.engine.eventListeners.KeyEventListener;
import nl.basmens.raymarchingplayground.engine.eventListeners.MouseEventListener;
import nl.basmens.raymarchingplayground.renderer.Camera;
import nl.basmens.raymarchingplayground.renderer.Renderer;
import nl.basmens.raymarchingplayground.renderer.Shader;
import nl.basmens.raymarchingplayground.util.Time;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_V;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Scene {
  private static final Logger logger = LogManager.getLogger(Scene.class);

  Renderer renderer;
  Shader shader;
  Camera camera;

  private double cameraPosX, cameraPosY, cameraPosZ;
  private double cameraDirX, cameraDirY, cameraDirZ;
  private double cameraSpeed = 10;
  private double cameraSensitivity = 0.002;


  // Fractals
  private String shaderFilePath = "src/main/resources/shaders/fractals/ray_marching.glsl";
  private String sceneFilePath = "src/main/resources/shaders/fractals/menger_sponge.glsl";

  // Old
  //private String shaderFilePath = "src/main/resources/shaders/old/ray_marching_old.glsl";
  //private String sceneFilePath = "src/main/resources/shaders/old/old_scene.glsl";

  // Over_engineered
  // private String shaderFilePath = "src/main/resources/shaders/over_engineered/ray_marching_over_engineered.glsl";
  // private String sceneFilePath = "src/main/resources/shaders/over_engineered/spheres.glsl";
  // private String sceneFilePath = "src/main/resources/shaders/over_engineered/test_scene.glsl";

  // Shadertoy
  // private String shaderFilePath = "src/main/resources/shaders/shadertoy/shadertoy_wrapper.glsl";
  // private String sceneFilePath = "src/main/resources/shaders/shadertoy/shadertoy_scene.glsl";
  


  // ==================================================================================================================================================
  // Initialization
  // ==================================================================================================================================================
  public Scene() {
    super();
  }


  public void init() {
    logger.info("FractalScene init");

    shader = new Shader(shaderFilePath);
    String source = shader.getFragmentSource();

    Pattern p = Pattern.compile("(<scene code here>)", Pattern.CASE_INSENSITIVE);
    Matcher m = p.matcher(source);
    int splitIndex = 0;
    if(m.find()) {
      splitIndex = source.indexOf("\n", m.end());
    } else {
      logger.error("No place to put the scene code. Add '<scene code here>' somewhere to insert the scene code");
    }

    // Pattern p = Pattern.compile("(#)([a-z]+)( )");
    // Matcher m = p.matcher(source);
    // int splitIndex = 0;
    // while(m.find(splitIndex)) {
    //   splitIndex = m.end();
    // }
    // splitIndex = source.indexOf("\n", splitIndex);

    // Generate shader and renderer
    shader.setFragmentSource(source.substring(0, splitIndex) + Shader.readFragmentShaderFile(sceneFilePath) + "\n" + source.substring(splitIndex));
    shader.compile();

    renderer = new Renderer(shader);
    renderer.generateBufferObjects();

    // Generate camera
    camera = new Camera((float)(Math.PI / 4));

    cameraPosX = 0;
    cameraPosY = 0;
    cameraPosZ = 35;

    cameraDirX = 0;
    cameraDirY = 0;
    cameraDirZ = 0;

    setCamera();

    // Upload uniforms
    camera.uploadDataToShader(shader);
    shader.uploadVec2i("u_resolution", new Vector2i(Window.get().getWidth(), Window.get().getHeight()));
  }


  // ==================================================================================================================================================
  // Update
  // ==================================================================================================================================================
  public void update(double dt) {
    double t = Math.pow(Time.getTime() * 20 + 4, 0.6) - 2;
    // double cameraDist = 30 / (t * 0.1 + 2) + 5;

    // double angle = t * 0.05 - 0.5;
    // //double angle = -0f;
    // camera.setPosition(new Vector3f((float)(Math.sin(angle) * cameraDist), (float)(Math.sin(angle * 0.4) * 4), (float)(Math.cos(angle) * cameraDist)));
    // camera.setDirection(new Vector3f((float)(Math.sin(angle * 0.4) * 0.4), (float)(angle - Math.PI / 4), (float)(angle * 0.03)));
    // camera.uploadDataToShader(shader);
    
    updateMovement(dt);

    shader.uploadVec1f("u_time", (float)t);
    renderer.render();
  }


  private void updateMovement(double dt) {
    cameraSpeed *= Math.pow(1.2, MouseEventListener.getScrollY());

    cameraDirY -= MouseEventListener.getDx() * cameraSensitivity;
    cameraDirX -= MouseEventListener.getDy() * cameraSensitivity;

    if (KeyEventListener.isKeyPressed(GLFW_KEY_W)) {
      cameraPosX -= Math.sin(cameraDirY) * cameraSpeed * dt;
      cameraPosZ -= Math.cos(cameraDirY) * cameraSpeed * dt;
    }
    if (KeyEventListener.isKeyPressed(GLFW_KEY_S)) {
      cameraPosX += Math.sin(cameraDirY) * cameraSpeed * dt;
      cameraPosZ += Math.cos(cameraDirY) * cameraSpeed * dt;
    }
    if (KeyEventListener.isKeyPressed(GLFW_KEY_A)) {
      cameraPosX -= Math.cos(cameraDirY) * cameraSpeed * dt;
      cameraPosZ += Math.sin(cameraDirY) * cameraSpeed * dt;
    }
    if (KeyEventListener.isKeyPressed(GLFW_KEY_D)) {
      cameraPosX += Math.cos(cameraDirY) * cameraSpeed * dt;
      cameraPosZ -= Math.sin(cameraDirY) * cameraSpeed * dt;
    }
    if (KeyEventListener.isKeyPressed(GLFW_KEY_SPACE)) {
      cameraPosY += cameraSpeed * dt;
    }
    if (KeyEventListener.isKeyPressed(GLFW_KEY_V)) {
      cameraPosY -= cameraSpeed * dt;
    }

    setCamera();
  }


  // ==================================================================================================================================================
  // Set camera
  // ==================================================================================================================================================
  private void setCamera() {
    camera.setPosition(new Vector3f((float)cameraPosX, (float)cameraPosY, (float)cameraPosZ));
    camera.setDirection(new Vector3f((float)cameraDirX, (float)cameraDirY, (float)cameraDirZ));
    camera.uploadDataToShader(shader);
  }
}
