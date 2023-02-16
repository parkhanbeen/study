# 람다 표현식

## 람다란?

* **람다 표현식**은 메서드로 전달할 수 있는 익명 함술르 단순화한 것이라고 할 수 있다.

### 람다 특징

* **익명** : 보통의 메서드와 달리 이름이 없으므로 **익명**이라 표현한다. 구현해야 할 코드에 대한 걱정거리가 줄어든다.
* **함수** : 람다는 메서드처럼 특정 클래스에 종속되지 않으므로 함수라고 부른다. 하지만 메서드처럼 파라미터 리스트, 바디, 반환 형식, 가능한 예외리스트를 포함한다.
* **전달** : 람다 표현식을 메서드 인수로 전달하거나 변수로 저장할 수 있다.
* **간결성** : 익명 클래스처럼 많은 자질구레한 코드를 구현할 필요가 없다.

```java
Comparator<Apple> byWeight = new Comparator<Apple>() {
    public int compare(Apple a1, Apple a2) {
        return a1.getWeight().compareTo(a2.getWeight());
    }
}

// 람다로 변환
Comparator<Apple> byWeight = (Apple a1, Apple a2) -> a1.getWeight().compareTo(a2.getWeight());
```

### 람다 구성

```java
(Apple a1, Apple a2)  ->  a1.getWeight().compareTo(a2.getWeight());
// <람다 파라미터>     <화살표>                   <람다 바디>
```

* **파라미터 리스트**
  * `Comparator`의 `compare` 메서드 파라미터(사과 두 개)
* **화살표**
  * 화살표(`->`)는 람다의 파라미터 리스트와 바디를 구분한다.
* **람다 바디**
  * 두 사과의 무게를 비교한다. 람다의 반환값에 해당하는 표현식이다.

## 어디에, 어떻게 람다를 사용할까?

### 함수형 인터페이스

* **함수형 인터페이스**는 정확히 하나의 추상 메서드를 지정하는 인터페이스다.

```java
/**
 * java.util.Comparator.
 */
public interface Comparator<T> {
    int compare(T o1, T o2);
}

/**
 * java.lang.Runnable.
 */
public interface Runnable {
    void run();
}

/**
 * java.awt.event.ActionListener.
 */
public interface ActionListener extends EventListener {
    void actionPerformed(ActionEvent e);
}

/**
 * java.util.concurrent.Callable.
 */
public interface Callable<V> {
    V call() throws Exception;
}

/**
 * java.security.PrivilegedAction.
 */
public interface PrivilegedAction<T> {
    T run();
}
```

* **디폴트 메서드**를 포함하더라도 **추상 메서드가 오직 하나면** 함수형 인터페이스이다.
* 람다 표현식으로 함수형 인터페이스의 추상 메서드 구현을 직접 전달할 수 있으므로 **전체 표현식을 함수형 인터페이스로 취급**할 수 있다.

### 함수 디스크립터

* 람다 표현식의 시그니처를 서술하는 메서드를 **함수 디스크립터**라고 부른다.
  * ex) `(Apple, Apple) -> int`, `() -> T : Suuplier`

### @FunctionalInterface 란?

* 자바8 에 새롭게 추가된 어노테이션으로 `@FunctionalInterface`는 함수형 인터페이스를 가리키는 어노테이션이다.
* `@FunctionalInterface`로 인터페이스를 선언했지만 실제로 함수형 인터페이스가 아니라면 컴파일러가 에러를 발생시킨다.

### 실행 어라운드 패턴

* 실제 자원을 처리하는 코드를 설정과 정리 두 과정이 둘러싸는 형태를 **실행 어라운드 패턴**이라고 부른다.

```java
public String processFile() throws IOException {
    try (BufferedReader br = new BufferedReader(new FileReader("data.txt"))){
        return br.readLine();  // 실제 필요한 작업을 하는 행
    }
}
```

#### 1 단계 : 동작 파라미터화

