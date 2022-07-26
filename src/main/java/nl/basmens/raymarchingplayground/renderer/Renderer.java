package nl.basmens.raymarchingplayground.renderer;

public abstract class Renderer {
  protected Shader shader;

  protected int vaoID, vboID, eboID;


  Renderer(Shader shader) {
    this.shader = shader;
  }


  public abstract void generateBufferObjects();
  public abstract void render();
}
