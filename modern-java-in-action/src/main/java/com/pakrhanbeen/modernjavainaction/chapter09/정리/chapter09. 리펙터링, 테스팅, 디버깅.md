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

### 디자인 패턴

* 디자인 패턴은 공통적인 소프트웨어 문제를 설계할 때 재사용할 수 있는, 검증된 청사진을 제공한다.

#### 전략(strategy) 패턴

* 전략 패턴은 한 유형의 알고리즘을 보유한 상태에서 런타임에 적절한 알고리즘을 선택하는 기법이다.
  * 알고리즘을 나타내는 인터페이스
  * 다양한 알고리즘을 나타내는 한 개 이상의 인터페이스 구현
  * 전략 객체를 사용하는 한 개 이상의 클라이언트
```java
public interface ValidationStrategy {
    boolean execute(String s);
}

public class IsAllLowerCase implements ValidationStrategy {
    public boolean execute(String s) {
        return s.matches("[a-z]+");
    }
}

public class IsNumeric implements ValidationStrategy {
  public boolean execute(String s) {
    return s.matches("\\d+");
  }
}

public class Validator {
    private final ValidationStrategy strategy;
    
    public Validator(ValidationStrategy v) {
        this.strategy = v;
    }
    public boolean validate(String s) {
        return strategy.execute(s);
    }
}

Validator numericValidator = new Validator((String s) -> s.matches("[a-z]+"));
boolean b1 = numericValidator.validate("aaaa");
Validator lowerCaseValidator = new Validator((String s) -> s.matches("\\d+"));
boolean b2 = numericValidator.validate("bbbb");
```

* 람다 표현식을 이용하면 전략 패턴에서 발생하는 자잘한 코드를 제거할 수 있다.

#### 템플릿 메서드(template method) 패턴

* 알고리즘의 개요를 제시한 다음에 알고리즘의 일부를 고칠 수 있는 유연함을 제공해야 할 때 템플릿 메서드 디자인 패턴을 사용한다.
```java
abstract class OnlineBanking {
    public void processCustomer(int id) {
        Customer c = Database.getCustomerWithId(id);
        makeCustomerHappy(c);
    }
    abstract  void makeCustomerHappy(Customer c);
}

public void processCustomer(int id, Consumer<Customer> makeCustomerHappy) {
    Customer c = Database.getCustomerWithId(id);
    makeCustomerHappy.accept(c);
}

// 람다
new OnlineBankingLambda().processCustomer(1337, (Customer c) -> 
        System.out.println("Hello " + c.getName()));
```

#### 옵저버(observer) 패턴

* 어떤 이벤트가 발생했을 때 한 객체(**주체**)가 다른 객체 리스트(**옵저버**)에 자동으로 알림을 보내야 하는 상황에서 옵저버 패턴을 사용한다.
```java
interface Observer {
    void notify(String tweet);
}

class NYTimes implements Observer {
    public void notify(String tweet) {
        if (tweet != null && tweet.contains("money")) {
            System.out.println("Breaking news in NY! " + tweet);
        }
    }
}

class Guardian implements Observer {
    public void notify(String tweet) {
        if (tweet != null && tweet.contains("queen")) {
            System.out.println("Yet more news from London..." + tweet);
        }
    }
}

interface Subject {
    void registerObserver(Observer o);
    void notifyObservers(Observer o);
}

class Feed implements Subject {
    private final List<Observer> observers = new ArrayList<>();
    public void registerObserver(Observer o) {
        this.observers.add(o);
    }
    public void notifyObservers(String tweet) {
        observers.forEach(o -> o.notify(tweet));
    }
}

Feed f = new Feed();
f.registerObserver((String tweet) -> {
    if (tweet != null && tweet.contains("money")) {
        System.out.println("Breaking news in NY! " + tweet);
    }
});

f.registerObserver((String tweet) -> {
    if (tweet != null && tweet.contains("queen")) {
        System.out.println("Breaking news in London... " + tweet);
    }
})
```

#### 의무 체인(chain of responsibility)

* 작업 처리 객체의 체인(동작 체인 등)을 만들 때는 의무 체인 패턴을 사용한다.
* 한 객체가 어떤 작업을 처리한 다음에 다른 객체로 결과를 전달하고, 다른 객체도 해야 할 작업을 처리한 다음에 또 다른 객체로 전달하는 식이다.
```java
public abstract class ProcessingObject<T> {
    protected ProcessingObject<T> successor;
    public void setSuccessor(ProcessingObject<T> successor) {
        this.successor = successor;
    }
    public T handle(T input) {
        T r = handleWork(input);
        if (successor != null) {
            return successor.handle(r);
        }
        return r;
    }
    abstract protected T handleWork(T input);
}

public class HeaderTextProcessing extends ProcessingObject<String> {
    public String handleWork(String text) {
        return "From Raoul, Mario and Alan: " + text;
    }
}

public class SpellCheckerProcessing extends ProcessingObject<String> {
    public String handleWork(String text) {
        return text.replaceAll("labda", "lambda");
    }
}

// 람다
UnaryOperator<String> headerProcessing = (String, text) -> "From Raoul, Mario and Alan: " + text;
UnaryOperator<String> spellCheckerProcessing = (String, text) -> text.replaceAll("labda", "lambda");
Function<String, String> pipeline = headerProcessing.andThen(spellCheckerProcessing);   // 동작 체인으로 두 함수를 조합
String result = pipeline.apply("Aren ' t labdas really sexy?!!");
```

