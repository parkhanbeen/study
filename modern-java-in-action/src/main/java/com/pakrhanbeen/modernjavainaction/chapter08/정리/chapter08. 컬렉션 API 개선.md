# 컬렉션 API 개선

## 컬렉션 팩토리

* 자바 9에서는 작은 컬렉션 객체를 쉽게 만들 수 있는 몇 가지 방법을 제공한다.

```java
List<String> friends = new ArrayList<>();
friends.add("민수");
friends.add("철수");
friends.add("영희");

List<String> friends = Array.asList("민수", "철수", "영희");

// set의 경우
Set<String> friends = new HashSet<>(Array.asList("민수", "철수", "영희"));

// Stream API를 이용하는 경우
Set<String> friends = Stream.of("민수", "철수", "영희")
    .collect(Collectors.toSet());
```

* `Array.asList` 팩터리 메서드를 이용해 코드를 간단하게 줄일 수 있다.
* 고정 크기의 리스트를 만들었으므로 요소를 갱신할 순 있지만 새 요소를 추가하거나 요소를 삭제할 순 없다.
  * 요소를 추가하려 하면 `UnsupportedOperationException`이 발생한다.
* `Set`의 경우 `Array.asSet` 이라는 팩터리 메서드가 없어 다른 방법으로 생성한다.
  * 하지만 두 방법 모두 매끄럽지 못하며 내부적으로 불필요한 객체 할당을 필요로 한다. 
  * 결과는 변환할 수 있는 집합이라는 사실도 주목하자.

### 자바 9의 컬렉션 팩토리 메서드

* `List.of` : 변경할 수 없는 리스트를 만듬, 요소 자체가 변하는 것을 막을 순 없다.
* `Set.of` : 변경할 수 없는 집합을 만듬, 중복된 요소를 제공해 집합을 만들려고하면 `IllegalArgumentException`이 발생한다.
* `Map.of` : 키와 값을 번갈아 제공하는 방법으로 맵을 만들 수 있다. 열개 이하의 키와 값 쌍을 가진 작은 맵을 만들 때 유용하다.
  * ```java
    Map<String, Integer> ageOfFriends = Map.of("민수", 30, "철수", 21, "영희", 28);
    ```
* `Map.ofEntries` : 10개 이상의 맵에서 유용하며 `Map.Entry<K, V>` 객체를 인수로 받는다.
  * ```java
    Map<String, Integer> ageOfFriends = Map
    .ofEntries(Map.entry("민수", 30), Map.entry("철수", 21), Map.entry("영희", 28));
    ```

## 리스트와 집합 처리

* 자바 8에서는 `List, Set` 인터페이스에 다음과 같은 메서드를 추가했다.
  * `removeIf` : 프레디케이트를 만족하는 요소를 제거한다. `List`나 `Set`을 구현하거나 그 구현을 상속받은 모든 클래스에서 이용할 수 있다.
  * `replaceAll` : 리스트에서 이용할 수 있는 기능으로 `UnaryOperator` 함수를 이용해 요소를 바꾼다.
  * `sort` : `List` 인터페이스에서 제공하는 기능으로 리스트를 정렬한다.
* 이들 메서드는 호출한 컬렉션 자체를 바꾼다. 

### removeIf 메서드

```java
for (Transaction transcation : transactions) {
    if (Character.isDigit(transaction.getReferenceCode().charAt(0))) {
        transactions.remove(transaction);
    }
}

// 내부적으로 해석한 코드
for (Iterator<Transaction> iterator = transactions.iterator(); iterator.hasNext();) {
    Transaction transaction = iterator.next();
    if (Character.isDigit(transaction.getReferenceCode().charAt(0))) {
        transactions.remove(transction);
    }
}
```

* 위 코드는 `ConcurrentModificationException`을 일으킨다. 내부적으로 보면 두 개의 개별 객체가 컬렉션을 관리한다.
* 결과적으로 반복자의 상태는 컬렉션의 상태와 서로 동기화되지 않는다.
* `Iterator` 객체를 명시적으로 사용하고 그 객체의 `remove()` 메서드를 호출함으로 문제를 해결할 수 있다.
```java
for (Iterator<Transaction> iterator = transactions.iterator(); iterator.hasNext();) {
    Transaction transaction = iterator.next();
    if (Character.isDigit(transaction.getReferenceCode().charAt(0))) {
        iterator.remove();
    }
}
```
* 코드가 조금 복잡해졌다. 이 코드 패턴은 자바 8의 `removeIf` 메서드로 바꿀 수 있다.
```java
transactions.removeIf(transaction -> 
    Character.isDigit(transaction.getReferenceCode().charAt(0)));
```
* 코드가 단순해질 뿐 아니라 버그도 예방할 수 있다.

