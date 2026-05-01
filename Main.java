import java.nio.file.*;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        try {
            String fileName = args.length > 0 ? args[0] : "sample.bhasha";
            String source = Files.readString(Path.of(fileName));

            Lexer lexer = new Lexer(source);
            List<Token> tokens = lexer.tokenize();

            System.out.println("===== REVIEW 2: TOKENS =====");
            for (Token token : tokens) {
                System.out.println(token);
            }

            Parser parser = new Parser(tokens);
            ASTNode program = parser.parseProgram();

            System.out.println("\n===== REVIEW 2: PARSER RESULT =====");
            System.out.println("Total statements parsed: " + program.statements.size());

            SymbolTable symbolTable = parser.getSymbolTable();
            symbolTable.printTable();

            if (lexer.hasError() || parser.hasError || symbolTable.hasError) {
                System.out.println("\nReview 2 has errors. Code generation stopped.");
                return;
            }

            CodeGenerator generator = new CodeGenerator(symbolTable);
            String javaCode = generator.generate(program);

            Files.writeString(Path.of("ShobujOutput.java"), javaCode);

            System.out.println("\n===== REVIEW 2: GENERATED JAVA CODE =====");
            System.out.println(javaCode);

            System.out.println("Generated file: ShobujOutput.java");

        } catch (Exception e) {
            System.err.println("Compiler error: " + e.getMessage());
        }
    }
}