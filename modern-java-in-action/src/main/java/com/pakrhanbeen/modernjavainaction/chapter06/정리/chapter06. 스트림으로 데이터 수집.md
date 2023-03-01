# 스트림으로 데이터 수집

## 컬렉터

* `Collector`인터페이스 구현은 스트림의 요소를 어떤 식으로 도출할지 지정한다.
  * `toList`를 이용해 각 요소를 리스트로 만들어라. 
    * `collect(Collectors.toList());`
  * `groupingBy`를 이용해 각 키 버킷 그리고 각 키 버킷에 대응하는 요소 리스트를 값으로 포함하는 맵을 만들어라.
    * `collect(groupingBy(Transaction::getCurrency));`
* 훌륭하게 설계된 함수형 API의 또 다른 장점으로 높은 수준의 조합성과 재사용성을 꼽을 수 있다.
* `collect`로 결과를 수집하는 과정을 간단하면서도 유연한 방식으로 정의할 수 있다는 점이 컬렉터의 최대 강점이다.
* 스트림에 `collect`를 호출하면 스트림의 요소에 (컬렉터로 파라미터화된) 리듀싱 연산이 수행된다.
* `Collector`인터페이스의 메서드를 어덯게 구현하느냐에 따라 스트림에 어떤 리듀싱 연산을 수행할지 결정된다.
* `Collectors`유틸리티 클래스는 자주 사용하는 컬렉터 인스턴스를 손쉽게 생성할 수 있는 정적 팩토리 메서드를 제공한다.
* `Collectors`에서 제공하는 메서드의 기능은 크게 세 가지로 구분할 수 있다.
  1. 스트림 요소를 하나의 값으로 리듀스하고 요약
  2. 요소 그룹화
  3. 요소 분할

## 리듀싱과 요약

* **counting** : 개수를 계산한다.
  ```java
  menu.stream().collect(Collectors.counting());
  // 불필요한 과정 생략
  menu.stream().count();
  ```
  
* **maxBy, minBy** : 최댓값, 최솟값을 계산한다. 
  ```java
  // 스트림 요소를 비교하는데 사용할 Comparator를 생성
  Comparator<Dish> dishCaloriesComparator = Comparator.comparingInt(Dish::getCalories);
  Optional<Dish> mostCalorieDish = menu.stream().collect(maxBy(dishCaloriesComparator));
  ```
  
* **summingInt** : 인수로 전달된 함수 객체를 `int`로 매핑한 컬렉터를 반환한다. 
 그리고 `summingInt`가 `collect` 메서드로 전달되면 요약 작업을 수행한다.
  ```java
  // 메뉴 리스트의 총 칼로리를 계산
  int totalCalories = menu.stream().collect(summingInt(Dish::getCalories));
  ```
  * 그 외 `Collectors.summingLong, Collectors.summingDouble` 메서드는 같은 방식으로 동작하며 각각 `long, double` 
    형식의 데이터로 요약한다는 점만 다르다. <br><br>

* **averagingInt** : 평균값을 계산한다. 
  ```java
  menu.stream().collect(averagingInt(Dish::getCalories));
  ```
  * 그 외 `averagingLong, averagingDouble` 등 다양한 형식으로 평균을 계산할 수 있다. <br><br>

* **summarizingInt** : 요소 수, 최댓값과 최솟값, 합계와 평균 등 두 개 이상의 연산을 한 번에 수행해야 할 경우 사용
  ```java
  // IntSummmaryStatistics 클래스로 요소 수, 최댓값과 최솟값, 합계와 평균 정보가 수집
  IntSummmaryStatistics menuStatistics = menu.stream().collect(summarizingInt(Dish::getCalories));
  
  // 출력
  IntSummmaryStatistics{count=9, sum=4300, min=120, average=477.77778, max=800}
  ```
  * 그 외 `summarizingLong, summarizingDouble` 메서드와 관련된 `LongSummmaryStatistics, DoubleSummmaryStatistics`
   클래스도 있다. <br><br>

* **joining** : 스트림의 각 개체에 `toString` 메서드를 호출해서 추출한 모든 문자열을 하나의 문자열로 연결해서 반환한다.
  ```java
  String shortMenu = menu.stream().map(Dish::getName).collect(joining());
  ```
  * `joining` 내부적으로 `StringBuilder`를 이용해서 문자열을 하나로 만든다.
  * 결과 문자열을 해석하기 힘들다면 두 요소 사이에 구분 문자열을 넣을 수 있도록 오버로드된 `joining` 팩토리 메서드가 있다.
  ```java
  String shortMenu = menu.stream().map(Dish::getName).collect(joining(", "));
  ```
  
### 범용 리듀싱 요약 연산

