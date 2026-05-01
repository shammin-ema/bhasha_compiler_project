import java.util.*;

public class Parser {

    private final List<Token> tokens;
    private int pos = 0;
    private final SymbolTable symbolTable = new SymbolTable();
    public boolean hasError = false;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    private Token peek() {
        return tokens.get(pos);
    }

    private Token peekAt(int offset) {
        int index = pos + offset;
        return index < tokens.size() ? tokens.get(index) : tokens.get(tokens.size() - 1);
    }

    private Token consume() {
        return tokens.get(pos++);
    }

    private boolean check(TokenType type) {
        return peek().type == type;
    }

    private boolean checkAny(TokenType... types) {
        for (TokenType type : types) {
            if (peek().type == type) return true;
        }
        return false;
    }

    private void skipNewlines() {
        while (check(TokenType.NEWLINE)) {
            consume();
        }
    }

    public ASTNode parseProgram() {
        ASTNode program = new ASTNode(ASTNode.NodeType.PROGRAM);

        skipNewlines();

        while (!check(TokenType.EOF)) {
            ASTNode statement = parseStatement();
            if (statement != null) {
                program.statements.add(statement);
            }
            skipNewlines();
        }

        return program;
    }

    private ASTNode parseStatement() {
        if (check(TokenType.SHONKHA) || check(TokenType.BAKKHO)) {
            return parseDeclaration();
        }

        if (check(TokenType.DEKHAO)) {
            return parsePrint();
        }

        if (check(TokenType.JODI)) {
            return parseIf();
        }

        if (check(TokenType.JOTOKKHON)) {
            return parseWhile();
        }

        if (check(TokenType.IDENTIFIER) && peekAt(1).type == TokenType.ASSIGN) {
            return parseAssignment();
        }

        if (check(TokenType.SEMICOLON) || check(TokenType.NEWLINE)) {
            consume();
            return null;
        }

        syntaxError("Unexpected token: " + peek().value);
        recoverToNewlineOrSemicolon();
        return null;
    }

    private ASTNode parseDeclaration() {
        Token typeToken = consume();
        String dataType = typeToken.value;

        Token nameToken = expect(TokenType.IDENTIFIER);
        expect(TokenType.ASSIGN);

        ASTNode expression = parseExpression();
        String expressionType = inferType(expression);

        if (expressionType != null && !dataType.equals(expressionType)) {
            semanticError("Type mismatch: variable '" + nameToken.value
                    + "' is " + dataType + " but value is " + expressionType, nameToken.line);
        }

        symbolTable.declare(nameToken.value, dataType, nameToken.line);

        ASTNode node = new ASTNode(ASTNode.NodeType.DECL, nameToken.value);
        node.dataType = dataType;
        node.right = expression;
        return node;
    }

    private ASTNode parseAssignment() {
        Token nameToken = consume();

        if (!symbolTable.isDeclared(nameToken.value)) {
            semanticError("Undeclared variable: " + nameToken.value, nameToken.line);
        }

        expect(TokenType.ASSIGN);

        ASTNode expression = parseExpression();

        String variableType = symbolTable.typeOf(nameToken.value);
        String expressionType = inferType(expression);

        if (variableType != null && expressionType != null && !variableType.equals(expressionType)) {
            semanticError("Type mismatch in assignment: " + nameToken.value, nameToken.line);
        }

        ASTNode node = new ASTNode(ASTNode.NodeType.ASSIGN, nameToken.value);
        node.right = expression;
        return node;
    }

    private ASTNode parsePrint() {
        consume();

        ASTNode node = new ASTNode(ASTNode.NodeType.PRINT);
        node.right = parseExpression();

        return node;
    }

    private ASTNode parseIf() {
        consume(); // যদি

        ASTNode condition = parseCondition();

        expect(TokenType.TAHOLE);
        skipNewlines();

        ASTNode node = new ASTNode(ASTNode.NodeType.IF);
        node.condition = condition;

        while (!checkAny(TokenType.NAHLE, TokenType.SESH, TokenType.EOF)) {
            ASTNode statement = parseStatement();

            if (statement != null) {
                node.thenBody.add(statement);
            }

            skipNewlines();
        }

        if (check(TokenType.NAHLE)) {
            consume();
            skipNewlines();

            while (!checkAny(TokenType.SESH, TokenType.EOF)) {
                ASTNode statement = parseStatement();

                if (statement != null) {
                    node.elseBody.add(statement);
                }

                skipNewlines();
            }
        }

        expect(TokenType.SESH);

        return node;
    }

