package FrontEnd.AST;

public class VariableNode extends ExpressionNode {

    private String name;

    public VariableNode(String name) {
        this.setName(name);
    }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