### replaceAll 메서드

* `List` 인터페이스의 `replaceAll` 메서드를 이용해 리스트의 각 요소를 새로운 요소로 바꿀 수 있다.
* 스트림 API를 이용해 문제를 해결할 수 있지만 스트림은 새로운 컬렉션을 만든다. 만약 우리가 원하는 것이 기존 컬렉션을 바꾸는 것이라면 다음과 
 같이 이용할 수 있다.
```java
referenceCodes.replaceAll(code -> 
    Character.toUpperCase(code.charAt(0)) + code.substring(1));
```

## 맵 처리

* 자바 8에서는 `Map` 인터페이스에 몇 가지 디폴트 메서드를 추가했다. 다양한 메서드를 살펴보자.

### forEach 메서드

* 자바 8에서 부터 `Map` 인터페이스는 `BiConsumer`(키와 값을 인수로 받음)를 인수로 받는 `forEach` 메서드를 지원하므로 코드를 
 간단하게 구현할 수 있다.
```java
ageOfFriends.forEach((friend, age) -> 
    System.out.println(friend + " is " + age + " yrears old"));
```

### 정렬 메서드

* 두 개의 새로운 유틸리티를 이용하면 맵의 항목을 값 또는 키를 기준으로 정렬할 수 있다.
  * `Entry.comparingByValue`
  * `Entry.comparingByKey`

```java
Map<String, String> favouriteGames = Map.ofEntries(
    entry("민수", "스타크레프트"), 
    entry("영희", "카트라이더"),
    entry("철수", "거상"));

favouriteGames.entrySet()
    .stream()
    .sorted(Entry.ComparingByKey())   // 사람의 이름을 알파벳 순으로 스트림 요소를 처리함
    .forEachOrdered(System.out::println);
```

### getOrDefault 메서드

* 기존에 찾으려는 키가 존재하지 않으면 null이 반환되므로 `NullPointerException`을 방지하려면 요청 결과가 null인지 확인해야 한다.
* `getOrDefault` 메서드를 이용하면 쉽게 이 문제를 해결할 수 있다.
```java
Map<String, String> favouriteGames = Map.ofEntries(
    entry("민수", "스타크레프트"),
    entry("영희", "카트라이더"),
    entry("철수", "거상"));

System.out.println(favouriteGames.getOrDefault("한빈", "메이플스토리"));
```

* 키가 존재하더라도 값이 null 인 상황에서는 null을 반환할 수 있다는 사실을 주목하자.
  * 즉 키가 존재하느냐의 여부에 따라서 두 번째 인수가 반환될지 결정된다.

### 계산 패턴

* 맵에 키가 존재하는지 여부에 따라 어떤 동작을 실행하고 결과를 저장해야 하는 상황이 필요한 때가 있다.
* `computeIfAbsent` : 제공된 키에 해당하는 값이 없으면(값이 없거나 null), 키를 이용해 새 값을 계산하고 맵에 추가한다.
* `computeIfPresent` : 제공된 키가 존재하면 새 값을 계산하고 맵에 추가한다.
* `compute` : 제공된 키로 새 값을 계산하고 맵에 저장한다.
```java
Map<String, byte[]> dataToHash = new HashMap<>();
MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

lines.forEach(line ->    // line은 맵에서 찾을 키
    dataToHash.computeIfAbsent(line, this::calculateDigest));  // 키가 존재하지 않으면 동작을 실행

private byte[] calculateDigest(String key) {
    return messageDigest.digest(key.getBytes(StandardCharsets.UTF_8));
}
```

### 삭제 패턴

* 자바 8에서는 키가 특정한 값과 연관되었을 때만 항목을 제거하는 오버로드 버전 메서드를 제공한다.
```java
favouriteGames.remove(key, value);
```

### 교체 패턴

* `replaceAll` : `BiFunction`을 적용한 결과로 각 항목의 값을 교체한다. 이 메서드는 이전에 살펴본 `List`의 `replaceAll`과 비슷한 동작을 수행한다.
* `Replace` : 키가 존재하면 맵의 값을 바꾼다. 키가 특정 값으로 매핑되었을 때만 값을 교체하는 오버로드 버전도 있다.