    private ASTNode parseWhile() {
        consume(); // যতক্ষণ

        ASTNode condition = parseCondition();

        expect(TokenType.TAHOLE);
        skipNewlines();

        ASTNode node = new ASTNode(ASTNode.NodeType.WHILE);
        node.condition = condition;

        while (!checkAny(TokenType.SESH, TokenType.EOF)) {
            ASTNode statement = parseStatement();

            if (statement != null) {
                node.loopBody.add(statement);
            }

            skipNewlines();
        }

        expect(TokenType.SESH);

        return node;
    }

    private ASTNode parseCondition() {
        ASTNode left = parseExpression();

        if (!checkAny(TokenType.GREATER, TokenType.LESS,
                TokenType.GREATER_EQ, TokenType.LESS_EQ,
                TokenType.EQUALS, TokenType.NOT_EQUALS)) {
            syntaxError("Comparison operator expected in condition");
            return left;
        }

        Token operator = consume();
        ASTNode right = parseExpression();

        ASTNode node = new ASTNode(ASTNode.NodeType.BINOP, operator.value);
        node.left = left;
        node.right = right;

        return node;
    }

    private ASTNode parseExpression() {
        ASTNode left = parseTerm();

        while (check(TokenType.PLUS) || check(TokenType.MINUS)) {
            String operator = consume().value;
            ASTNode right = parseTerm();

            ASTNode node = new ASTNode(ASTNode.NodeType.BINOP, operator);
            node.left = left;
            node.right = right;

            left = node;
        }

        return left;
    }

    private ASTNode parseTerm() {
        ASTNode left = parsePrimary();

        while (check(TokenType.MULTIPLY) || check(TokenType.DIVIDE)) {
            String operator = consume().value;
            ASTNode right = parsePrimary();

            ASTNode node = new ASTNode(ASTNode.NodeType.BINOP, operator);
            node.left = left;
            node.right = right;

            left = node;
        }

        return left;
    }

    private ASTNode parsePrimary() {
        Token token = peek();

        if (check(TokenType.NUMBER)) {
            consume();
            return new ASTNode(ASTNode.NodeType.NUMBER, token.value);
        }

        if (check(TokenType.STRING)) {
            consume();
            return new ASTNode(ASTNode.NodeType.STRING, token.value);
        }

        if (check(TokenType.IDENTIFIER)) {
            consume();

            if (!symbolTable.isDeclared(token.value)) {
                semanticError("Undeclared variable: " + token.value, token.line);
            }

            return new ASTNode(ASTNode.NodeType.IDENTIFIER, token.value);
        }

        syntaxError("Expected value but found: " + token.value);
        recoverToNewlineOrSemicolon();
        return new ASTNode(ASTNode.NodeType.NUMBER, "0");
    }

    private String inferType(ASTNode node) {
        if (node == null) return null;

        return switch (node.nodeType) {
            case NUMBER -> "সংখ্যা";
            case STRING -> "বাক্য";
            case IDENTIFIER -> symbolTable.typeOf(node.value);
            case BINOP -> {
                String leftType = inferType(node.left);
                String rightType = inferType(node.right);

                if ("বাক্য".equals(leftType) || "বাক্য".equals(rightType)) {
                    yield "বাক্য";
                }

                yield "সংখ্যা";
            }
            default -> null;
        };
    }

    private Token expect(TokenType expected) {
        if (check(expected)) {
            return consume();
        }

        syntaxError("Expected " + expected + " but found " + peek().type);
        recoverToNewlineOrSemicolon();
        return new Token(expected, "", peek().line);
    }

    private void recoverToNewlineOrSemicolon() {
        while (!checkAny(TokenType.NEWLINE, TokenType.SEMICOLON, TokenType.EOF)) {
            consume();
        }

        if (check(TokenType.NEWLINE) || check(TokenType.SEMICOLON)) {
            consume();
        }
    }

    private void syntaxError(String message) {
        System.err.println("[Syntax Error] line " + peek().line + ": " + message);
        hasError = true;
    }

    private void semanticError(String message, int line) {
        System.err.println("[Semantic Error] line " + line + ": " + message);
        hasError = true;
    }
}