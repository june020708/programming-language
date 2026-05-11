• 수정 코드 설명 
1. Parser.java 
```
private Decls params() { 
    Decls params = new Decls(); 
    Type t = type(); 
    String id = match(Token.ID); 
    params.add(new Decl(id, t)); 
    while (token == Token.COMMA) { 
        match(Token.COMMA); 
        t = type(); 
        id = match(Token.ID); 
        params.add(new Decl(id, t)); 
    } 
    return params; 
}
```
function 메소드에서 token이 Token.RPAREN이 아닐 때, 즉 fun <type> id(<params>) <stmt>에서 괄호가 닫히지 않았을 때, params 메소드를 호출함으로써 매개변수를 결정한다. parameter는 <type> id 꼴이므로 type형 변수 t에 타입을 저장하고 Token.ID를 match해 id 변수에 저장한 뒤 params.add(new Decl(id, t));를 통해 param을 추가한다. 만약 while문 조건처럼 token이 Token.COMMA를 읽었다면 매개변수가 여려 개라는 뜻이므로 token이 ,를 더이상 읽지 않을 때 까지Token.COMMA를 match하고 위 과정을 반복한다. 저장된 매개변
수인 params를 반환함으로써 params 메소드가 완성된다. 

```
private Let letStmt () { 
    match(Token.LET);    
    Decls ds = decls(); 
    Functions fs = null; 
    if(token == Token.FUN) { 
        fs = functions(); 
    } 
         
    match(Token.IN); 
    Stmts ss = stmts(); 
    match(Token.END);    
    match(Token.SEMICOLON); 
    return new Let(ds, fs, ss); 
}
```
hi13.s에서 처럼 Let문에서도 함수를 정의할 수 있다. 따라서 token이 Token.FUN을 읽었다면 functions 메소드를 호출하여 fs변수에 저장하고 return new Let(df, fs, ss); 에 사용함으로써 반환한다. 

```
private Call call(Identifier id) { 
    match(Token.LPAREN); 
    Call c = new Call(id, arguments()); 
    match(Token.RPAREN); 
    match(Token.SEMICOLON); 
    return c; 
}
``` 
assignment 메소드에서 id 다음에 괄호가 나오면 function이므로, call(id)를 반환하게된다. 이때 호출되는 call 메소드는 id에 해당하는 Exprs를 반환해야한다. 따라서 Token.LPAREN, Token.RPAREN, Token.SEMICOLON을 각각 순서에 맞게 match하고, Token.RPAREN match 전에 Call(id, arguments()) 객체를 새로 생성해 반환하게된다. 이때 arguments 메소드 내부에서 token이 Token.RPAREN인지 검사하고 Token.COMMA가 있다면 es.add(expr());을 통해 expr를 추가하므로, call 메소드에서 expr를 여러 개인지 확인하지 않아도 된다.  
 
2. Sint.java 
Eval(Call c, State state)를 완성하기 전에 newFrame()과 deleteFrame을 먼저 설명하겠다.
```
State newFrame (State state, Call c, Function f) { 
    if (c.args.size() == 0) return state; 
    Value val[] = new Value[c.args.size()]; 
    int i=0; 
    for (Expr e : c.args) val[i++] = V(e,state); 
    i=0;  
    for (Decl d : f.params) { 
       Identifier v = (Identifier)d.id; 
       state.push(v, val[i++]); 
    } 
    state.push(new Identifier("return"), null); 
    return state; 
}
```
NewFrame 메소드에서는 스택 프레임 변수 val[]을 만들고 arguments의 값을 V(e, state)를 통해 계산하여 저장한다. 이후 state에 매개변수들을 push하고 return 값을 마지막으로 push한다.  

```
State deleteFrame (State state, Call c, Function f) { 
    state.pop();  
    if (f.params != null) state  = free(f.params, state); 
    return state;             
}
```
DeleteFrame 메소드에선 반대로 스택 프레임 상단에 있는 return 값을 state.pop()을 통해 제거하고,스택 프레임에 들어있는 매개변수들을 free(f.params, state)를 통해 제거한다. 이후 변경된 state를 반환한다. 

```
State Eval(Call c, State state) { 
    Value v = state.get(c.fid); 
    Function f = v.funValue(); 
    state = newFrame(state, c, f);  
    state = Eval(f.stmt, state); 
    state = deleteFrame(state, c, f); 
    return state; 
} 
```
state.get(c.fid)와 그 값이 저장된 Value 변수인 v의 v.funValue() 를 통해 찾아낸 function으로  스택프레임을 만드는 메소드인 newFrame()을 호출하고, f.stmt가 어떤 stmt임에 따라 오버로딩된 Eval들을 호출하는 Eval(f.stmt, state)을 호출한다. 이후 스택프레임을 삭제하는 deleteFrame()을 호출하고 state를 반환한다. return 값을 반환하는 하단의 V(Call c, State state) 메소드와는 다르게 return 값을 state.peek().val로 호출하지 않는다. 

```
State Eval(Let l, State state) { 
    State s = allocate(l.decls, state);         
    if (l.funs != null) { 
        for (Function fun : l.funs) 
            s.push(fun.id, new Value(fun)); 
    }    
    s = Eval(l.stmts, s); 
    return free(l.decls, s); 
}
``` 
Let문에서 functino이 정의될 수 있으므로 if(l.funs != null)을 통해 l에 funs가 존재하는지 확인하고, 존재한다면 state에  function을 s.push(fun.id, new Value(fun))을 통해 push한다. 

• 결과 분석 
Sint.java를 읽어보다가 TODO 주석이 달린 Eval(Call c, State state) 메소드와 V(Call c, State state) 메소드가 반환 타입이 다름에도 불구하고 차이가 v = state.peek().val; 이 있는가 뿐인 게 궁금하여 호출부를 찾아보았다. Eval(Stmt s, State state) 메소드에서 s instanceof Call일때는 return Eval((Call)s, state)를, s instanceof Return 일 때는 return Eval((Return)s, state)를 호출하여 반환값을 필요로하는 호출인지 아닌지에 따라 다른 Eval 메소드를 사용함을 알 수 있었다. Eval(Return r, State state) 메소드에서 V 메소드를 호출하여 얻은 스택 프레임 상단의 return 값을 얻을 수 있다. 사실 이번 과제는 솔루션코드
가 제공되어 분석만 하면 됐었지만, 만약 구현을 해야됐다면 이 부분을 이해하는데 시간이 많이 걸렸을 것 같다. 