#### 팩토리(factory)

* 인스턴스화 로직을 클라이언트에 노출하지 않고 객체를 만들 때 팩토리 패턴을 사용한다.
* 생정자와 설정을 외부로 노출하지 않음으로써 클라이언트가 단순하게 상품을 생산할 수 있다.
```java
public class ProductFactory {
    public static Product createProduct(String name) {
        switch (name) {
          case "loan" : return new Loan();
          case "stock" : return new Stock();
          case "bond" : return new Bond();
          default: throw new RuntimeException("No such product " + name);
        }
    }
}

Product p = ProductFactory.createProduct("loan");

// 람다
final static Map<String, Supplier<Product>> map = new HashMap<>();
static {
    map.put("loan", Loan::new);
    map.put("stock", Stock::new);
    map.put("bond", Bond::new);
}

public static Product createProduct(String name) {
    Supplier<Product> p = map.get(name);
    if (p != null) {
        return p.get();
    }
    throw new IllegalArgumentException("No such product " + name);
}
```

## 람다 테스팅

### 보이는 람다 표현식의 동작 테스팅

* 람다는 익명(결국 익명 함수)이므로 테스트 코드 이름을 호출 할 수 없다.
* 따라서 필요하면 람다를 필드에 저장해서 재사용할 수 있으며 람다의 로직을 테스트 할 수 있다.
```java
public class Point {
    public final static Comparator<Point> compareByXAndThenY = 
            comparing(Point::getx).thenComparing(Point::getY);
}
@Test
public void testComparingTwoPoints() throws Exception {
    Point p1 = new Point(10, 15);
    Point p2 = new Point(10, 15);
    int result = Point.compareByXAndThenY.compare(p1, p2);
    assertTrue(result < 0);
}
```
* 람다 표현식은 함수형 인터페이스의 인스턴스를 생성한다는 사실을 기억하자.

### 람다를 사용하는 메서드의 동작에 집중하라

* 람다의 목표는 정해진 동작을 다른 메서드에서 사용할 수 있도록 하나의 조각으로 캡슐화하는 것이다. 그러려면 세부 구현을 포함하는 람다 표현식을 공개하지 말아야 한다.
* 람다 표현식을 사용하는 메서드의 동작을 테스트함으로써 람다를 공개하지 않으면서도 람다 표현식을 검증할 수 있다.
* 람다 표현식 자체를 테스트 하는 것보다는 람다 표현식이 사용되는 메서드 동작을 테스트하는 것이 바람직하다.

### 복잡한 람다를 개별 메서드로 분할하기

* 복잡한 람다를 테스트할 경우 해결책은 람다 표현식을 메서드 참조로 바꾸는 것이다. 그러면 일반 메서드를 테스트하듯이 람다 표현식을 테스트할 수 있다.

### 고차원 함수 테스팅

* 함수를 인수로 받거나 다른 함수를 반환하는 메서드(고차원 함수)는 좀 더 사용하기 어렵다.
* 메서드가 람다를 인수로 받는다면 다른 람다로 메서드의 동작을 테스트할 수 있다.
* 코드를 테스트하면서 람다 표현식에 어떤 문제가 있음을 발견하게 되는데 그래서 디버깅이 필요하다.

## 디버깅

* 문제가 발생한 코드를 디버깅할 때 두 가지를 먼저 확인해야 한다.
  * 스택 트레이스
  * 로깅
* 하지만 람다 표현식과 스트림은 기존의 디버깅 기법을 무력화한다.

### 스택 트레이스 확인

* 람다 표현식은 이름이 없기 때문에 조금 복잡한 스택 트레이스가 생성된다.
* 메서드 참조를 사용해도 스택 트레이스에는 메서드명이 나타나지 않는다.
* 메서드 참조를 사용하는 클래스와 같은 곳에 선언되어 있는 메서드를 참조할 때는 메서드 참조 이름이 스택 트레이스에 나타난다.
* 따라서 람다 표현식과 관련한 스택 트레이스는 이해하기 어려울 수 있다는 점을 염두에 두자.

### 정보 로깅

* 스트림의 파이프라인 연산을 디버깅 할 경우 `forEach`로 스트림 결과를 출력하거나 로깅할 수 있다.
* 하지만 `forEach`를 호출하는 순간 전체 스트림이 소비된다. 중간 연산 과정을 확인하고 싶다면 바로 `peek`이라는 스트림 연산을 활용할 수 있다.
* `peek`은 스트림의 각 요소를 소비한 것처럼 동작을 실행하지만 `forEach`처럼 실제로 스트림의 요소를 소비하지는 않는다.
* `peak`은 자신이 확인한 요소를 파이프라인의 다음 연산으로 그대로 전달한다.
```java
numbers.stream()
        .peek(x -> System.out.pringln("from stream: " + x)) // 처음 소비한 요소 출력
        .map(x -> x + 17)
        .peek(x -> System.out.pringln("after map: " + x)) // map 동작 실행 결과 출력
        .filter(x -> x % 2 == 0)
        .peek(x -> System.out.pringln("after filter: " + x))
        .limit(3)
        .peek(x -> System.out.pringln("after limit: " + x))  // limit 동작 후 선택된 숫자를 출력
        .collect(toList());
```
