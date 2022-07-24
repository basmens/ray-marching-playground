package nl.basmens.raymarchingplayground.engine;

import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFWErrorCallbackI;

public class ErrorCallback implements GLFWErrorCallbackI {
  Logger logger;


  public ErrorCallback(Logger logger) {
    this.logger = logger;
  }


  @Override
  public void invoke(int error, long description) {
    logger.error(error + " - " + description);
  }
}
