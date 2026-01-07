// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Hashtable;

import static jminusminus.TokenKind.*;

/**
 * A lexical analyzer for j--, that has no backtracking mechanism.
 */
class Scanner {
    // End of file character.
    public final static char EOFCH = CharReader.EOFCH;

    // Keywords in j--.
    private Hashtable<String, TokenKind> reserved;

    // Source characters.
    private CharReader input;

    // Next unscanned character.
    private char ch;

    // Whether a scanner error has been found.
    private boolean isInError;

    // Source file name.
    private String fileName;

    // Line number of current token.
    private int line;

    /**
     * Constructs a Scanner from a file name.
     *
     * @param fileName name of the source file.
     * @throws FileNotFoundException when the named file cannot be found.
     */
    public Scanner(String fileName) throws FileNotFoundException {
        this.input = new CharReader(fileName);
        this.fileName = fileName;
        isInError = false;

        // Keywords in j--
        reserved = new Hashtable<String, TokenKind>();
        reserved.put(ABSTRACT.image(), ABSTRACT);
        reserved.put(BOOLEAN.image(), BOOLEAN);
        reserved.put(BREAK.image(), BREAK); // Add BREAK
        reserved.put(CASE.image(), CASE); // Add CASE
        reserved.put(CATCH.image(), CATCH); // Add CATCH
        reserved.put(CHAR.image(), CHAR);
        reserved.put(CLASS.image(), CLASS);
        reserved.put(CONTINUE.image(), CONTINUE); // Add CONTINUE
        reserved.put(DEFLT.image(), DEFLT); // Add DEFAULT
        reserved.put(DO.image(), DO); // Add DO
        reserved.put(DOUBLE.image(), DOUBLE); // Add DOUBLE
        reserved.put(ELSE.image(), ELSE);
        reserved.put(EXTENDS.image(), EXTENDS);
        reserved.put(FALSE.image(), FALSE);
        reserved.put(FINALLY.image(), FINALLY); // Add FINALLY
        reserved.put(FOR.image(), FOR); // Add FOR
        reserved.put(IF.image(), IF);
        reserved.put(IMPLEMENTS.image(), IMPLEMENTS); // Add IMPLEMENTS
        reserved.put(IMPORT.image(), IMPORT);
        reserved.put(INSTANCEOF.image(), INSTANCEOF);
        reserved.put(INT.image(), INT);
        reserved.put(INTERFACE.image(), INTERFACE); // Add INTERFACE
        reserved.put(LONG.image(), LONG); // Add LONG
        reserved.put(NEW.image(), NEW);
        reserved.put(NULL.image(), NULL);
        reserved.put(PACKAGE.image(), PACKAGE);
        reserved.put(PRIVATE.image(), PRIVATE);
        reserved.put(PROTECTED.image(), PROTECTED);
        reserved.put(PUBLIC.image(), PUBLIC);
        reserved.put(RETURN.image(), RETURN);
        reserved.put(STATIC.image(), STATIC);
        reserved.put(SUPER.image(), SUPER);
        reserved.put(SWITCH.image(), SWITCH); // Add SWITCH
        reserved.put(THIS.image(), THIS);
        reserved.put(THROW.image(), THROW); // Add THROW
        reserved.put(THROWS.image(), THROWS); // Add THROWS
        reserved.put(TRUE.image(), TRUE);
        reserved.put(TRY.image(), TRY); // Add TRY
        reserved.put(VOID.image(), VOID);
        reserved.put(WHILE.image(), WHILE);

        // Prime the pump.
        nextCh();
    }

