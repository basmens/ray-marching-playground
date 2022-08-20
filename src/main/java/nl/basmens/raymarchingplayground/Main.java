package nl.basmens.raymarchingplayground;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.basmens.raymarchingplayground.engine.Window;

public class Main {
  private static final Logger LOGGER = LogManager.getLogger(Main.class);

  public static void main(String[] passedArgs) {
    LOGGER.info("Main method");

    Window window = Window.get();
    window.init();
    window.run();
  }
}
