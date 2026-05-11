
수정 코드 설명 
1. allocate 와 free
```
else {
    state.push(decl.id, V(decl.expr, state)); 
} 
```
우선 decl.expr가 null이 아닐 때는 state의 상태를 변이시킨다. 

```
if (decl.expr == null) { 
    if (decl.type == Type.INT) state.push(decl.id, new Value(0)); 
    else if (decl.type == Type.BOOL) state.push(decl.id, new Value(false)); 
    else if (decl.type == Type.STRING) state.push(decl.id, new Value("")); 
} 
```
Decl.expr가 null이라면 디폴트 value를 id와 함께 push 해야한다. 단순히 decl.id와 null값을 
한 쌍으로 push하게 되면 추후에 interpreting 결과로 NPE 예외가 발생하지 않게 된다. 따
라서 decl의 타입 케이스를 나눠 그에 알맞는 디폴트 Value를 생성하여 push하였다. Integer 
타입은 실행 예시에 맞게끔 0으로, String은 “”, Bool은 false를 디폴트 값으로 설정했다.  

다음은 수정한 free 메소드이다. 
```
State free (Decls ds, State state) { 
    if (ds != null) { 
        for (Decl decl : ds) state.pop(); 
        return state; 
    } 
    return null; 
} 
```
Decls인 ds에 들어있는 decl 개수 만큼 반복함을 for(Decl decl : ds) 와 같이 표현했다. 처음
엔 state 스택의 중간에 위치한 pair를 어떻게 꺼내야 할까 고민하며 임시 스택을 만들어 
삭제할 pair를 제외한 pair들을 집어넣고, 원본에 복사하는 방법도 생각했으나 필요가 없다
고 생각해 위와 같이 코드를 간단히 작성했다. 가령 let이 중첩되어 있더라도 내부 let이 
마칠 때 push된 pair들은 먼저 제거되므로 free할 대상은 언제나 스택의 top에 위치한다. 
따라서 push된 횟수만큼만 단순히 pop해주면 된다. 
 
2. relational operations과 logical operations
```   
        case "==": 
            if(v1.type() == Type.INT && v2.type() == Type.INT) return new Value(v1.intValue() == v2.intValue()); 
            if(v1.type() == Type.BOOL && v2.type() == Type.BOOL) return new Value(v1.boolValue() == v2.boolValue()); 
            if(v1.type() == Type.STRING && v2.type() == Type.STRING) return new Value(v1.stringValue().equals(v2.stringValue())); 
        case "!=": 
            if(v1.type() == Type.INT && v2.type() == Type.INT) return new Value(v1.intValue() != v2.intValue()); 
            if(v1.type() == Type.BOOL && v2.type() == Type.BOOL) return new Value(v1.boolValue() != v2.boolValue()); 
            if(v1.type() == Type.STRING && v2.type() == Type.STRING) return new Value(!v1.stringValue().equals(v2.stringValue())); 
```
==와 !=는 Value인 v1과 v2의 type에 따라 구분했다. String의 ==, != 연산은 동일 객체를 
평가하기 때문이다. 따라서 Int와 Bool일 때는 단순 ==, != 연산 결과를 반환했지만, String타
입은 equals()를 사용하여 동일한 문자열을 갖는지 판단한 뒤 반환했다.