    /**
     * Scans and returns the next token from input.
     *
     * @return the next scanned token.
     */
    public TokenInfo getNextToken() {
        StringBuffer buffer;
        boolean moreWhiteSpace = true;
        while (moreWhiteSpace) {
            while (isWhitespace(ch)) {
                nextCh();
            }
            if (ch == '/') {
                nextCh();
                if (ch == '/') {
                    // CharReader maps all new lines to '\n'.
                    while (ch != '\n' && ch != EOFCH) {
                        nextCh();
                    }
                } else if (ch == '*'){  // Add multi-line comment capability
                    nextCh();  // Read the next char
                    boolean commentFlag = false;
                    while (!commentFlag){
                        if (ch == '*'){ // If the next char is another '*'
                            nextCh();  // Read next char
                            if (ch == '/'){ // if the char after '*' is a '/'
                                nextCh();
                                commentFlag = true; // break the loop
                            } else {
                                nextCh(); // Otherwise loop the characters
                            }
                        } else {
                            nextCh(); // If the char is not '*', loop the chars
                        }
                    }
                } else if (ch == '='){
                    nextCh();
                    return new TokenInfo(DIV_ASSIGN, line); // Add /= operations
                } else {
                    // old -> reportScannerError("Operator / is not supported in j--");
                    return new TokenInfo(DIV, line);  // Adding division operations
                }
            } else {
                moreWhiteSpace = false;
            }
        }
        line = input.line();
        switch (ch) {
            case ',':
                nextCh();
                return new TokenInfo(COMMA, line);
            case '.':
                buffer = new StringBuffer();
                buffer.append(ch);
                nextCh();
                if (!isDigit(ch)){
                    // If there are no digits, then it's just the DOT '.'
                    return new TokenInfo(DOT, line);
                }
                readDigits(buffer); // readDigits reads all the next chars as long as they are digits, and appends them
                if (ch == 'd' || ch == 'D'){
                    buffer.append(ch);
                    nextCh();
                    return new TokenInfo(DOUBLE_LITERAL, buffer.toString(), line); // Ex: .12345d
                }
                // exponentCheck checks if the next char is 'e' or 'E' along with the optional '-' or '+'
                if (exponentCheck(buffer)) {
                    // Exponents MUST be followed by at least one digit
                    if (!isDigit(ch)){
                        reportScannerError("Invalid syntax; exponent must be followed by at least one digit\n");
                    }
                    readDigits(buffer);
                    if (ch == 'd' || ch == 'D'){
                        buffer.append(ch);
                        nextCh();
                    }
                    return new TokenInfo(DOUBLE_LITERAL, buffer.toString(), line); // Ex: .1234e(+)S12(d)
                }
                return new TokenInfo(DOUBLE_LITERAL, buffer.toString(), line); // Ex: .12345
            case '[':
                nextCh();
                return new TokenInfo(LBRACK, line);
            case '{':
                nextCh();
                return new TokenInfo(LCURLY, line);
            case '(':
                nextCh();
                return new TokenInfo(LPAREN, line);
            case ']':
                nextCh();
                return new TokenInfo(RBRACK, line);
            case '}':
                nextCh();
                return new TokenInfo(RCURLY, line);
            case ')':
                nextCh();
                return new TokenInfo(RPAREN, line);
            case ';':
                nextCh();
                return new TokenInfo(SEMI, line);
            case '*':
                nextCh();
                if (ch == '='){
                    nextCh();
                    return new TokenInfo(STAR_ASSIGN, line); // Add *= operations
                }
                return new TokenInfo(STAR, line);
            case '%':
                nextCh();
                if (ch == '='){
                    nextCh();
                    return new TokenInfo(REM_ASSIGN, line); // Add %= operations
                }
                return new TokenInfo(REM, line);  // Add modulo operations
            case '?':
                nextCh();
                return new TokenInfo(QUESTION, line); // Add ? operations
            case ':':
                nextCh();
                return new TokenInfo(COLON, line); // Add : operations
            case '+':
                nextCh();
                if (ch == '=') {
                    nextCh();
                    return new TokenInfo(PLUS_ASSIGN, line);
                } else if (ch == '+') {
                    nextCh();
                    return new TokenInfo(INC, line);
                } else {
                    return new TokenInfo(PLUS, line);
                }
            case '-':
                nextCh();
                if (ch == '-') {
                    nextCh();
                    return new TokenInfo(DEC, line);
                } else if (ch == '='){
                    nextCh();
                    return new TokenInfo(MINUS_ASSIGN, line); // Add -= operations
                } else {
                    return new TokenInfo(MINUS, line);
                }
            case '=':
                nextCh();
                if (ch == '=') {
                    nextCh();
                    return new TokenInfo(EQUAL, line);
                } else {
                    return new TokenInfo(ASSIGN, line);
                }
            case '>':
                nextCh();
                if (ch == '>'){
                    nextCh();
                    if (ch == '>'){
                        nextCh();
                        if (ch == '='){
                            nextCh();
                            return new TokenInfo(LRSHIFT_ASSIGN, line); // Add >>>= operations
                        }
                        return new TokenInfo(LRSHIFT, line); // Add >>> operations
                    } else if (ch == '='){
                        nextCh();
                        return new TokenInfo(ARSHIFT_ASSIGN, line); // Add >>= operations
                    }
                    return new TokenInfo(ARSHIFT, line); // Add >> operations
                } else if (ch == '='){
                    nextCh();
                    return new TokenInfo(GE, line); // Add >= operations
                }
                return new TokenInfo(GT, line);
            case '<':
                nextCh();
                if (ch == '=') {
                    nextCh();
                    return new TokenInfo(LE, line);
                } else if (ch == '<'){
                    nextCh();
                    if (ch == '='){
                        nextCh();
                        return new TokenInfo(ALSHIFT_ASSIGN, line); // Add <<= operations
                    }
                    return new TokenInfo(ALSHIFT, line); // Add << operations
                } else {
                    return new TokenInfo(LT, line); // Add < operations
                }
            case '!':
                nextCh();
                if (ch == '='){
                    nextCh();
                    return new TokenInfo(NOT_EQUAL, line); // Add != operations
                }
                return new TokenInfo(LNOT, line);
            case '~':
                nextCh();
                return new TokenInfo(NOT, line); // Add ~
            case '|':
                nextCh();
                if (ch == '='){
                    nextCh();
                    return new TokenInfo(OR_ASSIGN, line); // Add |= operations
                } else if (ch == '|') {
                    nextCh();
                    return new TokenInfo(LOR, line); // Add || operations
                }
                return new TokenInfo(OR, line); // Add |
            case '^':
                nextCh();
                if (ch == '='){
                    nextCh();
                    return new TokenInfo(XOR_ASSIGN, line); // Add ^= operations
                }
                return new TokenInfo(XOR, line); // Add ^
            case '&':
                nextCh();
                if (ch == '&') {
                    nextCh();
                    return new TokenInfo(LAND, line);
                } else if (ch == '=') {
                    nextCh();
                    return new TokenInfo(AND_ASSIGN, line); // Add &= operations
                } else {
                    return new TokenInfo(AND, line); // Add &
                }
            case '\'':
                buffer = new StringBuffer();
                buffer.append('\'');
                nextCh();
                if (ch == '\\') {
                    nextCh();
                    buffer.append(escape());
                } else {
                    buffer.append(ch);
                    nextCh();
                }
                if (ch == '\'') {
                    buffer.append('\'');
                    nextCh();
                    return new TokenInfo(CHAR_LITERAL, buffer.toString(), line);
                } else {
                    // Expected a ' ; report error and try to recover.
                    reportScannerError(ch + " found by scanner where closing ' was expected");
                    while (ch != '\'' && ch != ';' && ch != '\n') {
                        nextCh();
                    }
                    return new TokenInfo(CHAR_LITERAL, buffer.toString(), line);
                }
            case '"':
                buffer = new StringBuffer();
                buffer.append("\"");
                nextCh();
                while (ch != '"' && ch != '\n' && ch != EOFCH) {
                    if (ch == '\\') {
                        nextCh();
                        buffer.append(escape());
                    } else {
                        buffer.append(ch);
                        nextCh();
                    }
                }
                if (ch == '\n') {
                    reportScannerError("Unexpected end of line found in string");
                } else if (ch == EOFCH) {
                    reportScannerError("Unexpected end of file found in string");
                } else {
                    // Scan the closing "
                    nextCh();
                    buffer.append("\"");
                }
                return new TokenInfo(STRING_LITERAL, buffer.toString(), line);
            case EOFCH:
                return new TokenInfo(EOF, line);
            // If the next char is any digit, start reading for an INT, DOUBLE or LONG literal
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                buffer = new StringBuffer();
                readDigits(buffer); // Continue reading digits if there are more
                if (ch == 'l' || ch == 'L'){
                    buffer.append(ch); // Append the 'l' or 'L' to the buffer
                    nextCh();
                    return new TokenInfo(LONG_LITERAL, buffer.toString(), line); // Add Long literal functionality
                } else if (ch == 'd' || ch == 'D') {
                    buffer.append(ch);
                    nextCh();
                    return new TokenInfo(DOUBLE_LITERAL, buffer.toString(), line); // Ex: 100d, 67D, etc
                } else if (exponentCheck(buffer)) {
                    // Exponents must be followed by at least one digit
                    if (!isDigit(ch)) {
                        reportScannerError("Invalid syntax; exponent must be followed by at least one digit\n");
                    }
                    readDigits(buffer); // Read/append digits after the exponent
                    if (ch == 'd' || ch == 'D') {
                        buffer.append(ch);
                        nextCh();
                    }
                    // In Java, when dealing with exponents, the result is by default a double
                    // Meaning, the 'd' or 'D' are optional
                    return new TokenInfo(DOUBLE_LITERAL, buffer.toString(), line); // Ex: 50e(+)17(d)
                } else if (ch == '.') {
                    buffer.append(ch); // Append the decimal
                    nextCh();
                    if (isDigit(ch)){
                        readDigits(buffer); // Read/append digits after the decimal
                        if (exponentCheck(buffer)){
                            if (!isDigit(ch)) {
                                reportScannerError("Invalid syntax; exponent must be followed by at least one digit\n");
                            }
                            readDigits(buffer);
                            if (ch == 'd' || ch == 'D') {
                                buffer.append(ch);
                                nextCh();
                            }
                            return new TokenInfo(DOUBLE_LITERAL, buffer.toString(), line); // Ex: 47.59e(+)17(d)
                        }
                        if (ch == 'd' || ch == 'D') {
                            // Optional in a case where we have, say, 45.73
                            buffer.append(ch);
                            nextCh();
                        }
                        return new TokenInfo(DOUBLE_LITERAL, buffer.toString(), line); // Ex: 100.02(d)
                    }
                    if (ch == 'd' || ch == 'D') {
                        buffer.append(ch);
                        nextCh();
                        return new TokenInfo(DOUBLE_LITERAL, buffer.toString(), line); // Ex: 0.d
                    }
                    if (exponentCheck(buffer)){
                        if (!isDigit(ch)) {
                            reportScannerError("Invalid syntax; exponent must be followed by at least one digit\n");
                        }
                        readDigits(buffer);
                        if (ch == 'd' || ch == 'D') {
                            buffer.append(ch);
                            nextCh();
                            return new TokenInfo(DOUBLE_LITERAL, buffer.toString(), line); // Ex: 1.e23d
                        }
                        return new TokenInfo(DOUBLE_LITERAL, buffer.toString(), line); // Ex: 1.e23
                    }
                    return new TokenInfo(DOUBLE_LITERAL, buffer.toString(), line); // Ex: 0.
                }
                return new TokenInfo(INT_LITERAL, buffer.toString(), line);
            default:
                if (isIdentifierStart(ch)) {
                    buffer = new StringBuffer();
                    while (isIdentifierPart(ch)) {
                        buffer.append(ch);
                        nextCh();
                    }
                    String identifier = buffer.toString();
                    if (reserved.containsKey(identifier)) {
                        return new TokenInfo(reserved.get(identifier), line);
                    } else {
                        return new TokenInfo(IDENTIFIER, identifier, line);
                    }
                } else {
                    reportScannerError("Unidentified input token: '%c'", ch);
                    nextCh();
                    return getNextToken();
                }
        }
    }

