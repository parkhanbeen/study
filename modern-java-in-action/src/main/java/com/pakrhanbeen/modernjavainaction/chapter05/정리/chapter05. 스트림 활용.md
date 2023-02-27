# 스트림 활용

## 필터링

* `filter` 메서드는 **프레디케이트**를 인수로 받아서 프레디케이트와 일치하는 모든 요소를 포함하는 스트림을 반환한다.

```java
List<Dish> vegetarianMenu = menu.stream()
    .filter(Dish::isVegetarian)
    .collect(toList());
```

* 스트림은 고유 요소로 이루어진 스트림을 반환하는 `distinct` 메서드도 지원한다.
  * 고유 여부는 스트림에서 만든 객체의 `hashCode, equals`로 결정

```java
List<Integer> numbers = Arrays.asList(1, 2, 1, 3, 3, 2, 4);
numbers.stream()
    .filter(i -> i % 2 == 0)
    .distinct()
    .forEach(System.out::println);
```

## 스트림 슬라이싱

### 프레디케이트를 이용한 방법

* **takeWhile** 
  * 리스트가 정렬되어 있을 경우 특정 값보다 크거나 같을 때 반복 작업을 중단하고 싶을 경우 사용한다.
    ```java
    List<Dish> slicedMenu1 = specialMenu.stream()
                                .takeWhile(dish -> dish.getCalories() < 320)
                                .collect(toList());
    ```
  * `takeWhile`을 이용하면 무한 스트림을 포함한 모든 스트림에 프레디케이트를 적용해 스트림을 슬라이스할 수 있다.

* **dropWhile**
  * 프로디케이트가 처음으로 거짓이 되는 지점까지 발견된 요소를 버린다.
    ```java
    List<Dish> slicedMenu1 = specialMenu.stream()
                                .dropWhile(dish -> dish.getCalories() < 320)
                                .collect(toList());
    ```
  * `dropWhile`은 `takeWhile`과 정반대의 작업을 수행한다.
  * 프레디케이트가 거짓이 되면 그 지점에서 작업을 중단하고 남은 모든 요소를 반환한다.

### 스트림 축소

* 스트림은 주어진 값 이하의 크기를 갖는 새로운 스트림을 반환하는 `limit(n)` 메서드를 지원한다.

    ```java
    List<Dish> slicedMenu1 = specialMenu.stream()
                                    .filter(dish -> dish.getCalories() > 300)
                                    .limit(3)
                                    .collect(toList());
    ```

* 프레디케이트와 일치하는 처음 세 요소를 선택한 다음에 즉시 결과를 반환한다.

### 요소 건너뛰기

* 처음 n개 요소를 제외한 스트림을 반환하는 `skip(n)` 메서드를 지원한다.

    ```java
    List<Dish> dishes = menu.stream()
                            .filter(d -> d.getCalories() > 300)
                            .collect(toList());
    ```

## 매핑

* 스트림 API의 `map, flatMap` 메서드는 특정 데이터를 선택하는 기능을 제공한다.

### 스트림의 각 요소에 함수 적용하기

#### map

* `map` 메서드는 인수로 제공된 함수 각 요소에 적용되며 함수를 적용한 결과가 새로운 요소로 매핑된다.
  * 이 과정은 기존 값을 고친다는 개념보다 '새로운 버전을 만든다'라는 개념에 가까우므로 **변환**에 가까운 **매핑**이라는 단어를 사용한다.
  
```java
List<String> dishNames = menu.stream()
                            .map(Dish::getName)
                            .collect(toList());
```

### 스트림 평면화

* 문자열 리스트에서 **고유 문자**로 이루어진 리스트를 반환해보자.
  * `["Hello", "World"]` -> `["H", "e", "l", "o", "W", "r", "d"]` 로 변환

      ```java
      word.stream()
          .map(word -> word.split(""))
          .distinct()
          .collect(toList());
      
      ```
  * 위 코드는 문자열 배열을 반환하는 문제가 있다. `map` 메서드가 반환하는 스트림 형식은 `Stream<String[]>` 이다.
    * 우리가 원하는 것은 `Stream<String>`이다.
  * 이러한 문제는 `flatMap`이라는 메서드를 이용해 해결할 수 있다.

  ```java
  // Arrays.stream()을 이용한 시도
  word.stream()
      .map(word -> word.split(""))
      .map(Arrays::stream)
      .distinct()
      .collect(toList());
  // 실패 : List<Stream<String>이 만들어짐
  
  // flatMap 이용
  List<String> uniqueCharacters = words.stream()
                                       .map(word -> word.split(""))  // 각 단어를 개별 문자를 포함하는 배열로 변환
                                       .flatMap(Arrays::stream)      // 생성된 스트림을 하나의 스트림으로 평면화
                                       .distinct()
                                       .collect(toList()); 
  ```