```
        case "<": 
            if(v1.type() == Type.INT && v2.type() == Type.INT) return new Value(v1.intValue() < v2.intValue()); 
            if(v1.type() == Type.STRING && v2.type() == Type.STRING) return new Value(v1.stringValue().compareTo(v2.stringValue()) < 0); 
        case "<=": 
            if(v1.type() == Type.INT && v2.type() == Type.INT) return new Value(v1.intValue() <= v2.intValue()); 
            if(v1.type() == Type.STRING && v2.type() == Type.STRING) return new Value(v1.stringValue().compareTo(v2.stringValue()) <= 0); 
        case ">": 
            if(v1.type() == Type.INT && v2.type() == Type.INT) return new Value(v1.intValue() > v2.intValue()); 
            if(v1.type() == Type.STRING && v2.type() == Type.STRING) return new Value(v1.stringValue().compareTo(v2.stringValue()) > 0); 
        case ">=": 
            if(v1.type() == Type.INT && v2.type() == Type.INT) return new Value(v1.intValue() >= v2.intValue()); 
            if(v1.type() == Type.STRING && v2.type() == Type.STRING) return new Value(v1.stringValue().compareTo(v2.stringValue()) >= 0); 
```
다른 연산자들도 type별로 구분했는데, String type의 대소연산은 사전 순으로 비교해야하기 
때문이다. 따라서 Int일 때는 비교 결과를 반환했지만, String일 때는 compareTo()를 사용하
고 0과 결과값을 비교하여 반환하였다. Bool은 대소를 판별할 수 없으므로 분류하지 않았
다. 
```
        case "&": 
            return new Value(v1.boolValue() && v2.boolValue()); 
        case "|": 
            return new Value(v1.boolValue() || v2.boolValue()); 
```
논리 연산자는 두 Value의 and연산과 or연산을 수행한 값을 반환했다. 
 
3. do-while과 for
```
        case DO: 
            s = doStmt(); return s; 
        case FOR: 
            s = forStmt(); return s;
```
우선 stmt() 내부 swtich(token)에 DO와 FOR case를 추가했다. 

```
private Stmts doStmt() { 
    Stmts ss = new Stmts(); 
    match(Token.DO); 
    Stmt s = stmt(); 
    ss.stmts.add(s); 
    match(Token.WHILE); 
    match(Token.LPAREN); 
    Expr e = expr(); 
    match(Token.RPAREN); 
    match(Token.SEMICOLON); 
    ss.stmts.add(new While(e, s)); 
    return ss; 
} 
```
추가한 doStmt 메소드이다. dowhileStmt는 do <stmt> while (<expr>); 이므로 이에 맞게 
match하였다. stmt가 한번 수행되고 while문이 실행되므로, ss.stmts에 stmt를 add한 뒤 new 
While(e, s)를 add하였다. 

```
private Let forStmt() { 
    match(Token.FOR);
    match(Token.LPAREN);
    Type t = type(); 
    String id1 = match(Token.ID); 
    match(Token.ASSIGN); 
    Expr e1 = expr(); 
    match(Token.SEMICOLON); 
    Expr e2 = expr(); 
    match(Token.SEMICOLON); 
    String id2 = match(Token.ID); 
    match(Token.ASSIGN); 
    Expr e3 = expr(); 
    match(Token.RPAREN); 
    Stmt s = stmt(); 
    Decls ds = new Decls(new Decl(id1, t, e1)); 
    Stmts ss = new Stmts(s); 
    ss.stmts.add(new Assignment(new Identifier(id2), e3)); 
    return new Let(ds, new Stmts(new While(e2, ss))); 
}
```

추가한 forStmt 메소드이다. forStmt는 for (<type> id = <expr>; <expr>; id = <expr>) <stmt> 로 
구성되어 있는데, 괄호 안에 3가지 식은 각각 초기화식, 조건식, 증감식이다. 초기화식은 
decl로, 조건식은 while의 조건식으로, 증감식은 stmt가 끝난 뒤 이루어져야 한다. 따라서 
new Decl(id1, t, e1)로 구성된 Decls를 만들고, stmt와 증감식을 Stmts로 묶고,  new Let(ds, 
new Stmts(new While(e2, ss)))를 반환함으로써 for문을 while을 포함한 let문으로 표현했다. 
이 메소드 완성이 가장 복잡했는데, 특히 증감식을 Stmts에 추가하는 아이디어를 생각하는 
데 오래걸렸다. 

결과 분석

테스트 결과는 모두 실행 예시와 같게 나왔다. 
처음에 allocate의 디폴트값을 생각없이 null로 정하고 코드를 실행했었는데 NPE가 발생해 type별로 if문을 사용하여 디폴트값을 정해줬다. 
또한 dowhileStmt와 forStmt를 구현하는 것이 복잡했는데, doStmt에선 stmt와 whileStmt를 Stmts로, forStmt에선 stmt와 증감식을 Stmts로 묶는 방법을 깨달아야만 구현할 수 있었다. 
