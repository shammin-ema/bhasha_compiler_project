import java.util.*;

public class ASTNode {

    public enum NodeType {
        PROGRAM,

        // Review 1
        DECL,
        ASSIGN,
        PRINT,
        BINOP,
        NUMBER,
        STRING,
        IDENTIFIER,

        // Future Review 3
        IF,
        WHILE
    }

    public NodeType nodeType;
    public String value;
    public String dataType;

    public ASTNode left;
    public ASTNode right;

    // Future fields for Review 3
    public ASTNode condition;
    public List<ASTNode> thenBody = new ArrayList<>();
    public List<ASTNode> elseBody = new ArrayList<>();
    public List<ASTNode> loopBody = new ArrayList<>();

    public List<ASTNode> statements = new ArrayList<>();

    public ASTNode(NodeType type) {
        this.nodeType = type;
    }

    public ASTNode(NodeType type, String value) {
        this.nodeType = type;
        this.value = value;
    }
}