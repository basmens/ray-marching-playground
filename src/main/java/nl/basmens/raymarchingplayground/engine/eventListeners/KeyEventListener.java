package nl.basmens.raymarchingplayground.engine.eventListeners;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

import java.security.InvalidParameterException;

public class KeyEventListener {
  private static KeyEventListener instance = null;

  private boolean[] keysPressed = new boolean[350];


  // ==================================================================================================================================================
  // Constructor
  // ==================================================================================================================================================
  private KeyEventListener() {

  }


  // ==================================================================================================================================================
  // Singleton get
  // ==================================================================================================================================================
  public static KeyEventListener get() {
    if (instance == null) {
      instance = new KeyEventListener();
    }

    return instance;
  }


  // ==================================================================================================================================================
  // KeyCallBack
  // ==================================================================================================================================================
  public static void keyCallBack(long window, int key, int scanCode, int action, int mods) {
    if (action == GLFW_PRESS) {
      get().keysPressed[key] = true;
    } else if (action == GLFW_RELEASE) {
      get().keysPressed[key] = false;
    }
  }


  // ==================================================================================================================================================
  // Getters
  // ==================================================================================================================================================
  public static boolean isKeyPressed(int key) {
    if (key < get().keysPressed.length) {
      return get().keysPressed[key];
    } else {
      throw new InvalidParameterException("Key number " + key + " doesn't exist. Maximum key number is " + (get().keysPressed.length - 1));
    }
  }
}
