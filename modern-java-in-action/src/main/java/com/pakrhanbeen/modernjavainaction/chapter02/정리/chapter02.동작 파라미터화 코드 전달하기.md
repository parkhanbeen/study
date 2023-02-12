# 동작 파라미터화 코드 전달하기

## 동작 파라미터화

* 동작 파리미터화란 아직은 어떻게 실행할 것인지 결정하지 않은 코드 블록을 의미한다.
* 이 코드 블록의 실행은 나중으로 미뤄진다.
* 동작 파리미터화를 추가하려면 쓸데없는 코드가 늘어난다.
* 자바 8은 람다 표현식으로 이 문제를 해결한다.

### 선택 조건을 결정하는 인터페이스 (프레디케이트)

* 참 또는 거짓을 반환하는 함수를 **프레디케이트**라고 한다.

```java
public interface ApplePredicate {
    boolean test (Apple apple);
}
```

```java
// 무거운 사과만 선택
public class AppleHeavyWeightPredicate implements ApplePredicate {
    public boolean test(Apple apple) {
        return apple.getWeight() > 150;
    }
}

// 녹색 사과만 선택
public class AppleGreenColorPredicate implements ApplePredicate {
    public boolean test(Apple apple) {
        return GREEN.equals(apple.getColor());
    }
}
```

* `ApplePredicate`는 사과 선택 전략을 캡슐화한다.
* 이와 같은 패턴을 전략 디자인 패턴이라고 부른다.


### 추상적 조건으로 필터링

```java
public static List<Apple> filterApples(List<Apple> inventory, ApplePredicate p) {
    List<Apple> result = new ArrayList<>();
    
    for (Apple apple : inventory) {
        if(p.test(apple)) {         // 프레디케이트 객체로 사과 검사 조건을 캡슐화 했다.
            result.add(apple);
        }       
    }
    return result;
}
```

* 위 예제는 `ApplePredicate`를 이용한 필터 메서드다.
* `ApplePredicate`를 만들어 `filterApples` 메서드로 전달하여 유연성을 높였다.
* 만약 빨간 사과를 검색해달라는 요구사항이 추가되면 `ApplePredicate`를 적절하게 구현하면 된다.

```java
public class AppleRedAndHeavyPredicate implements ApplePredicate {
    public test(Apple apple) {
        return RED.equals(apple.getColor()) && apple.getWeight() > 150;
    }
}

public static void main(String[] args) {
    List<apple> redAndHeavyApples = filterApples(inventory, new AppleRedAndHeavyPredicate());
}
```

* `filterApples` 메서드의 동작을 파라미터화 한다.

* 하지만 여러 클래스를 구현해서 인스턴스화하는 과정이 거추장스럽게 느껴진다.

### 익명 클래스

* 자바는 클래스의 선언과 인스턴스화를 동시에 수행할 수 있도록 **익명 클래스(anonymous class)** 라는 깁버을 제공한다.
* **익명 클래스**는 자바의 지역 클래스(local class)와 비슷한 개념이다.

```java
// 메서드 동작을 직접 파라미터화
List<Apple> redApples = filterApples(inventory, new ApplePrecate() {
   public boolean test(Apple apple) {
       return RED.equals(apple.getColor());
    } 
});
```

* 익명 클래스 역시 부족한 점이 있다.
  * 여전히 많은 공간을 차지함
  * 많은 프로그래머가 익명 클래스의 사용에 익숙하지 않고 코드가 장황함

### 람다 표현식

```java
List<Apple> result = filterApples(inventory, (Apple apple) -> RED.equals(apple.getColor()));
```

* 이전 코드를 람다로 표현하여 간단하게 재구현할 수 있다.
* 람다 표현식을 사용하면 유연성과 간결함이라는 두 마리 토끼를 잡을 수 있다.

```java
public interface Predicate<T> {
    boolean test(T t);
}

public static <T> List<T> filter(List<T> list, Predicate<T> p) {
    List<T> result = new ArrayList<>();
    for (T e : list) {
        if (p.test(e)) {
            result.add(e);
        }
    }
    return result;
}

List<Apple> redApples = filter(inventory, (Apple apple) -> RED.equals(apple.getColor()));

List<Integer> evenNumbers = filter(numbers, (Integer i) -> i % 2 == 0);
```

### Comparator

* 자바 8의 `List`에는 `sort` 메서드가 포함되어 있다(`Collections.sort`도 존재).
* `java.util.Comparator` 객체를 이용해 `sort`의 동작을 파라미터화 할 수 있다.

```java
// java.util.Comparator
public interface Comparator<T> {
    int compare(T o1, T o2);
}
```

* `Comparator`를 구현해서 `sort` 메서드의 동작을 다양화 할 수 있다.

```java
// 무게가 적은 순서로 사과를 정렬 - 익명 클래스.
inventory.sort(new Comparator<Apple>() {
    public int compare(Apple a1, Apple a2) {
        return a1.getWeight().compareTo(a2.getWeight());
    }
});

// 무게가 적은 순서로 사과를 정렬 - 람다 표현식
inventory.sort((Apple a1, Apple a2) -> a1.getWeight().compareTo(a2.getWeight()));
```


### Runnable

* 자바 스레드를 이용하면 병렬로 코드 블록을 실행할 수 있다.
* 자바 8까지는 `Thread` 생성자에 객체만을 전달할 수 있었으므로 보통 결과를 반환하지 않는 `void run` 메서드를 포함하는 익명 클래스가
 `Runnable` 인터페이스를 구현하도록 하는 것이 일반적인 방법이었다.

```java
// java.lang.Runnable
public interface Runnable {
    void run();
}

Thread t = new Thread(new Runnable() {
    public void run() {
      System.out.println("Hello world");
    }
});

// 람다 표현식
Thread t = new Thread(() -> System.out.println("Hello world"));
```

### Callable

* 자바 5부터 지원하는 `ExecutorService` 인터페이스는 태스크 제출과 실행 과정의 연관성을 끊어준다.
* `ExecutorService`를 이용하면 태스크를 스레드 풀로 보내고 결과를 `Future`로 저장할 수 있다는 점이 `Runnable`과 다르다.
  * `Callable` 인터페이스를 이용해 결과를 반환하는 태스크를 만든다.

```java
// java.util.concurrent.Callable
public interface Callable<V> {
  V call();
}

// 태스크를 실행하는 스레드의 이름을 반환
ExecutorService executorService = Executor.newCachedThreadPool();
Future<String> threadName = executorService.submit(new Callable<String>() {
    @Override
    public String call() throws Exception {
        return Thread.currentThread().getName();
    }
});

// 람다 표현식
Future<String> threadName = executorService.submit(() -> Thread.currentThread().getName());
```

## 정리

* 동작 파라미터화를 이용하여 요구사항에 더 잘 대응할 수 있는 코드를 구현할 수 있다.
* 자바 8이전에는 지저분하게 구현 했던 코드를 자바 8에서는 인터페이스를 상속받아 여러 클래스를 구현해야 하는 수고를 없앨 수 있는 방법을 제공한다.