#### flatMap

* `flatMap`은 각 배열을 스트림이 아니라 스트림의 콘텐츠로 매핑한다.
  * 즉, `map(Arrays::stream)`과 달리 `flatMap`은 하나의 평면화된 스트림을 반환한다.
  * `flatMap`메서드는 스트림의 각 값을 다른 스트림으로 만든 다음에 모든 스트림을 하나의 스트림으로 연결하는 기능을 수행한다.

## 검색과 매칭

* 스트림 API는 `allMatch, anyMatch, noneMatch, findFirst, findAny` 등 다양한 유틸리티 메서드를 제공한다.

### anyMatch

* 프레디케이트가 주어진 스트림에서 적어도 한 요소와 일치하는지 확인할 때 이용한다.

```java
if (menu.stream().anyMatch(Dish::isVegetarian)) {
        ...
}
```

### allMatch

* 스트림의 모든 요소가 주어진 프레디케이트와 일치하는지 검사한다.
```java
// 모든 요리가 1000칼로리 이하면 건강식으로 간주
boolean isHealthy = menu.stream().allMatch(dish -> dish.getCalories() < 1000);
```

### noneMatch

* `allMatch`와 반대 연산을 수행한다.
* 즉, 주어진 프레디케이트와 일치하는 요소가 없는지 확인한다.
```java
boolean isHealthy = menu.stream()
        .noneMatch(d -> d.getCalories() >= 1000);
```

<br>

`anyMath, allMatch, noneMatch` 세 메서드는 스트림 **쇼트서킷 기법**, 즉 자바의 `&&, ||`와 같은 연산을 활용한다.

### findAny

* 현재 스트림에서 임의의 요소를 반환한다.
* `findAny` 메서드는 다른 스트림연산과 연결해서 사용할 수 있다.

```java
Optional<Dish> dish = menu.stream()
        .filter(Dish::isVegetarian)
        .findAny();
```

* 쇼트서킷을 이용해서 결과를 찾는 즉시 실행을 종료한다.

> 여기서 `optional` 이란 <br>
> 자바 8 부터 추가된 클래스로 값이 없는 `null`을 대신해서 사용할 수 있는 컨테이너이다. <br>
> 값이 없을 때 처리하는 기능을 제공한다.
> <br>
> * `isPresent()` : 값을 포함하면 참 포함하지 않으면 거짓을 반환
> * `ifPresent(Consumer<T> block)` : 값이 있으면 주어진 블록을 실행
> * `T get()` : 값이 존재하면 값을 반환, 없으면 `NoSuchElementException`을 던진다.
> * `T orElse(T other)` : 값이 있으면 값을 반환, 없으면 기본값을 반환한다.
> 
> ```java
> menu.stream()
> .filter(Dish::isVegetarian)
> .findAny()
> .ifPresent(dish -> System.out.println(dish.getName()));   // 값이 있으면 출력 없으면 아무일도 일어나지 않는다.
> ```

### findFirst

* 스트림에서 첫 번째 요소를 찾을 경우 사용된다.
```java
List<Integer> someNumbers = Arrays.asList(1, 2, 3, 4, 5);
Optional<Integer> firstSquareDivisibleByThree = someNumbers.stream()
        .map(n -> n * n)
        .filter(n -> n % 3 == 0)
        .findFirst();   // 9;
```

### findAny와 findFirst는 언제 사용할까?

* 병렬 실행에서는 첫 번째 요소를 찾기 어렵다.
* 따라서 요소의 반환 순서가 상관없다면 병렬 스트림에서는 제약이 적은 `findAny`를 사용한다.
* 반환 순서가 상관 있다면 `findFirst`를 사용한다.

## 리듀싱

* 모든 스트림 요소를 처리해서 값으로 도출하는 것을 **리듀싱 연산**이라고 한다.

```java
// 일반 for-each 문
int sum = 0;
for  (int x : numbers) {
    sum += x;
}

// reduce 사용
int sum = numbers.stream().reduce(0, (a, b) -> a + b);

// 메서드 참조
int sum = numbers.stream().reduce(0, Integer::sum);
```

* 위 코드 `reduce` 연산은 두 개의 인수를 갖는다.
  * `초기값 0`
  * 두 요소를 조합해서 새로운 값을 만드는 `BinaryOperator<T>`
    * `(a, b) -> a + b`
