import java.util.*;

public class Lexer {

    private final String source;
    private int pos = 0;
    private int line = 1;
    private final List<Token> tokens = new ArrayList<>();
    private boolean hasError = false;

    private static final Map<String, TokenType> KEYWORDS = new LinkedHashMap<>();

    static {
        // Review 1 keywords
        KEYWORDS.put("সংখ্যা", TokenType.SHONKHA);
        KEYWORDS.put("বাক্য", TokenType.BAKKHO);
        KEYWORDS.put("দেখাও", TokenType.DEKHAO);

        // Future Review 3 keywords
        KEYWORDS.put("যদি", TokenType.JODI);
        KEYWORDS.put("তাহলে", TokenType.TAHOLE);
        KEYWORDS.put("নাহলে", TokenType.NAHLE);
        KEYWORDS.put("যতক্ষণ", TokenType.JOTOKKHON);
        KEYWORDS.put("শেষ", TokenType.SESH);
    }

    public Lexer(String source) {
        this.source = source;
    }

    public boolean hasError() {
        return hasError;
    }

    public List<Token> tokenize() {
        while (pos < source.length()) {
            skipSpaces();

            if (pos >= source.length()) {
                break;
            }

            char c = source.charAt(pos);

            if (c == '\n') {
                tokens.add(new Token(TokenType.NEWLINE, "\\n", line));
                line++;
                pos++;
                continue;
            }

            if (c == '/' && peek(1) == '/') {
                skipComment();
                continue;
            }

            if (c == '"') {
                readString();
                continue;
            }

            if (isAsciiDigit(c) || isBanglaDigit(c)) {
                readNumber();
                continue;
            }

            if (c == ';') {
                tokens.add(new Token(TokenType.SEMICOLON, ";", line));
                pos++;
                continue;
            }

            if (c == '+') {
                tokens.add(new Token(TokenType.PLUS, "+", line));
                pos++;
                continue;
            }

            if (c == '-') {
                tokens.add(new Token(TokenType.MINUS, "-", line));
                pos++;
                continue;
            }

            if (c == '*') {
                tokens.add(new Token(TokenType.MULTIPLY, "*", line));
                pos++;
                continue;
            }

            if (c == '/') {
                tokens.add(new Token(TokenType.DIVIDE, "/", line));
                pos++;
                continue;
            }

            if (c == '=') {
                if (peek(1) == '=') {
                    tokens.add(new Token(TokenType.EQUALS, "==", line));
                    pos += 2;
                } else {
                    tokens.add(new Token(TokenType.ASSIGN, "=", line));
                    pos++;
                }
                continue;
            }

            if (c == '>') {
                if (peek(1) == '=') {
                    tokens.add(new Token(TokenType.GREATER_EQ, ">=", line));
                    pos += 2;
                } else {
                    tokens.add(new Token(TokenType.GREATER, ">", line));
                    pos++;
                }
                continue;
            }

            if (c == '<') {
                if (peek(1) == '=') {
                    tokens.add(new Token(TokenType.LESS_EQ, "<=", line));
                    pos += 2;
                } else {
                    tokens.add(new Token(TokenType.LESS, "<", line));
                    pos++;
                }
                continue;
            }

            if (c == '!') {
                if (peek(1) == '=') {
                    tokens.add(new Token(TokenType.NOT_EQUALS, "!=", line));
                    pos += 2;
                } else {
                    lexerError("Unknown character: !");
                    pos++;
                }
                continue;
            }

            if (isWordChar(c) || c == '_') {
                readWord();
                continue;
            }

            lexerError("Unknown character: " + c);
            pos++;
        }

        tokens.add(new Token(TokenType.EOF, "", line));
        return tokens;
    }

    private void skipSpaces() {
        while (pos < source.length()) {
            char c = source.charAt(pos);
            if (c == ' ' || c == '\t' || c == '\r') {
                pos++;
            } else {
                break;
            }
        }
    }

    private void skipComment() {
        while (pos < source.length() && source.charAt(pos) != '\n') {
            pos++;
        }
    }

    private char peek(int offset) {
        int index = pos + offset;
        return index < source.length() ? source.charAt(index) : '\0';
    }

    private boolean isAsciiDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isBanglaDigit(char c) {
        return c >= '০' && c <= '৯';
    }

    private boolean isWordChar(char c) {
        return Character.isLetter(c) || (c >= '\u0980' && c <= '\u09FF');
    }

    private boolean isDelimiter(char c) {
        return c == ' ' || c == '\t' || c == '\r' || c == '\n'
                || c == '+' || c == '-' || c == '*' || c == '/'
                || c == '=' || c == '>' || c == '<' || c == '!'
                || c == '"' || c == ';';
    }

    private void readNumber() {
        StringBuilder sb = new StringBuilder();

        while (pos < source.length()) {
            char c = source.charAt(pos);

            if (isBanglaDigit(c)) {
                sb.append(c - '০');
                pos++;
            } else if (isAsciiDigit(c)) {
                sb.append(c);
                pos++;
            } else {
                break;
            }
        }

        tokens.add(new Token(TokenType.NUMBER, sb.toString(), line));
    }

    private void readString() {
        pos++;
        StringBuilder sb = new StringBuilder();

        while (pos < source.length() && source.charAt(pos) != '"') {
            if (source.charAt(pos) == '\n') {
                lexerError("String literal not closed before new line");
                break;
            }

            sb.append(source.charAt(pos));
            pos++;
        }

        if (pos < source.length()) {
            pos++;
        }

        tokens.add(new Token(TokenType.STRING, sb.toString(), line));
    }

    private void readWord() {
        StringBuilder sb = new StringBuilder();

        while (pos < source.length() && !isDelimiter(source.charAt(pos))) {
            sb.append(source.charAt(pos));
            pos++;
        }

        String word = sb.toString();
        TokenType type = KEYWORDS.getOrDefault(word, TokenType.IDENTIFIER);
        tokens.add(new Token(type, word, line));
    }

    private void lexerError(String message) {
        System.err.println("[Lexer Error] line " + line + ": " + message);
        hasError = true;
    }
}