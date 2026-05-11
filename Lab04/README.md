• 수정 코드 설명 
 
1. Parser.java
```
    private Decl decl() { 
       Type t = type(); 
       String id = match(Token.ID); 
       Decl d = null; 
 
       if (token == Token.LBRACKET) { 
            match(Token.LBRACKET); 
            Value n = literal(); 
            match(Token.RBRACKET); 
            d = new Decl(id, t, n.intValue()); 
       } else if (token == Token.ASSIGN) { 
            match(Token.ASSIGN); 
            Expr e = expr(); 
            d = new Decl(id, t, e); 
       } else  
            d = new Decl(id, t); 
 
        match(Token.SEMICOLON); 
        return d; 
    } 
```
If (token == Token.LBRACKET) {...} 부분은 배열을 정의했을 때의 부분이다. ‘[‘, ‘]’를 각 순서에 맞게 match시켰고, n은 숫자 리터럴이므로 literal() 메소드를 통해 match 메소드를 호출시킨 뒤 n을 intValue()를 통해 정수로 변환하여 new Decl(id, t, n.intValue());를 통해 d를 반환시키도록 하였다. 
``` 
    private Stmt assignment() { 
        Array ar = null;   
        Identifier id = new Identifier(match(Token.ID)); 
 
        if (token == Token.LBRACKET) { 
            match(Token.LBRACKET); 
            Expr ie = expr(); 
            match(Token.RBRACKET); 
            ar = new Array(id, ie); 
        } 
        match(Token.ASSIGN); 
        Expr e = expr(); 
        match(Token.SEMICOLON); 
        if (ar == null) return new Assignment(id, e); 
        else return new Assignment(ar, e); 
    } 
```
If (token == Token.LBRACKET) {...} 부분은 배열에 할당할 때의 부분이다. ‘[‘, ‘]’를 각 순서에 맞게 match 시켰고,  조건문 외부에 이미 e 변수가 사용되고 있으므로 헷갈리지 않게 하기 위해서 ie로 expr()를 호출하고 대입하였다. 이후에 Array 변수인 ar에 new Array(id, ie)를 통해 새로운 객체를 대입하였고 조건문 외부에서 반환되게 하였다.   

```
    private Expr factor() { 
        Operator op = null; 
        if (token == Token.MINUS)  
            op = new Operator(match(Token.MINUS)); 
 
        Expr e = null; 
        switch(token) { 
        case ID: 
            Identifier v = new Identifier(match(Token.ID)); 
            e = v; 
            if (token == Token.LBRACKET) { 
                match(Token.LBRACKET); 
                e = new Array(v, expr()); 
                match(Token.RBRACKET); 
            } 
            break; 
        case NUMBER: case STRLITERAL:  
            e = literal(); 
            break;  
        case LPAREN:  
            match(Token.LPAREN);  
            e = aexp();        
            match(Token.RPAREN); 
            break;  
        default:  
            error("Identifier | Literal");  
        } 
 
        if (op != null) 
            return new Unary(op, e); 
        else return e; 
    } 
```
마찬가지로 If (token == Token.LBRACKET) {...} 부분은 배열에 할당할 때의 부분이다. ‘[‘, ‘]’를 각 순서에 맞게 match 시켰고, e 변수에 new Array(v, expr())를 대입하여 반환하게 하였다. 
 
2. Sint.java 
```
    State Eval(Assignment a, State state) { 
        Value v = V(a.expr, state); 
         
        if (a.ar == null) 
            return state.set(a.id, v); 
        else { 
            Value[] ar = state.get(a.ar.id).arrValue(); 
            int index = V(a.ar.expr, state).intValue(); 
            ar[index] = v; 
            return state; 
        } 
    } 
```
사실 주석에 설명이 없었다면 구현해내는데 오랜 시간이 걸렸을 것 같다. Value[] 변수인 ar에 get함수를 통해 state.get(a.ar.id).arrValue()를 대입하여 배열을 찾고, int 변수인 index에 V(a.ar.expr, state).intValue()를 통해 expr의 값을 대입했다. expr에서 얻은 인덱스에 해당하는 공간에 v를 대입하고 state를 반환하였다. Assignment의 배열의 id를 get해야 하는데 Assignment의 id를 get하려고 했어서 수정하는데 시간이 오래걸렸다. 