* 스트림이 하나의 값으로 줄어들 때까지 람다는 각 요소를 반복해서 조합한다.

```java
Optional<Integer> sum = numbers.stream().reduce((a, b) -> (a + b));
```

* 위 코드는 초기값을 받지 않도록 오버로드된 `reduce`이다.
* 값이 없음을 가리킬수 있도록 `Optional` 객체로 감싼 결과를 반환한다.

### 최댓값과 최솟값 reduce

* `reduce`는 두 인수를 받는다.
  * 초깃값
  * 스트림의 두 요소를 합쳐서 하나의 값으로 만드는 데 사용할 람다

```java
// 최댓값
Optional<Integer> max = numbers.stream().reduce(Integer::max);
// 최솟값
Optional<Integer> max = numbers.stream().reduce(Integer::min);
```

### reduce 메서드의 장점과 병렬화

* `reduce`를 이용하면 내부 반복이 추상화되면서 내부 구현에서 병렬로 `reduce`를 실행할 수 있게 된다.

```java
int sum = numbers.stream().reduce(0, Integer::sum);
```

### 스트림 연산 : 상태 없음과 상태 있음

* `stream` 메서드를 `parallelStream`로 바꾸는 것만으로도 별다른 노력 없이 병렬성을 얻을 수 있다.
* `map, filter` 등은 입력 스트림에서 각 요소를 받아 0 또는 결과를 출력 스트림으로 보낸다. 즉 내부 상태를 갖지 않는 연산이다.

* 하지만 `reduce, sum, max` 같은 연산은 결과를 누적할 내부 상태가 필요하다.
* 스트림에서 처리하는 요소 수와 관계없이 내부 상태의 크기는 **한정**되어 있다.


* 반면 `sorted, distinct` 같은 연산은 `filter, map`처럼 스트림을 입력으로 받아 다른 스트림을 출력하는 것처럼 보일 수 있다.
* 하지만 스트림의 요소를 정렬하거나 중복을 제거하려면 과거의 이력을 알고 있어야 한다.
  * 예를 들어 어떤 요소를 출력 스트림으로 추가하려면 **모든 요소가 버퍼에 추가되어 있어야 한다.**
* 따라서 데이터 스트림의 크기가 크거나 무한이라면 문제가 생길 수 있다. 이러한 연산을 **내부 상태를 갖는 연산**이라 한다.

## 숫자형 스트림

```java
int calories = menu.stream()
        .map(Dish::getCalories)
        .reduce(0, Integer::sum);
```

* 위 코드에는 박싱 비용이 숨어있다. 내부적으로 합계를 계산하기 전에 `Integer`를 기본형을 언박싱해야 한다.
* 스트림 API 숫자 스트림을 효율적으로 처리할 수 있도록 **기본형 특화 스트림**을 제공한다.

### 기본형 특화 스트림

* 자바 8에서는 세 가지 기본형 특화 스트림을 제공한다. (박싱 비용을 피할 수 있다)
  * `IntStream, DoubleStream, LongStream`
* 각각의 인터페이스는 숫자 스트림의 합계를 계산하는 `sum`, 최댓값 요소를 검색하는 `max` 같이 자주 사용하는 숫자 관련 리듀싱 연산
 수행 메서드를 제공한다.
* 또한 필요할 때 다시 객체 스트림으로 복원하는 기능도 제공한다.
* 특화 스트림은 오직 박싱 과정에서 일어나는 효율성과 관련 있으며 스트림에 추가 기능을 제공하지는 않는다는 사실을 기억하자.

#### 숫자 스트림으로 매핑

* 스트림을 특화 스트림으로 변환할 때는 `mapToInt, mapToDouble, mapToLong` 세 가지 메서드를 가장 많이 사용한다.

```java
int calories = menu.stream()
                  .mapToInt(Dish::getCalories)   // IntStream 변환
                  .sum();
```

* 스트림이 비어있으면 `sum`은 기본값 0을 반환한다.
* `IntStream`은 `max, min, average`등 다양한 유틸리티 메서드도 지원한다.

#### 객체 스트림으로 복원

```java
IntStream intStream = menu.stream().mapToInt(Dish::getCalories);  // 숫자 스트림 변환
Stream<Integer> stream = inStream.boxed();  // 숫자 스트림을 스트림으로 변환
```

#### 기본값 : OptionalInt

