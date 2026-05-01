public enum TokenType {
    // Data types
    SHONKHA,        // সংখ্যা
    BAKKHO,         // বাক্য

    // Output
    DEKHAO,         // দেখাও

    // Future keywords for Review 3
    JODI,           // যদি
    TAHOLE,         // তাহলে
    NAHLE,          // নাহলে
    JOTOKKHON,      // যতক্ষণ
    SESH,           // শেষ

    // Literals and identifier
    NUMBER,
    STRING,
    IDENTIFIER,

    // Operators
    PLUS,
    MINUS,
    MULTIPLY,
    DIVIDE,
    ASSIGN,

    // Future comparison operators for Review 3
    GREATER,
    LESS,
    GREATER_EQ,
    LESS_EQ,
    EQUALS,
    NOT_EQUALS,

    // Misc
    SEMICOLON,
    NEWLINE,
    EOF
}