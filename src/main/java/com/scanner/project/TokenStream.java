package com.scanner.project;

// TokenStream.java
// Scanner for KAY language

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class TokenStream {

    // Instance variables
    private boolean isEof = false;
    private char nextChar = ' ';
    private BufferedReader input;

    // Added for compatibility with demo file
    public boolean isEoFile() {
        return isEof;
    }

    // Constructor
    public TokenStream(String fileName) {
        try {
            input = new BufferedReader(new FileReader(fileName));
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + fileName);
            isEof = true;
        }
    }

    public Token nextToken() {

        while (true) {

            Token t = new Token();
            t.setType("Other");
            t.setValue("");

            // Skip whitespace
            skipWhiteSpace();

            // EOF check
            if (isEof) {
                t.setType("EOF");
                t.setValue("EOF");
                return t;
            }

            // Handle comments: //
            if (nextChar == '/') {
                char lookAhead = readChar();
                if (lookAhead == '/') {
                    // Skip rest of line
                    while (!isEof && !isEndOfLine(nextChar)) {
                        nextChar = readChar();
                    }
                    continue; // restart scan after comment
                } else {
                    // Single '/' operator
                    t.setType("Operator");
                    t.setValue("/");
                    nextChar = lookAhead;
                    return t;
                }
            }

            // Handle assignment operator :=
            if (nextChar == ':') {
                t.setType("Operator");
                t.setValue(":");
                nextChar = readChar();
                if (nextChar == '=') {   // check :=
                    t.setValue(":=");
                    nextChar = readChar();
                }
                return t;
            }

            // Operators (+, -, *, <, >, ==, !=, &&, ||, etc.)
            if (isOperator(nextChar)) {
                t.setType("Operator");
                t.setValue(t.getValue() + nextChar);

                switch (nextChar) {

                case '<':
                case '>':
                case '=':
                case '!':
                    nextChar = readChar();
                    if (nextChar == '=') {
                        t.setValue(t.getValue() + nextChar);
                        nextChar = readChar();
                    }
                    return t;

                case '&':
                    nextChar = readChar();
                    if (nextChar == '&') {
                        t.setValue(t.getValue() + nextChar);
                        nextChar = readChar();
                        return t;
                    }
                    t.setType("Other");
                    return t;

                case '|':
                    nextChar = readChar();
                    if (nextChar == '|') {
                        t.setValue(t.getValue() + nextChar);
                        nextChar = readChar();
                        return t;
                    }
                    t.setType("Other");
                    return t;

                default: // + - * %
                    nextChar = readChar();
                    return t;
                }
            }

            // Separators: parentheses, braces, semicolon, comma
            if (isSeparator(nextChar)) {
                t.setType("Separator");
                t.setValue(t.getValue() + nextChar);
                nextChar = readChar();
                return t;
            }

            // Identifiers, keywords, True/False literals
            if (isLetter(nextChar)) {
                t.setType("Identifier");
                while (isLetter(nextChar) || isDigit(nextChar)) {
                    t.setValue(t.getValue() + nextChar);
                    nextChar = readChar();
                }

                // Keywords for KAY language
                if (isKeyword(t.getValue())) {
                    t.setType("Keyword");
                }
                // Boolean literals
                else if (t.getValue().equals("True") || t.getValue().equals("False")) {
                    t.setType("Literal");
                }

                return t;
            }

            // Integer literals
            if (isDigit(nextChar)) {
                t.setType("Literal");
                while (isDigit(nextChar)) {
                    t.setValue(t.getValue() + nextChar);
                    nextChar = readChar();
                }
                return t;
            }

            // Unknown token
            t.setType("Other");
            t.setValue(t.getValue() + nextChar);
            nextChar = readChar();
            while (!isEndOfToken(nextChar)) {
                t.setValue(t.getValue() + nextChar);
                nextChar = readChar();
            }
            return t;
        }
    }

    private char readChar() {
        int i = 0;
        if (isEof)
            return (char) 0;
        try {
            i = input.read();
        } catch (IOException e) {
            System.exit(-1);
        }
        if (i == -1) {
            isEof = true;
            return (char) 0;
        }
        return (char) i;
    }

    private boolean isKeyword(String s) {
        // EXACT keywords used by your parser / grammar
        return s.equals("if") || s.equals("else") || s.equals("while")
                || s.equals("integer") || s.equals("bool") || s.equals("main");
    }

    private boolean isSeparator(char c) {
        return c == '(' || c == ')' || c == '{' || c == '}' || c == ';' || c == ',';
    }

    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '%'
                || c == '=' || c == '<' || c == '>' || c == '!' || c == '&' || c == '|';
    }

    private boolean isLetter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private boolean isDigit(char c) {
        return (c >= '0' && c <= '9');
    }

    private boolean isWhiteSpace(char c) {
        return c == ' ' || c == '\t' || c == '\r' || c == '\n' || c == '\f';
    }

    private boolean isEndOfLine(char c) {
        return c == '\r' || c == '\n' || c == '\f';
    }

    private boolean isEndOfToken(char c) {
        return isWhiteSpace(c) || isOperator(c) || isSeparator(c) || isEof;
    }

    private void skipWhiteSpace() {
        while (!isEof && isWhiteSpace(nextChar)) {
            nextChar = readChar();
        }
    }

    public boolean isEndofFile() {
        return isEof;
    }
}