* 현재 코드는 한 번에 한줄만 읽을 수 있다. 한 번에 두 줄을 읽거나 가장 자주 사용되는 단어를 반환하려면 어떻게 해야 할까?
* 기존의 설정, 정리 과정은 재사용하고 `processFile` 메서드만 다른 동작을 수행하도록 명령할 수 있다면 좋을 것이다.
* `processFile` 메서드가 `BufferedReader`를 이용해서 다른 동작을 수행할 수 있도록 `processFile` 메서드로 동작을 전달해야 한다.

```java
String result = processFile((BufferedReader br) -> br.readLine() + br.readLine());
```

* 람다를 이용해 `BufferedReader`에서 두행을 출력하는 코드이다.

#### 2 단계 : 함수형 인터페이스를 이용해 동작 전달

* 함수형 인터페이스 자리에 람다를 사용할 수 있다. 
* `BufferedReader -> String`과 `IOException`을 던질 수 있는 시그니처와 일치하는 함수형 인터페이스를 만들어보자.

```java

@FunctionalInterface
public interface BufferedReaderProcessor {
  String process(BufferedReader b) throws IOException;
}

public String processFile(BufferedReaderProcessor p) throws IOException {
  ...
}
```

#### 3 단계 : 동작 실행

```java
public String processFile(BufferedReaderProcessor p) throws IOException {
    try (BufferedReader br = new BufferedReader(new FileReader("data.txt"))) {
        return p.process(br);
    }
}
```

#### 4 단계 : 람다 전달

```java
// 한 행 처리
String oneLine = processFile((BufferedReader br) -> br.readLine());

// 두 행 처리
String twoLines = processFile((BufferedReader br) -> br.readLine() + br.readLine());
```

## 함수형 인터페이스 사용

