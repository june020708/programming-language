import java.io.*;

class Calc {
	int token; int value; int ch;
	private PushbackInputStream input;
	final int NUMBER = 256;

	Calc(PushbackInputStream is) {
		input = is;
	}

	int getToken() { /* tokens are characters */
		while (true) {
			try {
				ch = input.read();
				if (ch == ' ' || ch == '\t' || ch == '\r')
					;
				else if (Character.isDigit(ch)) {
					value = number();
					input.unread(ch);
					return NUMBER;
				} else
					return ch;
			} catch (IOException e) {
				System.err.println(e);
			}
		}
	}

	private int number() {
		/* number -> digit { digit } */
		int result = ch - '0';
		try {
			ch = input.read();
			while (Character.isDigit(ch)) {
				result = 10 * result + ch - '0';
				ch = input.read();
			}
		} catch (IOException e) {
			System.err.println(e);
		}
		return result;
	}

	void error() {
		System.out.printf("parse error : %d\n", ch);
		// System.exit(1);
	}

	void match(int c) {
		if (token == c)
			token = getToken();
		else
			error();
	}

	void command() {
		/* command -> expr '\n' */
		Object result = expr();
		if (token == '\n') /* end the parse and print the result */
			System.out.println("The result is: " + result);
		else
			error();
	}

	Object expr() {
		/* <expr> -> <bexp> {& <bexp> | '|'<bexp>} | !<expr> | true | false */
		Object result;
		if (token == '!') {
			// !<expr>
			match('!');
			result = !(boolean) expr();
		} else if (token == 't') {
			// true
			match('t');
			result = (boolean) true;
		} else if (token == 'f') {
			// false
			match('f'); // Implemented here
			result = (boolean) false;
		} else {
			/* <bexp> {& <bexp> | '|'<bexp>} */
			result = bexp();
			while (token == '&' || token == '|') {
				// Implemented here
				if (token == '&') {
					match('&');
                    if (result instanceof Boolean){
						boolean bexp2 = (boolean)bexp();
						result = (boolean) result && bexp2;
					}
				} else if (token == '|') {
					match('|');
                    if (result instanceof Boolean){
						boolean bexp2 = (boolean)bexp();
						result = (boolean) result || bexp2;
					}
				}
			}
		}
		return result;
	}

	Object bexp() {
        /* <bexp> -> <aexp> [<relop> <aexp>] */
		Object result;
		int aexp1 = aexp();
		if (token == '<' || token == '>' || token == '=' || token == '!') { // <relop>
			/* Check each string using relop(): "<", "<=", ">", ">=", "==", "!=" */
			// Implemented here
			switch (relop()) {
			case "<=" -> result = aexp1 <= aexp() ? (boolean) true : false;
			case "<" -> result = aexp1 < aexp() ? (boolean) true : false;
			case ">=" -> result = aexp1 >= aexp() ? (boolean) true : false;
			case ">" -> result = aexp1 > aexp() ? (boolean) true : false;
			case "==" -> result = aexp1 == aexp() ? (boolean) true : false;
			case "!=" -> result = aexp1 != aexp() ? (boolean) true : false;
			default -> {
				error();
				return null;	
			} 
			}
		} else {
			result = aexp1;
		}
		return result;
	}

	String relop() {
		/* <relop> -> ( < | <= | > | >= | == | != ) */
		String result = "";
		// Implemented here
		if (token == '<') {
			match('<');
			if (token == '=') {
				match('=');
				result = "<=";
			} else
				result = "<";
		} else if (token == '>') {
			match('>');
			if (token == '=') {
				match('=');
				result = ">=";
			} else
				result = ">";
		} else if (token == '=') {
			match('=');
			if (token == '=') {
				match('=');
				result = "==";
			}
		} else if (token == '!') {
			match('!');
			if (token == '=') {
				match('=');
				result = "!=";
			}
		}
        return result;
	}

	int aexp() {
		/* aexp -> term { '+' term | '-' term} */
		// Implemented here
		int result = term();
		while (token == '+' || token == '-') {
			if (token == '+') {
				match('+');
				result += term();
			} else if (token == '-') {
				match('-');
				result -= term();
			}
		}
		return result;
	}

	int term() {
		/* term -> factor { '*' factor | '/' factor} */
		// Implemented here
		int result = factor();
		while (token == '*' || token == '/') {
			if (token == '*') {
				match('*');
				result *= factor();
			} else if (token == '/') {
				match('/');
				int factor1 = factor();
				try{
					result /= factor1;
				} catch(ArithmeticException e){
					System.out.println("Can't devide with 0");
					return 0;
				}
			}
		}
		return result;
	}

	int factor() {
		/* factor -> ['-']('(' aexp ')' | number) */
		// Implemented here (Added ['-'])
		int result = 0;
		if (token == '-') {
			match('-');
			if (token == '(') {
				match('(');
				result = (-1) * aexp();
				match(')');
			} else if (token == NUMBER) {
				result = (-1) * value;
				match(NUMBER);
			}
		} else if (token == '(' || token == NUMBER) {
			if (token == '(') {
				match('(');
				result = aexp();
				match(')');
			} else if (token == NUMBER) {
				result = value;
				match(NUMBER); // token = getToken();
			}
		}
		return result;
	}

	void parse() {
		token = getToken(); // get the first token
		command(); // call the parsing command
	}

	public static void main(String args[]) {
		Calc calc = new Calc(new PushbackInputStream(System.in));
		while (true) {
			System.out.print(">> ");
			calc.parse();
		}
	}
}