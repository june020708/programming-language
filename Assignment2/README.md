• 수정 코드 설명 
1) Parser.java
```
match(Token.READ); 
Identifier id = new Identifier(match(Token.ID)); 
match(Token.SEMICOLON); 
return new Read(id);readStmt
```
readStmt는 Parser가  READ token을 읽었을 때 호출되는 메소드로 read와 id, 
semicolon으로 문장이 구성된다. 따라서 순서대로 match하되, Identifier를 포함하는 
Read를 반환해야하므로 match된 id로 초기화된 Read 객체를 반환한다. 

    match(Token.PRINT); 
    Expr e = expr(); 
    match(Token.SEMICOLON); 
    return new Print(e); 

이와 비슷하게 PRINT token을 읽었을 때 호출되는 printStmt도 순서대로 print와 
expr, semicolon을 match하되, expr()의 반환값으로 초기화된 Print 객체를 반환한다.  

    match(Token.WHILE); 
    match(Token.LPAREN); 
    Expr e = expr(); 
    match(Token.RPAREN); 
    Stmt s = stmt(); 
    return new While(e, s); 

WHILE 토큰을 읽었을 때 호출되는 whileStmt는 while과 (expr)와 stmt로 구성되어
있다. readStmt와 printStmt와 유사하지만 expr에 괄호가 씌워져 있으므로, expr()을 
호출하기 전후로 LPAREN과 RPAREN을 match해준다. 

다음은 expr()의 논리 연산 부분을 완성하는 과정이다. 

    while (token == Token.AND || token == Token.OR) {            
        Operator op = new Operator(match(token));   
        Expr e2 = bexp(); 
        e = new Binary(op, e, e2);             
    } 

AST로 표현하는게 목적이므로,  저번 과제처럼 논리 연산을 직접 구현할 필요는 
없었다. while로 반복을 표현하되, if문으로 AND, OR 연산을 나눌 필요없이, match된 
Operator와 bexp()의 반환값으로 초기화 된 while문 외부의 e, while문 내부의 e2로 
초기화한 Binary 객체를  return한다. return문은 while문 외부에 작성되어있다. 

    if (token == Token.LT || token == Token.LTEQ || token == Token.GT || token == Token.GTEQ || token == Token.EQUAL || token == Token.NOTEQ){ 
        Operator op = new Operator(match(token)); 
        Expr e2 = aexp(); 
        return new Binary(op, e, e2); 
    } 

bexp() 부분도 expr()과 비슷하게 구현했지만 차이점으로는 연산이 반복적이지 않
고 optional하다는 것이다. 따라서 while문 대신 if문으로 대체했고, bexp() 대신 
aexp()를 호출하여 e와 e2를 초기화했다. 

    if (command != null) command.display(0);    

오류가 발생하지 않도록 주석처리 되어있던 위 문장도 주석 해제했다. 


2) AST.java

트리의 각 노드를 레벨에 맞게 들여쓰기 하는 Indent 클래스의 display 메소드를 
사용하여 각 클래스에 display 메소드를 완성하였다. 

    public void display(int level){ 
        Indent.display(level, "Decls"); 
        for (int i = 0; i < this.size(); i++) { 
            this.get(i).display(level + 1); 
        } 
    } 

Decls는 Decl이 ArrayList로 이루어져 있다. 따라서 Decls를 출력한 뒤  for문과 get 
메소드를 사용해  각 Decl들에 접근하고  level을 증가시켰다. 

    public void display(int level){ 
        Indent.display(level, "Stmts"); 
        for (int i = 0; i < stmts.size(); i++) { 
            stmts.get(i).display(level + 1); 
        } 
    }
  
마찬가지로 Stmts도 출력 이후 각 Stmt들에 접근해 level을 증가시켰다. 
 
    public void display(int level){ 
        Indent.display(level, "Decl"); 
        type.display(level + 1); 
        id.display(level + 1); 
        if(expr != null) expr.display(level + 1); 
    } 
    
Decl은 Type과 Identifier로 이루어져 있으므로, Decl을 출력한 뒤 type과 id의 level
을 증가시켰다. Expr은 null로 초기화되어 있지만  

    Decl (String s, Type t, Expr e) { 
        id = new Identifier(s); type = t; expr = e; 
    } // declaration 
    
