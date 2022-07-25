package nl.basmens.raymarchingplayground.engine;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;

import nl.basmens.raymarchingplayground.renderer.Shader;

import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;


public class FractalScene extends Scene {
  private static final Logger logger = LogManager.getLogger(FractalScene.class);

  Shader defaultShader;

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
  // Initialization
  // =====================================================================================================
  public FractalScene() {
    super();
  }


  @Override
  public void init() {
    logger.info("FractalScene init");

    defaultShader = new Shader("src/main/resources/shaders/defaultShader.glsl");
    defaultShader.compile();

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
    defaultShader.use();

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
   
    defaultShader.detach();
  }
}