    /**
     * Returns true if an error has occurred, and false otherwise.
     *
     * @return true if an error has occurred, and false otherwise.
     */
    public boolean errorHasOccurred() {
        return isInError;
    }

    /**
     * Returns the name of the source file.
     *
     * @return the name of the source file.
     */
    public String fileName() {
        return fileName;
    }

    // Scans and returns an escaped character.
    private String escape() {
        switch (ch) {
            case 'b':
                nextCh();
                return "\\b";
            case 't':
                nextCh();
                return "\\t";
            case 'n':
                nextCh();
                return "\\n";
            case 'f':
                nextCh();
                return "\\f";
            case 'r':
                nextCh();
                return "\\r";
            case '"':
                nextCh();
                return "\\\"";
            case '\'':
                nextCh();
                return "\\'";
            case '\\':
                nextCh();
                return "\\\\";
            default:
                reportScannerError("Badly formed escape: \\%c", ch);
                nextCh();
                return "";
        }
    }

    // Advances ch to the next character from input, and updates the line number.
    private void nextCh() {
        line = input.line();
        try {
            ch = input.nextChar();
        } catch (Exception e) {
            reportScannerError("Unable to read characters from input");
        }
    }

    // Reports a lexical error and records the fact that an error has occurred. This fact can be
    // ascertained from the Scanner by sending it an errorHasOccurred message.
    private void reportScannerError(String message, Object... args) {
        isInError = true;
        System.err.printf("%s:%d: error: ", fileName, line);
        System.err.printf(message, args);
        System.err.println();
    }

