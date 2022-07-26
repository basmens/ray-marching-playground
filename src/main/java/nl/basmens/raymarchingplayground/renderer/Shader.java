package nl.basmens.raymarchingplayground.renderer;

import static org.lwjgl.opengl.GL30.*;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;
import org.joml.Vector4i;
import org.lwjgl.BufferUtils;

public class Shader {
  private static final Logger logger = LogManager.getLogger(Shader.class);

  private String filePath;

  private String vertexSource, fragmentSource;

  private int vertexShaderID, fragmentShaderID;
  private int shaderProgramID;

  private boolean beingUsed = false;


  // ==================================================================================================================================================
  // Initialization
  // ==================================================================================================================================================
  public Shader(String filePath) {
    logger.info("Shader constructor");

    this.filePath = filePath;

    vertexSource = Shader.readVertexShaderFile(filePath);
    fragmentSource = Shader.readFragmentShaderFile(filePath);
  }


  // ==================================================================================================================================================
  // Read source file
  // ==================================================================================================================================================
  public static String readVertexShaderFile(String filePath) {
    try {
      String source = new String(Files.readAllBytes(Paths.get(filePath)));

      Pattern p = Pattern.compile("(#type)( )(vertex)", Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(source);
      if (m.find()) {
        int startIndex = source.indexOf("\n", m.start());
        int endIndex = source.indexOf("#type", startIndex);

        if (endIndex == -1)
          endIndex = source.length();

        return source.substring(startIndex, endIndex);
      } else {
        throw new IOException("The given file must contain a vertex shader");
      }

    } catch(IOException e) {
      logger.error("Failed to load file '" + filePath + "'", e);
      assert false : "";
      return null;
    }
  }

  public static String readFragmentShaderFile(String filePath) {
    try {
      String source = new String(Files.readAllBytes(Paths.get(filePath)));

      Pattern p = Pattern.compile("(#type)( )(fragment)", Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(source);
      if (m.find()) {
        int startIndex = source.indexOf("\n", m.start());
        if (startIndex == -1)
          startIndex = source.length();

        int endIndex = source.indexOf("#type", startIndex);
        if (endIndex == -1)
          endIndex = source.length();

        return source.substring(startIndex, endIndex);
      } else {
        throw new IOException("The given file must contain a fragment shader");
      }

    } catch(IOException e) {
      logger.error("Failed to load file '" + filePath + "'", e);
      assert false : "";
      return null;
    }
  }


  // ==================================================================================================================================================
  // Getters and Setters
  // ==================================================================================================================================================
  public String getVertexSource() {
    return vertexSource;
  }


  public void setVertexSource(String vertexSource) {
    this.vertexSource = vertexSource;
  }


  public String getFragmentSource() {
    return fragmentSource;
  }


  public void setFragmentSource(String fragmentSource) {
    this.fragmentSource = fragmentSource;
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
    if (!beingUsed) {
      glUseProgram(shaderProgramID);
      beingUsed = true;
    }
  }


  // ==================================================================================================================================================
  // Detach shader
  // ==================================================================================================================================================
  public void detach() {
    if (beingUsed) {
      glUseProgram(0);
      beingUsed = false;
    }
  }


  // ==================================================================================================================================================
  // Upload uniforms matrices
  // ==================================================================================================================================================
  public void uploadMat4f(String varName, Matrix4f mat4) {
    int varLocation = glGetUniformLocation(shaderProgramID, varName);
    use();
    FloatBuffer matBuffer = BufferUtils.createFloatBuffer(16);
    mat4.get(matBuffer);
    glUniformMatrix4fv(varLocation, false, matBuffer);
  }

  public void uploadMat3f(String varName, Matrix3f mat3) {
      int varLocation = glGetUniformLocation(shaderProgramID, varName);
      use();
      FloatBuffer matBuffer = BufferUtils.createFloatBuffer(9);
      mat3.get(matBuffer);
      glUniformMatrix3fv(varLocation, false, matBuffer);
  }


  // ==================================================================================================================================================
  // Upload uniforms floats
  // ==================================================================================================================================================
  public void uploadVec4f(String varName, Vector4f vec) {
      int varLocation = glGetUniformLocation(shaderProgramID, varName);
      use();
      glUniform4f(varLocation, vec.x, vec.y, vec.z, vec.w);
  }
  // public void uploadVec4f(String varName, float x, float y, float z, float w) {
  //   int varLocation = glGetUniformLocation(shaderProgramID, varName);
  //   use();
  //   glUniform4f(varLocation, x, y, z, w);
  // }

  public void uploadVec3f(String varName, Vector3f vec) {
      int varLocation = glGetUniformLocation(shaderProgramID, varName);
      use();
      glUniform3f(varLocation, vec.x, vec.y, vec.z);
  }
  // public void uploadVec3f(String varName, float x, float y, float z) {
  //   int varLocation = glGetUniformLocation(shaderProgramID, varName);
  //   use();
  //   glUniform3f(varLocation, x, y, z);
  // }

  public void uploadVec2f(String varName, Vector2f vec) {
      int varLocation = glGetUniformLocation(shaderProgramID, varName);
      use();
      glUniform2f(varLocation, vec.x, vec.y);
  }
  // public void uploadVec2f(String varName, float x, float y) {
  //   int varLocation = glGetUniformLocation(shaderProgramID, varName);
  //   use();
  //   glUniform2f(varLocation, x, y);
  // }

  public void uploadVec1f(String varName, float val) {
      int varLocation = glGetUniformLocation(shaderProgramID, varName);
      use();
      glUniform1f(varLocation, val);
  }


  // ==================================================================================================================================================
  // Upload uniforms ints
  // ==================================================================================================================================================
  public void uploadVec4i(String varName, Vector4i vec) {
    int varLocation = glGetUniformLocation(shaderProgramID, varName);
    use();
    glUniform4i(varLocation, vec.x, vec.y, vec.z, vec.w);
  }
  // public void uploadVec4i(String varName, int x, int y, int z, int w) {
  //   int varLocation = glGetUniformLocation(shaderProgramID, varName);
  //   use();
  //   glUniform4i(varLocation, x, y, z, w);
  // }

  public void uploadVec3i(String varName, Vector3i vec) {
    int varLocation = glGetUniformLocation(shaderProgramID, varName);
    use();
    glUniform3i(varLocation, vec.x, vec.y, vec.z);
  }
  // public void uploadVec3i(String varName, int x, int y, int z) {
  //   int varLocation = glGetUniformLocation(shaderProgramID, varName);
  //   use();
  //   glUniform3i(varLocation, x, y, z);
  // }

  public void uploadVec2i(String varName, Vector2i vec) {
    int varLocation = glGetUniformLocation(shaderProgramID, varName);
    use();
    glUniform2i(varLocation, vec.x, vec.y);
  }
  // public void uploadVec2i(String varName, int x, int y) {
  //   int varLocation = glGetUniformLocation(shaderProgramID, varName);
  //   use();
  //   glUniform2i(varLocation, x, y);
  // }
  
  public void uploadVec1i(String varName, int val) {
      int varLocation = glGetUniformLocation(shaderProgramID, varName);
      use();
      glUniform1i(varLocation, val);
  }
}
