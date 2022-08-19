package nl.basmens.raymarchingplayground.renderer;


import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;


public class Renderer {
  protected Shader shader;

  protected int vaoID, vboID, eboID;


  private float[] vertexArray = {
    1f, -1f, 0f,  // Bottom right  0
   -1f,  1f, 0f,  // Top Left      1
    1f,  1f, 0f,  // Top right     2
   -1f, -1f, 0f   // Bottom Left   3
  };

  // Must be counter-clockwise
  private int[] elementArray = {
    1, 0, 2,  // Top right triangle
    1, 3, 0   // Bottom left triangle
  };


  public Renderer(Shader shader) {
    this.shader = shader;
  }


  // ==================================================================================================================================================
  // Generate VAO, VBO and EBO buffer objects and send them to the GPU
  // ==================================================================================================================================================
  public void generateBufferObjects() {
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
    int positionSize = 3;
    int vertexSizeBytes = positionSize * Float.BYTES;

    glVertexAttribPointer(0, positionSize, GL_FLOAT, false, vertexSizeBytes, 0);
    glEnableVertexAttribArray(0);
  }


  // ==================================================================================================================================================
  // Render
  // ==================================================================================================================================================
  public void render() {
    shader.use();

    // Bind VAO
    glBindVertexArray(vaoID);

    // Enable the vertex attribute pointers
    glEnableVertexAttribArray(0);

    glDrawElements(GL_TRIANGLES, elementArray.length, GL_UNSIGNED_INT, 0);

    // Unbind everything
    glDisableVertexAttribArray(0);
    glBindVertexArray(0); 
   
    shader.detach();
  }
}