    // Returns true if the char is an 'e' or 'E'; checks for an optional '+' or '-'
    private boolean exponentCheck(StringBuffer sb) {
        // If the next char is an 'e' or 'E', there is an exponent
        boolean eCheck = false;
        if (ch == 'e' || ch == 'E') {
            eCheck = true;
            sb.append(ch);
            nextCh();
            if (ch == '+' || ch == '-'){
                // Also checks if there is the optional '+' or '-' following the 'e' or 'E'
                sb.append(ch);
                nextCh();
            }
        }
        // Return the result
        return eCheck;
    }

    // Reads all the following digits (mainly for double literal checking)
    private void readDigits(StringBuffer sb){
        // While the next char is a digit, continuously read and append them to the string buffer
        while (isDigit(ch)){
            sb.append(ch);
            nextCh();
        }
    }

    // Returns true if the specified character is a digit (0-9), and false otherwise.
    private boolean isDigit(char c) {
        return (c >= '0' && c <= '9');
    }

    // Returns true if the specified character is a whitespace, and false otherwise.
    private boolean isWhitespace(char c) {
        return (c == ' ' || c == '\t' || c == '\n' || c == '\f');
    }

    // Returns true if the specified character can start an identifier name, and false otherwise.
    private boolean isIdentifierStart(char c) {
        return (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '_' || c == '$');
    }

