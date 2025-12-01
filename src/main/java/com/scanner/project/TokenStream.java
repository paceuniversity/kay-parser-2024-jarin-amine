package com.scanner.project;
// TokenStream.java

// Implementation of the Scanner for KAY

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class TokenStream {

	// Instance variables
	private boolean isEof = false; // is end of file
	private char nextChar = ' '; // next character in input stream
	private BufferedReader input;

	// This function was added to make the demo file work
	public boolean isEoFile() {
		return isEof;
	}

	// Constructor
	// Pass a filename for the program text as a source for the TokenStream.
	public TokenStream(String fileName) {
		try {
			input = new BufferedReader(new FileReader(fileName));
		} catch (FileNotFoundException e) {
			System.out.println("File not found: " + fileName);
			// System.exit(1); // Removed to allow ScannerDemo to continue
			// running after the input file is not found.
			isEof = true;
		}
	}

	public Token nextToken() { // Main function of the scanner
								// Return next token with its type and value.
		Token t = new Token();
		t.setType("Other");
		t.setValue("");

		// First check for whitespaces and bypass them
		skipWhiteSpace();

		// Check for end of file after skipping whitespace
		if (isEof) {
			return t; // Returns type "Other" with empty value for EOF
		}

		// Then check for a comment, and bypass it
		// but remember that / may also be a division operator.
		while (nextChar == '/') {
			char lookAhead = readChar(); // Read the character after the first '/'
			if (lookAhead == '/') { // If / is followed by another /
				// skip rest of line - it's a comment.
				while (!isEof && !isEndOfLine(nextChar)) {
					nextChar = readChar();
				}
				// After skipping the line, skip any remaining whitespace
				skipWhiteSpace();
				// Check for end of file after skipping comment and whitespace
				if (isEof) {
					return t;
				}
			} else {
				// A slash followed by anything else must be an operator.
				nextChar = lookAhead;
				t.setValue("/");
				t.setType("Operator");
				return t;
			}
		}

		// Then check for an operator; this part of the code should recover 2-character
		// operators as well as 1-character ones.
		if (isOperator(nextChar)) {
			t.setType("Operator");
			t.setValue(t.getValue() + nextChar);
			switch (nextChar) {
			case '<': // <= or <
			case '>': // >= or >
			case '=': // == or =
			case '!': // != (KAY likely uses this for not equal)
				nextChar = readChar();
				if (nextChar == '=') {
					t.setValue(t.getValue() + nextChar);
					nextChar = readChar();
				}
				return t;
			case '|':
				// Look for || (Logical OR)
				nextChar = readChar();
				if (nextChar == '|') {
					t.setValue(t.getValue() + nextChar);
					nextChar = readChar();
					return t;
				} else {
					// Single '|' is not valid in KAY; return the single '|' as "Other"
					t.setType("Other");
					return t; 
				}
			case '&':
				// Look for && (Logical AND)
				nextChar = readChar();
				if (nextChar == '&') {
					t.setValue(t.getValue() + nextChar);
					nextChar = readChar();
					return t;
				} else {
					// Single '&' is not valid in KAY; return the single '&' as "Other"
					t.setType("Other");
					return t;
				}
			default: // all other operators: +, -, *, %
				nextChar = readChar();
				return t; // Ensures a return for single-character operators
			}
		}

		// Then check for a separator
		if (isSeparator(nextChar)) {
			t.setType("Separator");
			t.setValue(t.getValue() + nextChar);
			nextChar = readChar();
			return t;
		}

		// Then check for an identifier, keyword, or literal (True or False).
		if (isLetter(nextChar)) {
			// Set to an identifier
			t.setType("Identifier");
			while ((isLetter(nextChar) || isDigit(nextChar))) {
				t.setValue(t.getValue() + nextChar);
				nextChar = readChar();
			}
			// Now see if this is a keyword
			if (isKeyword(t.getValue())) {
				t.setType("Keyword");
			} else if (t.getValue().equals("True") || t.getValue().equals("False")) {
				t.setType("Literal"); // Boolean Literals
			}
			if (isEndOfToken(nextChar)) { // If token is valid, returns.
				return t;
			}
		}

		if (isDigit(nextChar)) { // check for integer literals
			t.setType("Literal");
			while (isDigit(nextChar)) {
				t.setValue(t.getValue() + nextChar);
				nextChar = readChar();
			}
			// An Integer-Literal is to be only followed by a space,
			// an operator, or a separator.
			if (isEndOfToken(nextChar)) {// If token is valid, returns.
				return t;
			}
		}

		// Final check for unknown/other tokens
		t.setType("Other");
		
		if (isEof) {
			return t;
		}

		// Makes sure that the whole unknown token (Type: Other) is printed.
		// If the parser reached this point, it means the current character is unknown
		while (!isEndOfToken(nextChar)) {
			t.setValue(t.getValue() + nextChar);
			nextChar = readChar();
		}

		// Skip whitespace one final time before returning the unknown token
		skipWhiteSpace(); 

		return t;
	}

	private char readChar() {
		int i = 0;
		if (isEof)
			return (char) 0;
		System.out.flush();
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
		// Keywords based on common language structure
		return s.equals("if") || s.equals("else") || s.equals("while") || s.equals("int")
			|| s.equals("float") || s.equals("print") || s.equals("return") || s.equals("void");
	}

	private boolean isSeparator(char c) {
		// Separators: parentheses, braces, semicolon, comma
		return c == '(' || c == ')' || c == '{' || c == '}' || c == ';' || c == ',';
	}

	private boolean isOperator(char c) {
		// Checks for characters that start operators
		return c == '+' || c == '-' || c == '*' || c == '/' || c == '%'
			|| c == '=' || c == '<' || c == '>' || c == '!' || c == '&' || c == '|';
	}

	private boolean isLetter(char c) {
		return (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z');
	}

	private boolean isDigit(char c) {
		return (c >= '0' && c <= '9');
	}

	private boolean isWhiteSpace(char c) {
		return (c == ' ' || c == '\t' || c == '\r' || c == '\n' || c == '\f');
	}

	private boolean isEndOfLine(char c) {
		return (c == '\r' || c == '\n' || c == '\f');
	}

	private boolean isEndOfToken(char c) { // Is the value a seperate token?
		return (isWhiteSpace(nextChar) || isOperator(nextChar) || isSeparator(nextChar) || isEof);
	}

	private void skipWhiteSpace() {
		// check for whitespaces, and bypass them
		while (!isEof && isWhiteSpace(nextChar)) {
			nextChar = readChar();
		}
	}

	public boolean isEndofFile() {
		return isEof;
	}
}
