# 리팩터링, 테스팅, 디버깅

## 가독성과 유연성을 개선하는 리팩터링

### 코드 가독성 개선

* 일반적으로 코드 가독성이 좋다는 것은 `어떤 코드를 다른 사람도 쉽게 이해할 수 있음`을 의미한다.
  * 즉, 코드 가독성을 개선한다는 것은 우리가 구현한 코드를 다른 사람이 쉽게 이해하고 유지보수할 수 있게 만드는 것을 의미한다.
* 자바 8의 새 기능을 이용해 코드의 가독성을 높일 수 있다. 코드를 간결하고 이해하기 쉽게 만들 수 있다. 또한 메서드 참조와 스트림 API를 이용해 코드의
 의도를 명확하게 보여줄 수 있다.

### 익명 클래스를 람다 표현식으로 리팩터링하기

* 하나의 추상 메서드를 구현하는 익명 클래스는 람다 표현식으로 리펙터링할 수 있다.
* 하지만 모든 익명 클래스를 람다 표현식으로 변환할 수 있는 것은 아니다.
  * **익명 클래스에서 사용한 `this, super`는 람다 표현식에서 다른 의미를 갖는다.**
    * 익명 클래스에서 `this`는 익명클래스 자신을 가리키지만 람다에서 `this`는 람다를 감싸는 클래스를 가리킨다.
  * **익명 클래스는 감싸고 있는 클래스의 변수를 가릴 수 있다(섀도 변수 shadow variable).**
    * 하지만 람다 표현식으로는 변수를 가릴 수 없다.
    * ```java
      int a = 10;
      Runnable r1 = () -> {
        int a = 2;  // 컴파일 에러
      }
      
      Runnable r2 = new Runnable() {
        public void run() {
            int a = 2;  // 잘 동작한다.
        }
      }
      ```
  * **익명 클래스를 람다 표현식으로 바꾸면 콘텍스트 오버로딩에 따른 모호함이 초래될 수 있다.**
    * 익명 클래스는 인스턴스화할 때 명시적으로 형식이 정해지는 반면 람다의 형식은 콘텍스트에 따라 달라지기 때문이다.
    * ```java
      interface Task {
        public void execute();
      }
      public static void doSomething(Runnalbe r) {
        r.run();
      }
      public static void doSomething(Task a) {
        r.execute();
      }
      
      // Task를 구현하는 익명 클래스를 전달
      doSomething(new Task() {
        public void execute() {
            System.out.println("Danger danger!!");
        }
      });
      ```
    * 하지만 익명 클래스를 람다 표현식으로 바꾸면 메서드를 호출할 때 `Runnable`과 `Task` 모두 대상 형식이 될 수 있으므로 문제가 발생한다.
    * ```java
      doSomething(() -> System.out.println("Danger danger!!"));
      ```
    * 즉, `doSomething(Runnable)`과 `doSomething(Task)` 중 어느 것을 가리키는지 알 수 없는 모호함이 발생한다.
    * 하지만 `IDE`에서 제공하는 리펙터링 기능을 이용하면 이와 같은 문제가 자동으로 해결된다.

### 람다 표현식을 메서드 참조로 리팩터링하기

* 람다 표현식 대신 메서드 참조를 이용하면 가독성을 높일 수 있다.
```java
menu.stream()
    .collect(
        groupingBy(dish -> {
            if (dish.getCalories() <= 400) return CaloricLevel.DIET;
            else if (dish.getCalories() <= 700) return CaloricLevel.NORMAL;
            else return CaloricLevel.FAT;
    })
    )

// 람다 표현식을 별도의 메서드로 추출
menu.stream().collect(groupingBy(Dish::getCaloricLevel));
```

* `comparing, maxBy` 같은 정적 헬퍼 메서드를 활용하는 것도 좋다.
```java
inventory.sort((Apple a1, Apple a2) -> a1.getWeight().compareTo(a2.getWeight));  // 비교 구현에 신경 써야 한다.
    
inventory.sort(comparing(Apple::getWeight));  // 코드가 문제 자체를 설명한다.
```

