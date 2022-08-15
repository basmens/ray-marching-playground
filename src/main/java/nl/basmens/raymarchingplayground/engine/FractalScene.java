package nl.basmens.raymarchingplayground.engine;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector2i;
import org.joml.Vector3f;

import nl.basmens.raymarchingplayground.renderer.Camera;
import nl.basmens.raymarchingplayground.renderer.FractalRenderer;
import nl.basmens.raymarchingplayground.renderer.Shader;
import nl.basmens.raymarchingplayground.util.Time;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FractalScene extends Scene {
  private static final Logger logger = LogManager.getLogger(FractalScene.class);

  FractalRenderer renderer;
  Shader shader;
  Camera camera;

   private String shaderFilePath = "src/main/resources/shaders/ray_marching.glsl";
   //private String sceneFilePath = "src/main/resources/shaders/scenes/spheres.glsl";
   private String sceneFilePath = "src/main/resources/shaders/scenes/menger_sponge.glsl";

  // private String shaderFilePath = "src/main/resources/shaders/ray_marching_old.glsl";
  // private String sceneFilePath = "src/main/resources/shaders/scenes/ray_marching_old.glsl";

  // private String shaderFilePath = "src/main/resources/shaders/shadertoy_wrapper.glsl";
  // private String sceneFilePath = "src/main/resources/shaders/scenes/shadertoy_scene.glsl";


  // Fill the screen
  


  // ==================================================================================================================================================
  // Initialization
  // ==================================================================================================================================================
  public FractalScene() {
    super();
  }


  @Override
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

    renderer = new FractalRenderer(shader);
    renderer.generateBufferObjects();

    // Generate camera
    camera = new Camera(new Vector3f(0, 1, 6.5f), new Vector3f(0, 0, 0), (float)(Math.PI / 4));

    // Upload uniforms
    camera.uploadDataToShader(shader);
    shader.uploadVec2i("u_resolution", new Vector2i(Window.get().getWidth(), Window.get().getHeight()));
  }


  @Override
  public void update(double dt) {
    double cameraDist = 4;

    double angle = (Time.getTime() * 0.8) % (Math.PI * 2);
    //double angle = -1.5f;
    camera.setPosition(new Vector3f((float)(Math.sin(angle) * cameraDist), 1, (float)(Math.cos(angle) * cameraDist)));
    camera.setDirection(new Vector3f(0, (float)angle, 0));
    camera.uploadDataToShader(shader);

    shader.uploadVec1f("u_time", (float)Time.getTime());
    renderer.render();
  }
}