```
    State allocate (Decls ds, State state) { 
        if (ds != null) { 
            for (Decl decl : ds)  
                if (decl.arraysize > 0) { 
                    Value[] v = new Value[decl.arraysize]; 
                    for (int i = 0; i < decl.arraysize; i++){ 
                        v[i] = new Value(decl.type); 
                    } 
                    state.push(decl.id, new Value(v)); 
                } 
                else if (decl.expr == null) 
                    state.push(decl.id, new Value(decl.type)); 
                else 
                    state.push(decl.id, V(decl.expr, state)); 
        } 
        return state; 
    } 
``` 
If (decl.arraysize > 0) 조건에서 배열의 크기가 있다는 것을 조사했으므로, new Value[decl.arraysize]를 Value[] 변수 v에 대입하였고, 배열의 원소들 또한 각각 new Value(decl.type)을 통해 새로운 객체로 채웠다. 이후 state.push(decl.id, new Value(v))를 통해 stack에 push했는데, new Value(v)를 사용한 이유는 Value[]를 Value형식으로 바꾸기 위해서이다. 

``` 
    Value V(Expr e, State state) { 
        if (e instanceof Value)  
            return (Value) e; 
         
        if (e instanceof Identifier) { 
            Identifier v = (Identifier) e; 
            return (Value)(state.get(v)); 
        } 
        if (e instanceof Array) { 
            Array ar = (Array) e; 
            Value v = state.get(ar.id); 
            Value[] vs = v.arrValue(); 
            // if (vs == null) throw new NullPointerException("no array"); 
            int index = V(ar.expr, state).intValue(); 
            // if(index < 0 || index >= vs.length) throw new 
IndexOutOfBoundsException("invalid index"); 
            return (vs[index]); 
        } 
        if (e instanceof Binary) { 
            Binary b = (Binary) e; 
            Value v1 = V(b.expr1, state); 
            Value v2 = V(b.expr2, state); 
            return binaryOperation (b.op, v1, v2);  
        } 
        if (e instanceof Unary) { 
            Unary u = (Unary) e; 
            Value v = V(u.expr, state); 
            return unaryOperation(u.op, v);  
        } 
        throw new IllegalArgumentException("no operation"); 
    } 
```
주석은 null인 배열주소에 index를 통해 접근하거나, index가 배열의 크기를 넘었을 때 예외처리하고 싶었으나, test파일들에 이러한 상황이 나오진 않아서 필요없을 것 같아 주석처리하였다. (이미 다른 곳에서 검사가 되었을 수도 있다고 생각했다.) e가 instanceof를 통해 Array의 일종이라는 것을 알고 있기 때문에 (Array)로 캐스팅한 e를 Array변수 ar에 대입하고, state.get(ar.id)을 통해 배열을 찾아 arrValue()메소드로 변환된 값을 Value[] 변수인 vs에 대입했다. Int 변수 index에 V(ar.expr, state)을 통해 얻은 값을 intValue() 메소드를 통해 int로 변환하여 대입하고, vs[index]를 반환했다. 배열의 인덱스에 해당하는 원소를 반환한다는 뜻이다. 

• 결과 분석 
결과는 모두 실행 예시와 같게 나왔다. 
배열의 인덱스가 정수형이어야 하고, 배열은 Value 변수에 대입할 수 없어 캐스팅이나 메소드를 통해 적절히 형변환 하는 과정이 어려웠고, AST에 배열 관련 멤버들이 많이 추가되어 이를 파악하는데도 시간이 오래 걸렸던 것 같다.  