* `sum, maximum`등 자주 사용하는 리듀싱 연산은 메서드 참조와 함께 사용할 수 있는 내장 헬퍼 메서드를 제공한다.
```java
int totalCalories = menu.stream()
    .map(Dish::getCalories)
    .reduce(0, (c1, c2) -> c1 + c2);

// summingInt 내장 컬렉터를 이용
int totalCalories = menu.stream()
    .collect(summingInt(Dish::getCalories));
```

### 명령형 데이터 처리를 스트림으로 리팩터링 하기

* 스트림 API는 데이터 처리 파이프라인의 의도를 명확하게 보여준다.
* 스트림은 쇼트서킷과 게으름이라는 강력한 최적화뿐 아니라 멀티코어 아키텍처를 활용할 수 있는 지름길을 제공한다.
* 명령형 코드의 `break, continue, return`등의 제어 흐름문을 모두 분석해서 같은 기능을 수행하는 스트림 연산으로 유추해야 하므로 명령형
 코드를 스트림 API로 바꾸는 것은 쉬운 일이 아니다.

### 코드 유연성 개선

* 람다 표현식을 이용하면 동작 파라미터화(behaviour parameterization)를 쉽게 구현할 수 있다.
  * 즉, 다양한 람다를 전달해서 다양한 동작을 표현할 수 있다.

#### 함수형 인터페이스 적용

* 먼저 람다 표현식을 이용하려면 함수형 인터페이스가 필요하다. 따라서 함수형 인터페이스를 코드에 추가해야 한다.

#### 조건부 연기 실행

* 실제 작업을 처리하는 코드 내부에 제어 흐름문이 복잡하게 얽힌 코드를 흔히 볼 수 있다.
```java
if (logger.isLoggable(Log.FINER)) {
    logger.finer("Problem: " + generateDianostic());
}
```

* 위 코드는 다음과 같은 문제가 있다.
  * `logger`의 상태가 `isLoggable`이라는 메서드에 의해 클라이언트 코드로 노출된다.
  * 메시지를 로깅할 때마다 `logger` 객체의 상태를 매번 확인해야 할까? 이들은 코드를 어지럽힐 뿐이다.
* 메시지를 로깅하기 전에 `logger` 객체가 적절한 수준으로 설정되었는지 내부적으로 확인하는 `log` 메서드를 사용하는 것이 바람직하다.
```java
logger.log(Level.FINER, "Problem: " + generateDianostic());
```
* 하지만 위 코드로 모든 문제가 해결된 것은 아니다.
  * 즉, 인수로 전달된 메시지 수준에서 `logger`가 활성화되어 있지 않더라도 항상 로깅 메시지를 평가하게 된다.
* 람다를 이용하면 이 문제를 쉽게 해결할 수 있다.
```java
public void log(Level level, Supplier<String> msgSupplier)
        
logger.log(Level.FINER, () -> Problem: " + generateDianostic());
```

#### 실행 어라운드

* 매번 같은 준비, 종료 과정을 반복적으로 수행하는 코드가 있다면 이를 람다로 변환할 수 있다.
  * 준비, 종료 과정을 처리하는 로직을 재사용함으로써 코드 중복을 줄일 수 있다.
```java
String oneLine = processFile((BufferedReader b) -> b.readLine()); // 람다 전달
String twoLines = processFile((BufferedReader b) -> b.readLine() + b.readLine()); // 다른 람다 전달

public static String processFile(bufferedReaderProcessor p) throws IOException {
    try (BufferedReader br = new BufferedReader(new FileReader("ModernJavaInAction/chap9/data.txt"))) {
        return p.process(br);   // 인수로 전달된 BufferedReaderProcessor를 실행
    } 
}   

public interface BufferedReaderProcessor {  // IOException을 던질 수 있는 람다의 함수형 인터페이스
    String process(BufferedReader b) throws IOException;
}
```

## 람다로 객체지향 디자인 패턴 리팩터링하기