```java
Map<String, String> favouriteMovies = new HashMap<>(); // replaceAll을 적용하기 위해 바꿀수 있는 맵을 사용해야함
favouriteMovies.put("Raphael", "Star Wars");
favouriteMovies.put("Olivia", "james bond");
favouriteMovies.replaceAll((friend, moive) -> movie.toUpperCase());
```

### 합침

* 중복된 키가 없다면 `putAll`을 이용해 두 개의 맵을 합칠 수 있다.
* 값을 좀 더 유연하게 합쳐야 한다면 새로운 `merge` 메서드를 이용할 수 있다.
* 이 메서드는 중복된 키를 어떻게 합칠지 결정하는 `BiFunction`을 인수로 받는다.

```java
Map<String, String> family = Map.ofEntries(
    entry("Teo", "Star Wars"),
    entry("Cristina", "James Bond"));
Map<String, String> friends = Map.ofEntries(
    entry("Raphael", "Star Wars"),
    entry("Cristina", "Matrix"));

Map<String, String> everyone = new HashMap<>(family);
friends.forEach((k, v) ->
        everyone.merge(k, v, (movie1, movie2) -> movie1 + " $ " + movie2));  // 중복된 키가 있으면 두 값을 연결
```

* `merge`를 이용해 초기화 검사를 구현할 수도 있다.
```java
moviesToCount.merge(movieName, 1L, (key, count) -> count + 1L);
```
* 두 번째 인수 1L은 키의 반환 값이 없거나 null, 또는 키가 null 일 경우 기본 값으로 사용된다. 그 다음부터는 값이 1로 초기화되어 있으므로
 `BiFunction`을 적용해 값이 증가된다.

## 개선된 ConcurrentHashMap

* `ConcurrentHashMap` 클래스는 동시성 친화적이며 최신 기술을 반영한 `hashMap` 버전이다.
* `ConcurrentHashMap`은 내부 자료구조의 특정 부분만 잠궈 동시 추가, 갱신 작업을 허용한다. 따라서 동기화된 `Hashtable` 버전에 비해 
 읽기 쓰기 연산 성능이 월등하다(참고로, 표준 `HashMap`은 비동기로 동작함).

### 리듀스와 검색

* `forEach` : 각(키,값) 쌍에 주어진 액션을 실행
* `reduce` : 모든(키,값) 쌍을 제공된 리듀스 함수를 이용해 결과로 합침
* `search` : 널이 아닌 값을 반환할 때까지 각 (키,값) 쌍에 함수를 적용


* 이들 연산은 `ConcurrentHashMap`의 상태를 잠그지 않고 연산을 수행한다는 점을 주목하자. 따라서 이들 연산에 제공한 함수는 계산이 진행되는
 동안 바뀔 수 있는 객체, 값, 순서 등에 의존하지 않아야 한다.
* 또한 이들 연산에 병렬성 기준값(threshold)을 지정해야 한다. 
* 맵의 크기가 주어진 기준값보다 작으면 순차적으로 연산을 실행한다. 기준값을 1로 지정하면 공통 스레드 풀을 이용해 병렬성을 극대화한다.
  * `Long.MAX_VALUE`를 기준값으로 설정하면 한 개의 스레드로 연산을 실행한다.
```java
ConcurrentHashMap<String, Long> map = new ConcurrentHashMap<>();
long parallelismThreshold = 1;
Optional<Integer> maxValue = Optional.ofNullAble(map.reduceValues(parallelismThreshold, Long::max));
```
* `int, long, double`등의 기본값에는 전용 `each reduce` 연산이 제공되므로 `reduceValuesToInt, reduceKeysToLong`등을 
 이용하면 박싱 작업을 할 필요가 없고 효율적으로 작업을 처리할 수 있다.

### 계수

* `ConcurrentHashMap` 클래스는 맵의 매핑 개수를 반환하는 `mappingCount` 메서드를 제공한다.
* 기존의 `size` 메서드 대신 새 코드에서는 `int`를 반환하는 `mappingCount` 메서드를 사용하는 것이 좋다. 그래야 매핑의 개수가 `int`의
 범위를 넘어서는 이후의 상황을 대처할 수 있기 때문이다.

### 집합뷰

* `ConcurrentHashMap` 클래스는 `ConcurrentHashMap`을 집합 뷰로 반환하는 `keySet`이라는 새 메서드를 제공한다.
* 맵을 바꾸면 집합도 바뀌고 반대로 집합을 바꾸면 맵도 영향을 받는다.
* `newKeySet`이라는 새 메서드를 이용해 `ConcurrentHashMap`으로 유지되는 집합을 만들 수도 있다.
