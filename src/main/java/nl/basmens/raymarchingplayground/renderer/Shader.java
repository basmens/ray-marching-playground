package nl.basmens.raymarchingplayground.renderer;

import static org.lwjgl.opengl.GL30.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Shader {
  private static final Logger logger = LogManager.getLogger(Shader.class);

  private String filePath;

  private String vertexSource, fragmentSource;

  private int vertexShaderID, fragmentShaderID;
  private int shaderProgramID;


  // ==================================================================================================================================================
  // Initialization
  // ==================================================================================================================================================
  public Shader(String filePath) {
    logger.info("Shader constructor");

    this.filePath = filePath;

    try {
      String source = new String(Files.readAllBytes(Paths.get(filePath)));
      String[] splitString = source.split("(#type)( )([a-zA-Z]+)");

      // Find patterns after '#type'
      int index = source.indexOf("#type") + 6;
      int eol = source.indexOf("\n", index);
      String firstPattern = source.substring(index, eol).trim();

      index = source.indexOf("#type", eol) + 6;
      eol = source.indexOf("\n", index);
      String secondPattern = source.substring(index, eol).trim();

      // load source
      if(firstPattern.equals("vertex")) {
        vertexSource = splitString[1];
      } else if(firstPattern.equals("fragment")) {
        fragmentSource = splitString[1];
      } else {
        throw new IOException("Unexpected token '" + firstPattern + "'");
      }

      if(secondPattern.equals("vertex")) {
        vertexSource = splitString[2];
      } else if(secondPattern.equals("fragment")) {
        fragmentSource = splitString[2];
      } else {
        throw new IOException("Unexpected token '" + secondPattern + "'");
      }

    } catch(IOException e) {
      logger.error("Failed to load file '" + filePath + "'", e);
      assert false : "";
    }
  }


  // ==================================================================================================================================================
  // Compile and link shader
  // ==================================================================================================================================================
  public void compile() {
    // Vertex shader
    vertexShaderID = glCreateShader(GL_VERTEX_SHADER);
    glShaderSource(vertexShaderID, vertexSource);
    glCompileShader(vertexShaderID);

    int succes = glGetShaderi(vertexShaderID, GL_COMPILE_STATUS);
    if (succes == GL_FALSE) {
      int len = glGetShaderi(vertexShaderID, GL_INFO_LOG_LENGTH);
      logger.error("'" + filePath + "' vertex shader failed to compile:\n\t" + glGetShaderInfoLog(vertexShaderID, len));
      assert false : "";
    }

    // Fragment shader
    fragmentShaderID = glCreateShader(GL_FRAGMENT_SHADER);
    glShaderSource(fragmentShaderID, fragmentSource);
    glCompileShader(fragmentShaderID);

    succes = glGetShaderi(fragmentShaderID, GL_COMPILE_STATUS);
    if (succes == GL_FALSE) {
      int len = glGetShaderi(fragmentShaderID, GL_INFO_LOG_LENGTH);
      logger.error("'" + filePath + "' fragment shader failed to compile:\n\t" + glGetShaderInfoLog(fragmentShaderID, len));
      assert false : "";
    }

    // Link shaders
    shaderProgramID = glCreateProgram();
    glAttachShader(shaderProgramID, vertexShaderID);
    glAttachShader(shaderProgramID, fragmentShaderID);
    glLinkProgram(shaderProgramID);

    succes = glGetProgrami(shaderProgramID, GL_LINK_STATUS);
    if (succes == GL_FALSE) {
      int len = glGetProgrami(shaderProgramID, GL_INFO_LOG_LENGTH);
      logger.error("'" + filePath + "' shader failed to link:\n\t" + glGetProgramInfoLog(shaderProgramID, len));
      assert false : "";
    }
  }


  // ==================================================================================================================================================
  // Use shader
  // ==================================================================================================================================================
  public void use() {
    glUseProgram(shaderProgramID);
  }


  // ==================================================================================================================================================
  // Detach shader
  // ==================================================================================================================================================
  public void detach() {
    glUseProgram(0);
  }
}
