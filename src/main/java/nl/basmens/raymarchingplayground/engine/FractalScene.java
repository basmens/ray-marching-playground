package nl.basmens.raymarchingplayground.engine;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;


public class FractalScene extends Scene {
  private static final Logger logger = LogManager.getLogger(FractalScene.class);


  private String vertexShaderSource = 
  "#version 330 core \n" +
  "\n" +
  "layout (location = 0) in vec3 aPos; \n" +
  "layout (location = 1) in vec4 aColor; \n" +
  "\n" +
  "out vec4 fColor; \n" +
  "\n" +
  "void main() { \n" +
  "  fColor = aColor; \n" +
  "  gl_Position = vec4(aPos, 1); \n" +
  "} \n";

  private String fragmentShaderSource = 
  "#version 330 core \n" +
  "\n" +
  "in vec4 fColor; \n" +
  "\n" +
  "out vec4 color; \n" +
  "\n" +
  "void main() { \n" +
  "  color = fColor; \n" +
  "} \n";

  private int vertexShaderID, fragmentShaderID;
  private int shaderProgram;

  private int vaoID, vboID, eboID;


  private float[] vertexArray = {
    // Position             Color
     0.5f, -0.5f, 0.0f,     1.0f, 0.0f, 0.0f, 1.0f,  // Bottom right  0
    -0.5f,  0.5f, 0.0f,     0.0f, 1.0f, 0.0f, 1.0f,  // Top Left      1
     0.5f,  0.5f, 0.0f,     0.0f, 0.0f, 1.0f, 1.0f,  // Top right     2
    -0.5f, -0.5f, 0.0f,     1.0f, 1.0f, 0.0f, 1.0f   // Bottom Left   3
  };

  // Must be counter-clockwise
  private int[] elementArray = {
    1, 0, 2,  // Top right triangle
    1, 3, 0   // Bottom left triangle
  };


  // =====================================================================================================
  // Initializing
  // =====================================================================================================
  public FractalScene() {
    super();
  }


  @Override
  public void init() {
    logger.info("FractalScene init");

    // =====================================================================================================
    // Compile and link shader
    // =====================================================================================================

    // Vertex shader
    vertexShaderID = glCreateShader(GL_VERTEX_SHADER);
    glShaderSource(vertexShaderID, vertexShaderSource);
    glCompileShader(vertexShaderID);

    int succes = glGetShaderi(vertexShaderID, GL_COMPILE_STATUS);
    if (succes == GL_FALSE) {
      int len = glGetShaderi(vertexShaderID, GL_INFO_LOG_LENGTH);
      logger.error("Default vertex shader failed to compile:\n\t" + glGetShaderInfoLog(vertexShaderID, len));
    }

    // Fragment shader
    fragmentShaderID = glCreateShader(GL_FRAGMENT_SHADER);
    glShaderSource(fragmentShaderID, fragmentShaderSource);
    glCompileShader(fragmentShaderID);

    succes = glGetShaderi(fragmentShaderID, GL_COMPILE_STATUS);
    if (succes == GL_FALSE) {
      int len = glGetShaderi(fragmentShaderID, GL_INFO_LOG_LENGTH);
      logger.error("Default fragment shader failed to compile:\n\t" + glGetShaderInfoLog(fragmentShaderID, len));
    }

    // Link shaders
    shaderProgram = glCreateProgram();
    glAttachShader(shaderProgram, vertexShaderID);
    glAttachShader(shaderProgram, fragmentShaderID);
    glLinkProgram(shaderProgram);

    succes = glGetProgrami(shaderProgram, GL_LINK_STATUS);
    if (succes == GL_FALSE) {
      int len = glGetProgrami(shaderProgram, GL_INFO_LOG_LENGTH);
      logger.error("Default shader failed to link:\n\t" + glGetProgramInfoLog(shaderProgram, len));
    }


    // =====================================================================================================
    // Generate VAO, VBO and EBO buffer objects and send them to the GPU
    // =====================================================================================================
    vaoID = glGenVertexArrays();
    glBindVertexArray(vaoID);

    // Create float buffer of vertices
    FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertexArray.length);
    vertexBuffer.put(vertexArray).flip();

    // Create VBO and vertex buffer
    vboID = glGenBuffers();
    glBindBuffer(GL_ARRAY_BUFFER, vboID);
    glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

    // Create the indicis and upload
    IntBuffer elementBuffer = BufferUtils.createIntBuffer(elementArray.length);
    elementBuffer.put(elementArray).flip();

    eboID = glGenBuffers();
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBuffer, GL_STATIC_DRAW);

    // Add vertex attribute pointers
    int floatSizeBytes = 4;

    int positionSize = 3;
    int colorSize = 4;

    int vertexSizeBytes = (positionSize + colorSize) * floatSizeBytes;
    glVertexAttribPointer(0, positionSize, GL_FLOAT, false, vertexSizeBytes, 0);
    glEnableVertexAttribArray(0);

    glVertexAttribPointer(1, colorSize, GL_FLOAT, false, vertexSizeBytes, positionSize * floatSizeBytes);
    glEnableVertexAttribArray(1);
  }


  @Override
  public void update(float dt) {
    // Bind shader program
    glUseProgram(shaderProgram);

    // Bind VAO
    glBindVertexArray(vaoID);

    // Enable the vertex attribute pointers
    glEnableVertexAttribArray(0);
    glEnableVertexAttribArray(1);

    glDrawElements(GL_TRIANGLES, elementArray.length, GL_UNSIGNED_INT, 0);

    // Unbind everything
    glDisableVertexAttribArray(0);
    glDisableVertexAttribArray(1);
    glBindVertexArray(0); 
    glUseProgram(0);
  }
}
