package nl.basmens.raymarchingplayground.engine.event_listeners;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

import java.security.InvalidParameterException;

public final class MouseEventListener {
  private static MouseEventListener instance;

  private double posX;
  private double posY;
  private double prevX;
  private double prevY;
  private double scrollX;
  private double scrollY;
  private boolean isDragging;
  private boolean[] buttonsPressed = new boolean[3];

  private boolean hasInitialMousePos;

  // ===================================================================================================================
  // Constructor
  // ===================================================================================================================
  private MouseEventListener() {
    this.posX = 0;
    this.posY = 0;
    this.prevX = 0;
    this.prevY = 0;
    this.scrollX = 0;
    this.scrollY = 0;
  }

  // ===================================================================================================================
  // Singleton get
  // ===================================================================================================================
  public static MouseEventListener get() {
    if (instance == null) {
      instance = new MouseEventListener();
    }

    return instance;
  }

  // ===================================================================================================================
  // MousePosCallback
  // ===================================================================================================================
  public static void mousePosCallBack(long window, double posX, double posY) {
    get().prevX = get().posX;
    get().prevY = get().posY;
    get().posX = posX;
    get().posY = posY;

    if (!get().hasInitialMousePos) {
      get().prevX = get().posX;
      get().prevY = get().posY;
      get().hasInitialMousePos = true;
    }

    get().isDragging = get().buttonsPressed[0] || get().buttonsPressed[1] || get().buttonsPressed[2];
  }

  // ===================================================================================================================
  // MouseButtonCallback
  // ===================================================================================================================
  public static void mouseButtonCallback(long window, int button, int action, int mods) {
    if (button < get().buttonsPressed.length) {
      if (action == GLFW_PRESS) {
        get().buttonsPressed[button] = true;
      } else if (action == GLFW_RELEASE) {
        get().buttonsPressed[button] = false;
        get().isDragging = false;
      }
    }
  }

  // ===================================================================================================================
  // MouseScrollCallback
  // ===================================================================================================================
  public static void mouseScrollCallback(long window, double scrollX, double scrollY) {
    get().scrollX = scrollX;
    get().scrollY = scrollY;
  }

  // ===================================================================================================================
  // EndFrame
  // ===================================================================================================================
  public static void endFrame() {
    get().prevX = get().posX;
    get().prevY = get().posY;
    get().scrollX = 0;
    get().scrollY = 0;
  }

  // ===================================================================================================================
  // Getters
  // ===================================================================================================================
  public static double getX() {
    return get().posX;
  }

  public static double getY() {
    return get().posY;
  }

  public static double getPrevX() {
    return get().prevX;
  }

  public static double getPrevY() {
    return get().prevY;
  }

  public static double getDx() {
    return get().posX - get().prevX;
  }

  public static double getDy() {
    return get().posY - get().prevY;
  }

  public static double getScrollX() {
    return get().scrollX;
  }

  public static double getScrollY() {
    return get().scrollY;
  }

  public static boolean isDragging() {
    return get().isDragging;
  }

  public static boolean isButtonPressed(int button) {
    if (button < get().buttonsPressed.length) {
      return get().buttonsPressed[button];
    } else {
      throw new InvalidParameterException(
          "Button number " + button + " doesn't exist. Maximum button number is " + (get().buttonsPressed.length - 1));
    }
  }
}