    // Returns true if the specified character can be part of an identifier name, and false
    // otherwise.
    private boolean isIdentifierPart(char c) {
        return (isIdentifierStart(c) || isDigit(c));
    }
}

/**
 * A buffered character reader, which abstracts out differences between platforms, mapping all new
 * lines to '\n', and also keeps track of line numbers.
 */
class CharReader {
    // Representation of the end of file as a character.
    public final static char EOFCH = (char) -1;

    // The underlying reader records line numbers.
    private LineNumberReader lineNumberReader;

    // Name of the file that is being read.
    private String fileName;

    /**
     * Constructs a CharReader from a file name.
     *
     * @param fileName the name of the input file.
     * @throws FileNotFoundException if the file is not found.
     */
    public CharReader(String fileName) throws FileNotFoundException {
        lineNumberReader = new LineNumberReader(new FileReader(fileName));
        this.fileName = fileName;
    }

    /**
     * Scans and returns the next character.
     *
     * @return the character scanned.
     * @throws IOException if an I/O error occurs.
     */
    public char nextChar() throws IOException {
        return (char) lineNumberReader.read();
    }

    /**
     * Returns the current line number in the source file.
     *
     * @return the current line number in the source file.
     */
    public int line() {
        return lineNumberReader.getLineNumber() + 1; // LineNumberReader counts lines from 0
    }

    /**
     * Returns the file name.
     *
     * @return the file name.
     */
    public String fileName() {
        return fileName;
    }

    /**
     * Closes the file.
     *
     * @throws IOException if an I/O error occurs.
     */
    public void close() throws IOException {
        lineNumberReader.close();
    }
}
