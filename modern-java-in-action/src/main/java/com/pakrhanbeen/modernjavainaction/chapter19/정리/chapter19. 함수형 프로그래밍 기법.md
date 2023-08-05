# 함수형 프로그래밍 기법

## 함수는 모든 곳에 존재한다

* 일반값처럼 취급할 수 있는 함수를 **일급 함수**라고 한다.
* 자바 8에서는 `::` 연산자로 **메서드 참조**를 만들거나 `(int x) -> x + 1` 같은 람다 표현식으로 직접 함숫값을 표현해서 메서드를 함숫값으로 사용할 수 있다.

### 고차원 함수

* 고차원 함수는 하나 이상의 동작을 수행하는 함수이다.
  * 하나 이상의 함수를 인수로 받음
  * 함수를 결과로 반환
* 자바 8에서는 함수를 인수로 전달할 수 있을 뿐 아니라 결과를 반환하고, 지역 변수로 할당하거나, 구조체로 삽입할 수 있으므로 자바 8의 함수도 고차원 함수라고 할 수 있다.

### 커링

* 커링은 x와 y라는 두 인수를 받는 함수 f를 한 개의 인수를 받는 g라는 함수로 대체하는 기법이다. 이때 g라는 함수 역시 하나의 인수를 받는 함수를 반환한다.
* 함수 g와 원래 함수 f가 최종적으로 반환하는 값은 같다. 즉, `f(x, y) = (g(x))(y)`가 성립한다.

## 영속 자료구조

* 함수형 프로그램에서는 함수형 자료구조, 불변 자료구조 등의 용어도 사용하지만 보통은 영속 자료구조라고 부른다.
* 함수형 메서드에서는 전역 자료구조나 인수로 전달된 구조를 갱신할 수 없다. 자료구조를 바꾼다면 같은 메서드를 두 번 호출했을 때 결과가 달라지면서 참조 투명성에 위배되고
 인수를 결과로 단순하게 매핑할 수 있는 능력이 상실되기 때문이다.
* 함수형에서 자료구조가 갱신되면서 생기는 부작용은 메서드를 제한하는 방식으로 문제를 해결한다.
* 계산결과를 표현할 자료구조가 필요하면 기존의 자료구조를 갱신하지 않도록 새로운 자료구조를 만들어야 한다. 이는 표준 객체지향 프로그래밍의 관점에서도 좋은 기법이다.

## 스트림과 게으른 평가

* 효율적인 구현 및 여러 이유로 자바 8 설계자들은 스트림을 조금 특별한 방법으로 자바 8에 추가했다.
* 그중 하나로 스트림은 단 한 번만 소비할 수 있다는 제약이 있어서 스트림은 재귀적으로 정의할 수 없다.

### 게으른 평가

* `자바 8의 스트림은 게으르다`라는 말이 있다. 자바 8의 스트림은 요청할 때만 값을 생성하는 블랙박스와 같다.
* 스트림에 일련의 연산을 적용하면 연산이 수행되지 않고 일단 저장된다.
* 스트림에 **최종 연산**을 적용해서 실제 계산을 해야 하는 상황에서만 실제 연산이 이루어 진다. 특히 스트림에 여러 연산(`filter, map, reduce`등)을 적용할 때 이와 같은 특성을 활용할 수 있다.
* 게으른 특성 때문에 각 연산별로 스트림을 탐색할 필요 없이 한 번에 여러 연산을 처리할 수 있다.
* 게으른 자료구조는 강력한 프로그래밍 도구라는 사실을 기억하자. 애플리케이션을 구현하는데 도움을 준다면 게으른 자료구조를 사용하자.
* 하지만 게으른 자료구조 때문에 효율성이 떨어진다면 전통적인 방식으로 코드로 구현하자.

## 패턴 매칭

* 거의 모든 함수형 프로그래밍 언어에서는 제공하지만 자바에서는 지원하지 하지 않는 기능이다.

### 방문자 디자인 패턴