* 지금까지 살펴본 모든 컬렉터는 `reducing` 팩터리 메서드로 정의할 수 있다.
  * 즉, 범용 `Collectors.reducing`으로도 구현할 수 있다.
  * 그럼에도 범용 팩터리 메서드 대신 특화된 컬렉터를 사용하는 이유는 편의성 때문이다.

```java
int totalCalories = menu.stream().collect(reducing(0, Dish::getCalories, (i, j) -> i + j));
```

* `reducing`은 세 개의 인수를 받는다.
  * 첫 번째 인수는 리듀싱 연산의 시작값이거나 스트림에 인수가 없을 경우 반환값이다.
  * 두 번째 인수는 변환 함수다.
  * 세 번째 인수는 같은 종류의 두 항목을 하나의 값으로 더하는 `BinaryOperator`이다.

```java
// 한 개의 인수를 가진 reducing 버전
Optional<Dish> mostCaloriesDish = menu.stream()
        .collect(reducing(d1, d2) -> d1.getCalories() > d2.getCalories() ? d1 : d2));
```

## 그룹화

* 자바 8의 함수형을 이용하면 가독성 있는 한 줄의 코드로 그룹화를 구현할 수 있다.
```java
Map<Dish.Type, List<Dish>>> dishesByType = menu.stream().collect(groupingBy(Dish::getType));
```

* 함수를 기준으로 스트림이 그룹화되므로 이를 **분류 함수**라고 부른다.

### 다수준 그룹화

* 두 인수를 받는 팩토리 메서드 `Collectors.groupingBy`를 이용해서 항목을 다수준으로 그룹화할 수 있다.
* `Collectors.groupingBy`는 일반적으로 분류 함수와 컬렉터를 인수로 받는다.
  * 즉, 바깥쪽 `groupingBy`메서드에 스트림의 항목을 분류할 두 번째 기준을 정의하는 내부 `groupingBy`를 전달해서 두 수준으로 스트림의 항목을
   그룹화할 수 있다.

```java
Map<Dish.Type, Map<CaloricLevel, List<Dish>>> dishesByTypeCaloricLevel = 
menu.stream().collect(
    groupingBy(Dish::getType, 
        groupingBy(dish -> {
            if (dish.getCalories() <= 400) {
                return CaloricLevel.DIEF;
             } else if (dish.getCalories() <= 700) {
                return CaloricLevel.NORMAL;
             } else {
                return CaloricLevel.FAT;
             }   
        }))
)

// 결과
{MEAT={DIET=[chicken], NORMAL=[beef], FAT=[pork]}, 
 FISH={DIEF=[prawns], NORMAL=[salmon]},
 OTHER={DIET=[rice, seasonal fruit], NORMAL=[french fries, pizza]}}
```

### 서브그룹으로 데이터 수집

```java
Map<Dish.Type, Long> typesCount = menu.stream()
        .collect(groupingBy(Dish::getType, counting()));

// 결과
{MEAT=3, FISH=2, OTHER=4}
```

* 분류 함수 한 개의 인수를 갖는 `groupingBy(f)`는 사실 `groupingBy(f, toList())`의 축약형이다.

```java
menu.stream().collect(groupingBy(Dish::getType,        // 분류 함수
        collectingAndThen(
            maxBy(comparingInt(Dish::getCalories)),   // 감싸인 컬렉터
        Optional::get)));  // 변환 함수
```

* `groupingBy`는 가장 바깥쪽에 위치하면서 요리의 종류에 따라 메뉴 스트림을 세 개의 서브스트림으로 그룹화한다.
* `groupingBy` 컬렉터는 `collectingAndThen` 컬렉터를 감싼다. 따라서 두 번째 컬렉터는 그룹화된 세 개의 서브스트림에 적용된다.
* `collectingAndThen` 컬렉터는 세 번째 컬렉터 `maxBy`를 감싼다.
* 리듀싱 컬렉터가 서브스트림에 연산을 수행한 결과에 `collectingAndThen`의 `Optional::get` 변환 함수가 적용된다.
* `groupingBy` 컬렉터가 반환하는 맵의 분류 키에 대응하는 세 값이 각각의 요리 형식에서 가장 높은 칼로리다.


* 팩터리 메서드 `collectingAndThen`으로 컬렉터가 반환한 결과를 다른 형식으로 활용할 수 있다.
* 팩터리 메서드 `collectingAndThen`은 적용할 컬렉터와 변환 함수를 인수로 받아 다른 컬렉터를 반환한다.
* 반환되는 컬렉터는 기존 컬렉터의 래퍼 역할을 하며 `collect`의 마지막 과정에서 변환 함수로 자신이 반환하는 값을 매핑한다.


## 분할