* 자바 8은 [java.util.function](https://docs.oracle.com/javase/8/docs/api/java/util/function/package-summary.html)
패키지로 여러 가지 새로운 함수형 인터페이스를 제공한다.

### Predicate

* [java.util.function.Predicate](https://docs.oracle.com/javase/8/docs/api/java/util/function/Predicate.html) 인터페이스는
 `test`라는 추상 메서드를 정의하며 `test`는 제네릭 형식 `T`의 객체를 인수로 받아 불리언을 반환한다.

```java
@FuncationalInterface
public interface Predicate<T> {
	boolean test(T t);
}
```

### Consumer

* [java.util.function.Consumer](https://docs.oracle.com/javase/8/docs/api/java/util/function/Consumer.html) 인터페이스는
 제네릭 `T` 객체를 받아서 `void`를 반환하는 `accept`라는 추상 메서드를 정의한다.

```java
@FuncationalInterface
public interface Consumer<T> {
	void accept(T t);
}
```

### Function

* [java.util.function.Function<T, R>](https://docs.oracle.com/javase/8/docs/api/java/util/function/Function.html) 인터페이스는
 제네릭 `T`를 인수로 받아서 제네릭 형식 `R` 객체를 반환하는 추상 메서드 `apply`를 정의한다.

```java
@FuncionalInterface
public interface Function<T, R> {
    R apply(T t);
}
```

### 기본형 특화

* 자바 8에서는 기본형을 입출력으로 사용하는 상황에서 오토박싱 동작을 피할 수 있도록 특별한 버전의 함수형 인터페이스를 제공한다.

```java
public interface IntPredicate {
    boolean test(int t);
}

IntPredicate evenNumbers = (int i) -> i % 2 == 0;
evenNumbers.test(1000);  // 참(박싱 없음)

IntPredicate evenNumbers = (Integer i) -> i % 2 != 0;
evenNumbers.test(1000);  // 거짓(박싱)
```

* 특정 형식을 입력으로 받는 함수형 인터페이스의 이름 앞에는 `DoublePredicate, IntConsumer, LongBinaryOperator, IntFunction` 처럼 형식명이 붙는다.
* `Function`인터페이스는 `ToIntFunction<T>, IntToDoubleFunction` 등의 다양한 출력 형식 파라미터를 제공한다.

### 람다와 형수형 인터페이스

| 사용 사례      | 람다 예제                                                          | 대응하는 함수형 인터페이스                                                                                     |
|------------|----------------------------------------------------------------|----------------------------------------------------------------------------------------------------|
| 불리언 표현     | (List<String> list) -> list.isEmpty()                          | Predicate<List<String>>                                                                            |
| 객체 생성      | () -> new Apple(10)                                            | Supplier<Apple>                                                                                    |
| 객체에서 소비    | (Apple a) -> System.out.println(a.getWeight())                 | Consumer<Apple>                                                                                    |
| 객체에서 선택/추출 | (String s) -> s.length()                                       | Function<String, Integer> <br/> or ToIntFunction<String>                                           |
| 두 값 조합     | (int a, int b) -> a * b                                        | IntBinaryOperator                                                                                  |
| 두 객체 비교    | (Apple a1, Apple a2) -> <br/>a.getWeight().compareTo(a2.getWeight() | Comparator<Apple> or <br/>BiFunction<Apple, Apple, Integer> <br/> or ToIntBiFunction<Apple, Apple> |

### 예외, 람다, 함수형 인터페이스의 관계

* 함수형 인터페이스는 확인된 예외를 던지는 동작을 허용하지 않는다. 
* 즉, 예외를 던지는 람다 표현식을 만들려면 확인된 예외를 선언하는 함수형 인터페이스를 직접 정의하거나 람다를 `try/catch` 블록으로 감싸야 한다.
```java
@FunctionalInterface
public interface BufferedReaderProcessor {
  String process(BufferedReader b) throws IOException;
}
```

* 그러나 우리는 `Function<T, R>` 형식의 함수형 인터페이스를 기대하는 `API`를 사용하고 있으며 직접 함수형 인터페이스를 만들기 어려운 상황이다.

```java
Function<BufferdReader, String> f = (BufferedReader b) -> {
    try {
        return b.readLine();
    } catch(IOException e) {
        throw new RuntimeException(e);
    }
}
```

* 위 코드처럼 명시적으로 확인된 예외를 잡을 수 있다.

## 형식 검사, 형식 추론, 제약

### 형식 검사

* 람다가 사용되는 콘텍스트를 이용해서 람다의 형식을 추론할 수 있다.
* 어떤 콘텍스트에서 기대되는 람다 표현식의 형식을 **대상 형식**이라고 부른다.

```java
List<Apple> heavierThan150g = filter(inventory, (Apple apple) -> apple.getWeight() > 150);
```

1. `filter` 메서드의 선언을 확인한다.
2. `filter` 메서드는 두 번째 파라미터로 `Predicate<Apple>` 형식을 기대한다.
3. `Predicate<Apple>`은 `test`라는 한 개의 추상 메서드를 정의하는 함수형 인터페이스다.
4. `test` 메서드는 `Apple`을 받아 `boolean`을 반환하는 함수 디스크립터를 묘사한다.
5. `filter` 메서드로 전달된 인수는 이와 같은 요구사항을 만족해야 한다.

### 형식 추론

* 자바 컴파일러는 람다 표현식이 사용된 콘텍스트(대상 형식)를 이용해서 람다 표현식과 관련된 함수형 인터페이스를 추론한다.
* 즉, 대상형식을 이용해서 함수 디스크립터를 알 수 있으므로 컴파일러는 람다의 시그니처도 추론할 수 있다.
* 결과적으로 컴파일러는 람다 표현식의 파라미터 형식에 접근할 수 있으므로 람다 문법에서 이를 생략할 수 있다.
* 즉, 자바 컴파일러는 다음처럼 람다 파라미터 형식을 추론할 수 있다.

```java
// 형식을 추론하지 않음
Comparator<Apple> c = (Apple a1, Apple a2) -> a1.getWeight().compareTo(a2.getWeight());

// 형식을 추론함
Comparator<Apple> c = (a1, a2) -> a1.getWeight().compareTo(a2.getWeight());
```

* 상황에 따라 명시적으로 형식을 포함하는 것이 좋을 때도 있고 형식을 배제하는 것이 가독성을 향상시킬 때로 있다.
* 규칙은 없다. 개발자 스스로 어떤 코드가 가독성을 향상시킬 수 있는지 결정해야 한다.

### 지역 변수 사용

* 람다 표현식에서는 익명 함수가 하는 것처럼 **자유 변수**(파라미터로 넘겨진 변수가 아닌 외부에서 정의된 변수)를 활용할 수 있다.
 이와 같은 동작을 **람다 캡처링**이라고 부른다.

```java
int portNumber = 1337;
Runnable r = () -> System.out.println(portNumber);
```

* 람다 표현식은 한 번만 할당할 수 있는 지역 변수를 캡처할 수 있다. (인스턴스 변수 캡처는 `final` 지역 번수 `this`를 캡처하는 것과 마찬가지)

```java
// 에러: 람다에서 참고하는 지역 변수는 final 로 선언되거나 실질적으로 final 처럼 취급되어야 한다.
int portNumber = 1337;
Runnalbe r = () -> System.out.println(portNumber);
portNumber = 31337;
```

#### 지역 변수의 제약

* 인스턴스 변수는 힙에 저장되는 반면 지역 변수는 스택에 위치한다.
* 람다에서 지역 변수에 바로 접근할 수 있다는 가정하에 람다가 스레드에서 실행된다면 변수를 할당한 스레드가 사라져서 변수 할당이 해제되었는데도 
 람다를 실행하는 스레드에서는 해당 변수에 접근하려 할 수 있다.
* 따라서 자바 구현에서는 원래 변수에 접근을 허용하는 것이 아니라 자유 지역 변수의 복사본을 제공한다.
* 따라서 복사본의 값이 바뀌지 않아야 하므로 지역 변수에는 한 번만 값을 할당해야 한다는 제약이 생긴 것이다.

#### 클로저

* 원칙적으로 클로저란 함수의 비지역 변수를 자유롭게 참조할 수 있는 함수의 인스턴스를 가리킨다.
* 자바 8의 람다와 익명 클래스는 클로저와 비슷한 동작을 수행한다. 모두 메서드의 인수로 전달될 수 있으며 자신의 외부 영역의 변수에 접근할 수 있다.
* 다만 람다와 익명 클래스는 람다가 정의된 메서드의 지역 변수의 값은 바꿀 수 없다.
* 덕분에 람다는 변수가 아닌 값에 국한되어 어떤 동작을 수행한다는 사실이 명확해진다.
* 지역 변수값은 스택에 존재하므로 자신을 정의한 스레드와 생존을 같이 해야 하며 따라서 지역 변수는 `final`이어야 한다.
* 가변 지역 변수를 새로운 스레드에서 캡처할 수 있다면 안전하지 않은 동작을 수행할 가능성이 생긴다.(인스턴스 변수는 스레드가 공유하는 힙에 존재하므로 특별한 제약이 없다)

## 메서드 참조

* 메서드 참조를 이용하면 기존의 메서드 정의를 재활용해서 람다처럼 전달할 수 있다.

```java
inventory.sort((Apple a1, Apple a2) -> a1.getWeight().compareTo(a2.getWeight()));

// 메서드 참조
inventory.sort(comparing(Apple::getWeight));
```

1. **정적 메서드 참조**
   * `Integer::parseInt`
2. **다양한 형식의 인스턴스 메서드 참조**
   * `String.length ==> String::length`, `(String s) -> s.toUpperCase() ==> String::toUpperCase`
3. **기존 객체의 인스턴스 메서드 참조**
   * 예를 들어 `Transaction` 객체를 할당 받은 `expensiveTransaction` 지역 변수가 있고, `Transaction` 객체에는 `getValue` 메서드가 있다면,
    `expensiveTransaction::getValue`로 표현
   * `() -> expensiveTransaction.getValue() ==> expensiveTransaction::getValue`

* `ClassName::new`처럼 클래스명과 `new` 키워드를 이용해서 기존 생성자의 참조를 만들 수 있다.
* 이것은 정적 메서드의 참조를 만드는 방법과 비슷하다.

```java
// Supplier 의 get 메서드를 호출해 새로운 객체를 만들 수 있다. 
Supplier<Apple> c1 = Apple::new
Apple a1 = c1.get();

// Apple(Integer weight) 시그니처를 갖는 생성자는 Function 인터페이스로 구현 가능
Function<Integer, Apple> c2 = Apple::new
Apple a2 = c2.apply(110);

// Apple(String color, Integer weight) 두 인수를 갖는 생성자는 BiFunction 인터페이스와 같은 시그니처
BiFunction<Color, Integer, Apple> c3 = Apple:new;
Apple a3 = c3.apply(GREEN, 110);
```

## 람다 표현식을 조합할 수 있는 유용한 메서드

* 자바 8 API의 몇몇 함수형 인터페이스는 다양한 유틸리티 메서드를 포함한다.
  * ex) `Comparator, Function, Predicate` 같은 함수형 인터페이스는 람다 표현식을 조합할 수 있도록 유틸리티 메서드 제공
* 간단한 여러 개의 람다 표현식을 조합해서 복잡한 람다 표현식을 만들 수 있다.
* **디폴트 메서드**를 이용해 함수형 인터페이스의 정의에 벗어나지 않고 유틸리티 메서드를 제공한다.

### Comparator 조합

* 정적 메서드 `Comparator.comparing`을 이용해서 비교에 사용할 키를 추출하는 `Function`기반의 `Comparator`를 반환할 수 있다.

```java
Comparator<Apple> c = Comparator.comparing(Apple::getWeight);

// 역정렬 - 무게를 내림차순으로 정렬
inventory.sort(comparing(Apple::getWeight).reversed());
```

#### Comparator 연결

* 만약 무게가 같은 사과가 존재한다면? 이럴 땐 비교 결과를 더 다듬을 수 있는 두번째 `Comparator` 만들자.
```java
inventory.sort(comparing(Apple::getWeight)
    .reversed()  // 무게를 내림차순으로 정렬
    .thenComparing(Apple::getContry));    // 두 사과의 무게가 같으면 구가별로 정렬
```

#### Predicate 조합

* `Predicate` 인터페이스는 복잡한 프레디케이트를 만들 수 있도록 `negate, and, or` 세 가지 메서드를 제공한다.

```java
// 기존 프레디케이트 객체 redApple의 결과를 반전시킨 객체를 만든다.
Predicate<Apple> notRedApple = redApple.negate();  

// 두 프레디케이트를 연결해서 새로운 프레디케이트 객체를 만든다.
Predicate<Apple> redAndHeavyApple = redApple.and(apple -> apple.getWeight() > 150);

// or 을 이용해 "빨간색이면서 무거운(150 이상) 사과 또는 그냥 녹색 사과" 추출
Predicate<Apple> redAndHeavyAppleOrGreen = 
    redApple
    .and(apple -> apple.getWeight() >150)
    .or(apple -> GREEN.equals(a.getColor()));
```

* 단순한 람다 표현식을 조합해서 더 복잡한 람다 표현식을 만들 수 있고 심지어 람다 표현식을 조합해도 코드 자체가 문제를 잘 설명한다.

#### Function 조합

* `Function` 인터페이스는 `andThen, compose` 두 가지 디폴트 메서드를 제공한다.


* **andThen**
  * 주어진 함수를 먼저 적용한 결과를 다른 함수의 입력으로 전달하는 함수르 반환한다.
  ```java
  Funtion<Integer, Integer> f = x -> x + 1;
  Funtion<Integer, Integer> g = x -> x * 2;
  Funtion<Integer, Integer> h = f.andThen(g);
  int result = h.apply(1);   // 4를 반환
  ```

* **compose**
    * 인수로 주어진 함수를 먼저 실행한 다음에 그 결과를 외부 함수의 인수로 제공한다.
  ```java
  Funtion<Integer, Integer> f = x -> x + 1;
  Funtion<Integer, Integer> g = x -> x * 2;
  Funtion<Integer, Integer> h = f.compose(g);
  int result = h.apply(1);   // 3를 반환
  ```