* 자바에서는 **방문자 디자인 패턴**으로 자료형을 언랩할 수 있다. 특히 특정 데이터 형식을 '방문'하는 알고리즘을 캡슐화하는 클래스를 따로 만들 수 있다.
* 방문자 클래스는 지정된 데이터 형식의 인스턴스를 입력으로 받는다. 그리고 인스턴스의 모든 멤버에 접근한다. 
```java
class BinOp extends Expr {
    ...
    public Expr accept(SimplifyExprVisitor v) {
        return v.visit(this);
    }
}

class SimplifyExprVisitor {
    ...
    public Expr visit(BinOp e) {
        if ("+".equals(e.opname) && e.right instanceof Number && ...) {
            return e.left;
        }
        return e;
    }
}
```
* `SimplifyExprVisitor`를 인수로 받는 `accept`를 `BinOp`에 추가한 다음에 `BinOp` 자신을 `SimplifyExprVisitor`로 전달한다

### 패턴 매칭의 힘

* 패턴 매칭으로 단순하게 해결할 수 있다. 하지만 자바는 패턴 매칭을 지원하지 않는다.
```
// 스칼라는 패턴 매칭을 지원함.
def simplifyExpression(expr: Expr): Expr = expr match {
    case BinOp("+", e, Number(0)) => e  // 0 더하기
	case BinOp("*", e, Number(1)) => e  // 1 곱하기
	case BinOp("/", e, Number(1)) => e  // 1 나누기
	case _ => expr  // expr을 단순화할 수 없다
}
```
* 하지만 자바 8의 람다를 이용하면 패턴 매칭과 비슷한 코드를 만들 수 있다.

### 자바로 패턴 매칭 흉내 내기

```java
interface TriFunction<S, T, U, R> {
    R apply(S s, T t, U u);
}

public class Main {
    public static void main(String[] args) {
        patternMatchExpr(e, (op, l, r) -> {return binopcode;},
            (n) -> {return numcode;},
            () -> {return defaultcode;}
        );
    }
    
    static <T> T patternMatchExpr(Expr e,
        TriFunction<String, Expr, Expr, T> binopcase,
        Function<Integer, T> numcase,
        Supplier<T> defaultcase) {
        return (e instanceof Binop) ? binopcase.apply(((BinOp)e).opname,
            ((BinOp)e).left, ((BinOp)e).right) :
            (e instanceof Number) ? numcase.apply(((Number)e).val) : defaultcase.get();
    }
}
```

### 캐싱 또는 기억화

* **기억화**는 메서드에 래퍼로 캐시(HashMap 같은)를 추가하는 기법이다.
  * 래퍼가 호출되면 인수, 결과 쌍이 캐시에 존재하는지 먼저 확인한다.
  * 캐시에 값이 존재하면 캐시에 저장된 값을 즉시 반환한다.
  * 캐시 값이 존재하지 않으면 `computeNumberOfNodes`를 호출해서 결과를 계산한 다음에 새로운 인수, 결과 쌍을 캐시에 저장하고 결과를 반환한다.
* 엄밀히 따져서 캐싱, 즉 다수의 호출자가 공유하는 자료구조를 갱신하는 기법이므로 이는 순수 함수형 해결방식은 아니지만 감싼 버전의 코드는 참조 투명성을 유지할 수 있다.
```java
final Map<Range, Integer> numberOfNodes = new HashMap<>();
Integer computeNumberOfNodesUsingCache(Range range) {
    Integer result = numberOfNodes.get(range);
    if (result != null) {
        return result;
    }
    result = computeNumberOfNodes(range);
    numberOfNodes.put(range, result);
    return result;
}
```

### 콤비네이터

* 함수형 프로그래밍에서는 두 함수를 인수로 받아 다른 함수를 반환하는 등 함수를 조합하는 고차원 함수를 많이 사용하게 된다.
* 이처럼 함수를 조합하는 기능을 콤비네이터라고 부른다. 자바 8 API에 추가된 많은 기능은 **콤비네이터**의 영향을 받았다.