* 분할은 **분할 함수**라 불리는 프레디케이트를 분류 함수로 사용하는 특수한 그룹화 기능이다.
* 분할 함수는 불리언을 반환하므로 맵의 키 형식 `Boolean`이다. 결과적으로 그룹화 맵은 최대(참 아니면 거짓의 값을 갖는) 두 개의 그룹으로 분류된다.
```java
Map<Boolean, List<Dish>> partitionedMenu = menu.stream()
        .collect(partitioningBy(Dish::isVegetarian));
// 채식 요리
List<Dish> vegetarianDishes = partitionedMenu.get(true);
```

### 분할의 장점

* 분할 함수가 반환하는 참, 거짓 두 가지 요소의 스트림 리스트를 모두 유지한다는 것이 분할의 장점이다.

## Collector 인터페이스

* `Collector` 인터페이스는 리듀싱 연산(즉, 컬렉터)을 어떻게 구현할지 제공하는 메서드 집합으로 구성된다.
```java
// Collector 인터페이스
public interface Collector<T, A, R> {
    Supplier<A> supplier();
    BiConsumer<A, T> accumulator();
    BinaryOperator<A> combiner();
    Function<A, R> finisher();
    Set<Characteristics> characteristics();
}
```

* T는 수집될 스트림 항목의 제네릭 형식이다.
* A는 누적자, 즉 수집 과정에서 중간 결과를 누적하는 객체의 형식이다.
* R은 수집 연산 결과 객체의 형식(항상 그런 것은 아니지만 대개 컬렉션 형식)이다.

### supplier 메서드 : 새로운 결과 컨테이너 만들기

* `supplier` 메서드는 빈 결과로 이루어진 `supplier`를 반환해야 한다. 즉, `supplier`는 수집 과정에서 빈 누적자 인스턴스를 만드는 파라미터가 없는 함수다.
```java
public Supplier<List<T>> ssupplier() {
    return () -> new ArrayList<T>();
//    return ArrayList::new;   // 생성자 참조 전달 방법
}
```

### accumulator 메서드 : 결과 컨테이너에 요소 추가하기

* `accumulator` 메서드는 리듀싱 연산을 수행하는 함수를 반환한다.
* 스트림에서 n번째 요소를 탐색할 때 두 인수, 즉 누적자(스트림의 첫 n-1개 항목을 수집한 상태) 와 n번째 요소를 함수에 적용한다.
* 함수 반환값은 `void`, 즉 요소를 탐색하면서 적용하는 함수에 의해 누적자 내부상태가 바뀌므로 누적자가 어떤 값일지 단정할 수 없다.
```java
public BiConsumer<List<T>, T> accumulator() {
    return (list, item) -> list.add(item);
//    return List::add;       // 메서드 참조 방법
}
```

### finisher 메서드 : 최종 변환값을 결과 컨테이너로 적용하기

* `finisher` 메서드는 스트림 탐색을 끝내고 누적자 객체를 최종 결과로 변환하면서 누적 과정을 끝낼 때 호출할 함수를 반환해야 한다.
```java
public Function<List<T>, List<T>> finisher() {
    return Function.identity();
}
```

### combiner 메서드 : 두 결과 컨테이너 병합

* `combiner`는 스트림의 서로 다른 서브파트를 병렬로 처리할 때 누적자가 이 결과를 어떻게 처리할지 정의한다.
```java
public BinaryOperator<List<T>> combiner() {
    return (list1, list2) -> {
        list1.addAll(list2);
        return list1;
    }
}
```

### characteristics 메서드

* `characteristics` 메서드는 컬렉터의 연산을 정의하는 `characteristics` 형식의 불변 집합을 반환한다.
* `characteristics`는 스트림을 병렬로 리듀스할 것인지 그리고 병렬로 리듀스한다면 어떤 최적화를 선택해야 할지 힌트를 제공한다.
* `characteristics`는 다음 세 항목을 포함하는 열거형이다.

  * **UNORDERED** 
    * 리듀싱 결과는 스트림 요소의 방문 순서나 누적 순서에 영향을 받지 않는다.
  * **CONCURRENT**
    * 다중 스레드에서 `accumulator` 함수를 동시에 호출할 수 있으며 이 컬렉터는 스트림의 병렬 리듀싱을 수행할 수 있다.
    * 컬렉터의 플래그에 `UNORDERED`를 함께 설정하지 않았다면 데이터 소스가 정렬되어 있지 않은(즉, 집합처럼 요소의 순서가 무의미한)
     상황에서만 병렬 리듀싱을 수행할 수 있다.
  * **IDENTITY_FINISH**
    * `finisher` 메서드가 반환하는 함수는 단순히 `identity`를 적용할 뿐이므로 이를 생랼할 수 있다.
    * 따라서 리듀싱 과정의 최종 결과로 누적자 객체를 바로 사용할 수 있다. 또한 누적자 A를 결과 R로 안전하게 형변환할 수 있다.

