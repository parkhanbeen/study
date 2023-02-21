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

    ```java
    word.stream()
        .map(word -> word.split(""))
        .distinct()
        .collect(toList());
    ```

* 