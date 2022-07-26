package nl.basmens.raymarchingplayground.engine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import nl.basmens.raymarchingplayground.util.time;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;


public class Window {
  public static Window window = null;

  private static final Logger logger = LogManager.getLogger(Window.class);

  private static Scene currentScene;

  private long glfwMonitor;
  private long glfwWindow;
  private int width, height;
  private String title;


  // ==================================================================================================================================================
  // Constructor
  // ==================================================================================================================================================
  private Window() {
    logger.info("Window init");
    logger.info("Hello LWJGL " + Version.getVersion() + "!");

    this.title = "Ray Marching Playground";


    // Setup error callback
    GLFW.glfwSetErrorCallback(new ErrorCallback(logger));

    // Initialize GLFW
    if (!glfwInit())
			throw new IllegalStateException("Unable to initialize GLFW");

		// Configure GLFW
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
		//glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);

    // Create window
    glfwMonitor = glfwGetPrimaryMonitor();
    GLFWVidMode videoMode = glfwGetVideoMode(glfwMonitor);
    this.width = videoMode.width();
    this.height = videoMode.height();

    glfwWindow = glfwCreateWindow(this.width, this.height, this.title, glfwGetPrimaryMonitor(), NULL);
		if (glfwWindow == NULL)
			throw new RuntimeException("Failed to create the GLFW window");

    // Make the OpenGL context current
    glfwMakeContextCurrent(glfwWindow);

    // Enable v-sync
    glfwSwapInterval(1);

    // Make the window visible
    glfwShowWindow(glfwWindow);

    GL.createCapabilities();

    // Make scene
    currentScene = new FractalScene();
    currentScene.init();
  }


  // ==================================================================================================================================================
  // Singleton get
  // ==================================================================================================================================================
  public static Window get() {
    if (Window.window == null) {
      Window.window = new Window();
    }
    
    return window;
  }
  

  // ==================================================================================================================================================
  // Run
  // ==================================================================================================================================================
  public void run() {
    float beginTime = time.getTime();
    float endTime = time.getTime();
    float deltaTime = -1;

    while(!glfwWindowShouldClose(glfwWindow)) {
      // Poll events
      glfwPollEvents();

      // float r = (float)Math.sin(time.getTime() + Math.PI / 3 * 0);
      // float g = (float)Math.sin(time.getTime() + Math.PI / 3 * 2);
      // float b = (float)Math.sin(time.getTime() + Math.PI / 3 * 4);
      float r = 1.5f - Math.abs((time.getTime() + 0) % 3 - 1.5f);
      float g = 1.5f - Math.abs((time.getTime() + 1) % 3 - 1.5f);
      float b = 1.5f - Math.abs((time.getTime() + 2) % 3 - 1.5f);

      glClearColor(r, g, b, 1);
      glClear(GL_COLOR_BUFFER_BIT);

      if (deltaTime >= 0)
        currentScene.update(deltaTime);

      glfwSwapBuffers(glfwWindow);

      endTime = time.getTime();
      deltaTime = endTime - beginTime;
      beginTime = endTime;
    }
  }
}
