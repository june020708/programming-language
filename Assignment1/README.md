# programming-language
Study log for Programming Language course.

수정 코드 설명 
1) Subtraction (-) and division (/) 

    int aexp() { 

        /* aexp -> term { '+' term | '-' term} */ 

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

aexp는 term에 ‘+’ term 혹은 ‘-’ term이 0번 이상 반복되므로, while을 사용해 반복을 표현했다. 기존의 while 문인 

      while (token == '+') { 

            match('+'); 

            result += term(); 

        } 

에서 조건식에 ‘-’ token을 포함하게 수정했고, if-else if 문을 통해 ‘-’ token을 마주했을 때 뒤에 있는 term을 뺀 값을 반환하도록 코드를 완성했다.  

int term() { 

        /* term -> factor { '*' factor | '/' factor} */ 

        // Implemented this code 

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

term 또한 aexp와 마찬가지로 factor에 ‘*’ factor 혹은 ‘/’ factor가 0번 이상 반복되므로, while을 사용해 반복을 표현했고, 기존의 while 문인 

while (token == '*') { 

           match('*'); 

           result *= factor(); 

       } 

에서 조건식에 ‘/’ token을 포함하게 수정했고, if-else if 문을 통해 ‘/’ token을 마주했을 때 뒤에 있는 factor를 나눈 값을 반환하도록 코드를 완성했다. 단, 반환 타입이 정수형이므로 0으로 나눴을 때의 예외 처리를 해야하므로,  try-catch문을 통해 ArithmeticException을 예외 처리하고 0으로 나눌 수 없다는 문장을 출력하게 했다. return 값은 0으로 설정했지만, 무한대를 뜻하는 표현으로 변경할 수 있을 것이다. 

 

 

2) Comparison operators (< , <=, >, >=, ==, !=) 

String relop() { 

        /* <relop> -> ( < | <= | > | >= | == | != ) */ 

        String result = ""; 

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

 

relop()에 추가한 코드이다. relop은 bexp()에서 aexp 다음에 ‘<’, ‘>’, ‘!’, ‘=’ token을 마주했을 때 호출된다.  relop() 내에서 관계 연산자의 종류가 무엇인지 조건문과 match를 통해 확인한다. 

이후 String 형식으로 반환된 관계 연산자는  

    Object bexp() { 

        /* <bexp> -> <aexp> [<relop> <aexp>] */ 

        Object result; 

        int aexp1 = aexp(); 

        if (token == '<' || token == '>' || token == '=' || token == '!') { // <relop> 

            /* Check each string using relop(): "<", "<=", ">", ">=", "==", "!=" */ 

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

 

switch문에 들어가게 되고, 각 case에 따라 result에 true 혹은 false를 할당한다. default 문에서 relop()의 반환 값이 이상할 경우 error()를 호출하고 null을 반환하게 했다. 반환 값은 유동적으로 변경할 수 있을 것이다. 

 

 

 	3) Boolean expression(false), Logical operators (&, |, !) 

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

            match('f'); 

            result = (boolean) false; 

        } else { 

            /* <bexp> {& <bexp> | '|'<bexp>} */ 

            result = bexp(); 

            while (token == '&' || token == '|') { 

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

false 조건부는 true와 비슷하게 작성했다. 

expr는 bexp 와 0번 이상의 ‘&’ bexp 혹은 ‘|’ bexp로 이루어져 있으므로, while 문을 통해 반복이 표현되어 있다. token이 ‘&’나 ‘|’를 만나지 않았을 때, bexp를 반환하기 위해 반복문 이전에 result에 bexp()가 할당되어 있다. 

반복문 내에서 expr()의 logical operators 중 ‘&’ 연산과 ‘|’ 연산을 구현하기 위해서는 피연산부가 둘 다 Boolean 형식이어야 한다. 논리 연산을 위해서 result 변수에 할당된 bexp()가 true 혹은 false여야 하므로 if (result instanceof Boolean)을 통해 result가 Boolean 형식인지 검사하고, Boolean 형식이 맞다면 bexp()을 한 번 더 호출하여 논리 연산을 진행한 수 result에 Boolean 형식으로 할당하여 반환한다. 이 부분을 수정하는 데 가장 어려웠던 것 같다. 

 

 

4) Add [‘-’] operation in factor() 

int factor() { 

        /* factor -> ['-']('(' aexp ')' | number) */ 

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

우선, 동영상이나 Lab01.pdf에서 aexp가 expr로 오기되어 있어 pdf에 적혀있는 EBNF 문법에 따라 factor를 ‘(’ aexp ‘)’ 이거나 number로 구현했다.  

token이 ‘-’가 됐을 경우, 뒤에 괄호로 둘러싸인 aexp가 나오는지 number가 나오는지 조건문과 match로 검사하고, 정수형 타입인 aexp() 혹은 value에 -1을 곱하는 연산을 수행해 할당한 result를 반환하도록 하였다. 만약 token이 ‘-’가 아니라면 aexp() 혹은 value를 할당한 result를 그대로 반환한다. 

만약 aexp가 아니라 factor -> [‘-’] (‘(’ expr ‘)’ | number)가 맞다면 expr()가 int 형인지 instanceof를 통해 검사한 뒤 음수를 취하고, 나머지는 예외 처리하는 코드를 작성해야 할 것이다. 

 

결과 스크린샷 

 

 

 

결과 분석 

 

15문항 모두 결과가 예시와 같게 나왔다. 처음에 3>100&2==2 부분에서 parse error가 발생했는데(error code : 61), 

    void error() { 

        System.out.printf("parse error : %d\n", ch); 

        // System.exit(1); 

    } 

이는 문자 ‘=’의 파싱이 실패했다는 것을 의미한다. 각 메소드의 연산 과정 및 반환 이전에 breakpoints를 설정하고 디버깅해 본 결과 token이 ‘&’일 때 잘못된 연산 이후 command()로 result가 들어갔고, 다음 token이 ‘\n’이 아닌 ‘=’이었기 때문에 else 문의 error()를 호출하게 되었다. 즉 논리 연산 부분의 코드가 잘못 작성되어 있었다. 

따라서 if 문과 instanceof를 통해 논리 연산 좌항이 Boolean 타입이 오도록 하였고, 캐스팅을 통해 최종 result 값에 Boolean 형식의 값이 할당되게 하여 오류를 수정했다. 