이 경우 expr에 e가 대입되므로, if문을 통해 expr이 null이 아닌경우 expr도 level을 
증가시키도록 코드를 작성했다. 
 
    public void display(int level){ 
        Indent.display(level, "Type: " + id); 
    } 
    
Type의 display에선 “Type:  자료형” 꼴로 출력하기 위해서 위와 같이 작성하였다.

    //Identifier 
    public void display(int level){ 
        Indent.display(level, "Identifier: " + id); 
    } 
 
    //Value 
    public void display(int level){ 
        Indent.display(level, "Value: " + value); 
    } 
 
    //Operator 
    public void display(int level){ 
        Indent.display(level, "Operator: " + val); 
    } 
    
또한 Identifier, Value, Operator도 Type의 display와 비슷하게 작성하였다. 
 
    public void display(int level){ 
        Indent.display(level, "Assignment"); 
        id.display(level + 1); 
        expr.display(level + 1); 
    } 
    
Assignment는 Identifier와 Expr로 이루어져 있으므로, Assignment를 출력하고 id와 
expr의 level을 증가시켰다. 
 
    public void display(int level){ 
        Indent.display(level, "If"); 
        expr.display(level + 1); 
        stmt1.display(level + 1); 
        stmt2.display(level + 1); 
    } 
    
If는 Expr와 Stmt 1개 혹은 2개로 이루어져 있다. Stmt가 하나일 경우   

    If (Expr t, Stmt tp) { 
        expr = t; stmt1 = tp; stmt2 = new Empty( ); 
    } 
    
위와 같이 Empty 객체를 새로 생성한다. 따라서 Decl때 처럼 if문으로 stmt2를 전
달받았는지 확인할 필요없이 If를 출력하고 expr, stmt1, stmt2의 level을 증가시켰다. 
 
    public void display(int level){ 
        Indent.display(level, "While"); 
        expr.display(level + 1); 
        stmt.display(level + 1); 
    }
     
While은 Expr과 Stmt로 이루어져 있으므로 While을 출력하고 expr, stmt의 level을 
증가시켰다. 
 
    public void display(int level){ 
        Indent.display(level, "Let"); 
        decls.display(level + 1); 
        //funs.display(level + 1); 
        stmts.display(level + 1); 
    } 
    
Let은 Decls, Functions, Stmts로 구성되어 있지만 이번 과제에선 함수 구현부를 제외
하였다. Let을 출력한 후 decls와 stmts의 level을 증가시켰다. 
 
    public void display(int level){ 
        Indent.display(level, "Read"); 
        id.display(level + 1); 
    } 
    
Read는 Identifier로 이루어져 있으므로, Read를 출력하고 id의 level을 증가시켰다.   
 
    public void display(int level){ 
        Indent.display(level, "Print"); 
        expr.display(level + 1); 
    } 

Print는 Expr로 이루어져 있으므로, Print를 출력하고 expr의 level을 증가시켰다. 

    public void display(int level){ 
        Indent.display(level, "Binary"); 
        op.display(level + 1); 
        expr1.display(level + 1); 
        expr2.display(level + 1); 
    } 

Binary는 Operator와 2개의 Expr로 이루어져 있으므로, Binary를 출력하고 op와 
expr1, expr2의 level을 증가시켰다. 

    public void display(int level){ 
        Indent.display(level, "Unary"); 
        op.display(level + 1); 
        expr.display(level + 1); 
    } 

Unary는 단항연산이므로 expr가 하나이다. 따라서 Unary를 출력하고 op와 expr 하
나만의 level을 증가시킨다. 

 
• 결과 분석 
hi0.s ~ hi7.s의 결과는 모두 정상적으로 출력되었다. 처음 hi0.s를 읽어 실행했을 
때,  Decl 부분에서 Value값인 hello world가 출력되지 않았는데,  

    public void display(int level){ 
        Indent.display(level, "Decl"); 
        type.display(level + 1); 
        id.display(level + 1); 
    } 

와 같이 Decl의 display메소드를 작성했던 것이 원인이었다. Decl 클래스가 여러 
생성자를 가지고 있다는 것을 확인하지 못해 위와 같이 type과 id의 level만 증가시켰다. 
따라서 

    public void display(int level){ 
        Indent.display(level, "Decl"); 
        type.display(level + 1); 
        id.display(level + 1); 
        if(expr != null) expr.display(level + 1); 
    } 

If문을 통해 expr가 null이 아닌지 확인하고 expr의 level도 증가시키는 코드를 추가하였
다.