* 합계에서는 0이라는 기본값이 있었으므로 별 문제가 없지만 최댓값을 찾을 때는 0이라는 기본값 때문에 잘못된 결과가 도출될 수 있다.
* 스트림에 요소가 없는 상황과 실제 최댓값이 0인 상황을 어떻게 구별할 수 있을끼?
* `Optional`을 `Integer, String`등의 참조 형식으로 파라미터화할 수 있다.
  * 또한 `OptionalInt, OptionalDouble, OptionalLong` 세 가지 기본형 특화 스트림 버전도 제공한다.

```java
OptionalInt maxCalories = menu.stream()
                        .mapToInt(Dish::getCalories)
                        .max();
int max = maxCalories.orElse(1);   // 기본 최댓값을 명시적으로 설정
```

#### 숫자 범위

* 특정 범위의 숫자를 이용해야 하는 상황이 자주 발생한다.
  * 1에서 100 사이의 숫자를 생성하려 한다고 가정
  * 자바 8의 `IntStream, LongStream`에서는 `range, rangeClosed`라는 두 가지 정적 메서드를 제공
  * 두 메서드 모두 첫 번째 인수로 시작값, 두 번째 인수로 종료값을 갖는다.
  * `range` 메서드는 시작값과 종료값이 결과에 포함되지 않는 반면 `rangeClosed`는 시작값과 종료값이 결과에 포함된다는 점이 다르다.

```java
IntStream evenNumbers = IntStream.rangeClosed(1, 100)   // 1, 100 의 범위를 나타낸다.
        .filter(n -> n % 2 == 0);                       // 1 ~ 100까지의 짝수 스트림
System.out.printLn(evenNumbers.count());          
```

* 이 때 `IntStream.range(1, 100)`을 사용하면 1과 100을 포함하지 않으므로 짝수 49개를 반환한다.

## 스트림 만들기

### 값으로 스트림 만들기

* `Stream.of`를 이용해서 스트림을 만들 수 있다.
```java
Stream<String> stream = Stream.of("Modern", "Java", "In ", "Action");
stream.map(String::toupperCase).forEach(System.out::println);

// empty 메서드를 이용해 스트림을 비울 수 있다.
Stream<String> emptyStream = Stream.empty();
```

### null이 될 수 있는 객체로 스트림 만들기

* 자바 9에서는 `null`이 될 수 있는 객체를 스트림으로 만들 수 있는 새로운 메서드가 추가되었다.
```java
Stream<String> homeValueStream = Stream.ofNullable(System.getProperty("home"));

Stream.of("config", "home", "user")
        .flatMap(key -> Stream.ofNullable(System.getProperty(key)));
```

### 배열로 스트림 만들기
* 배열을 인수로 받는 정적 메서드 `Arrays.stream`을 이용해서 스트림을 만들 수 있다.
```java
int[] numbers = {2, 3, 5, 7, 11, 13};
int sum = Arrays.stream(numbers).sum();
```

### 파일로 스트림 만들기

* 파일을 처리하는 등의 I/O 연산에 사용하는 자바의 NIO API(비블록 I/O)도 스트림 API를 활용할 수 있도록 업데이트되었다.
* `java.nio.file.Files`의 많은 정적 메서드가 스트림을 반환한다.
* 예를 ㄷ르어 `Files.lines`는 주어진 파일의 행 스트림을 문자열로 반환한다.
```java
long uniqueWords = 0;
try (Stream<String> lines = Files.lines(Paths.get("data.txt"), Charset.defaultCharset())) {
    uniqueWords = lines.flatMap(line -> Arrays.stream(line.split(" ")))  // 고유 단어 수 계산
        .distinct()     // 중복 제거
        .count();       // 단어 스트림 생성
} catch(IOException e) { 
}
```

### 함수로 무한 스트림 만들기

* 스트림 API는 함수에서 스트림을 만들 수 있는 두 정적 메서드 `Stream.iterate, Stream.generate`를 제공한다.
* 두 연산을 이용해서 **무한 스트림**, 즉 고정된 컬렉션에서 고정된 크기로 스트림을 만들었던 것과는 달리 크기가 고정되지 않은 스트림을 만들 수 있다.

#### iterate

```java
Stream.iterate(0, n -> n + 2)
        .limit(10)
        .forEach(System.out::println);
```

#### generate

* `iterate`와 비슷하게 `generate`도 요구할 때 값을 계산하는 무한 스트림을 만들 수 있다.
  * 하지만 `iterate`와 달리 `generate`는 생산된 각 값을 연속적으로 계산하지 않는다.
```java
Stream.generate(Math::random)
        .limit(5)
        .forEach(System.out::println)
```
