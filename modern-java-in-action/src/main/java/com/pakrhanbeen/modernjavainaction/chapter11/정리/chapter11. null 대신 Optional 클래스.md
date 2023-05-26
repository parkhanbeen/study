# null 대신 Optional 클래스

## 값이 없는 상황을 어떻게 처리할까?

### 보수적인 자세로 NPE 줄이기

* 대부분의 프로그래머는 필요한 곳에 다양한 `null` 확인 코드를 추가해서 `null` 예외 문제를 해결하려 할 것이다.
* 변수를 접근할 때마다 중첩된 `if`가 추가되면서 코드 들여쓰기 수준이 증가한다. 따라서 이와 같은 반복 패턴코드를 `깊은 의심`이라고 부른다.
* 이를 반복하다보면 코드의 구조가 엉망이 되고 가독성도 떨어진다.

### null 때문에 발생하는 문제

* **에러의 근원이다** : `NPE`은 자바에서 가장 흔히 발생하는 에러다.
* **코드를 어지럽힌다** : 때로는 중첩된 `null` 확인 코드를 추가해야 하므로 `null` 때문에 코드 가독성이 떨어진다.
* **아무 의미가 없다** : `null`은 아무 의미도 표현하지 않는다. 특히 정적 형식 언어에서 값이 없음을 표현하는 방법으로 적절하지 않다.
* **자바 철학에 위배된다** : 자바는 개발자로부터 모든 포인터를 숨겼다. 하지만 예외가 있는데 그것이 바로 `null` 포인터다.
* **형식 시스템에 구멍을 만든다** : `null`은 무형식이며 정보를 포함하고 있지 않으므로 모든 참조 형식에 `null`을 할당할 수 있다.
 이런 식으로 `null`이 할당되기 시작하면서 시스템의 다른 부분으로 `null`이 퍼졌을 때 애초에 `null`이 어떤 의미로 사용되었지 알 수 없다.

### 다른 언어는 null 대신 무얼 사용하나?

* 최근 그루비 같은 어너에서는 안전 내비게이션 연산자(?.)를 도입해서 `null` 문제를 해결했다.

```groovy
def carInsuranceName = person?.car?.insurance?.name
```

* 하스켈, 스칼라등의 함수형 언어는 다른 관점에서 `null` 문제를 접근한다.
* 하스켈은 선택형값을 저장할 수 있는 `Maybe`라는 형식을 제공해 주어진 형식의 값을 갖거나 아니면 아무 값도 갖지 않을 수 있다. 따라서 `null` 참조 개념은 자연스럽게 사라진다.

## Optional 클래스

* 자바8은 하스켈과 스칼라의 영향을 받아서 `java.util.Optional<T>`라는 새로운 클래스를 제공한다.
* `Optional`은 선택형 값을 캡술화하는 클래스다. 값이 있으면 `Optional` 클래스는 값을 감싼다. 반면 값이 없으면 `Optional.empty` 메서드로
 `Optional`을 반환한다.
* `Optional.empty`는 `Optional`의 특별한 싱글턴 인스턴스를 반환하는 정적 팩터리 메서드다.
* `null`과 `Optional.empty`의 차이점은 `null`을 참조하면 NPE이 발생하지만 `Optional.empty()`는 `Optional` 객체이므로 이를 다양한
 방식으로 활용할 수 있다.

## Optional 적용 패턴

### Optional 객체 만들기

#### 빈 Optional

```java
Optional<Car> optCar = Optional.empty();
```

#### null이 아닌 값으로 Optional 만들기

```java
Optional<Car> optCar = Optional.of(car);
```

* `car`가 `null`이라면 즉시 `NPE`이 발생한다(`Optional`을 사용하지 않았다면 `car`의 프로퍼티에 접근하려 할 때 에러가 발생했을 것이다).

#### null 값으로 Optional 만들기

```java
Optional<Car> optCar = Optional.ofNullable(car);
```
* `car`가 `null`이면 빈 `Optional` 객체가 반환된다.

### 맵으로 Optional의 값을 추출하고 변환하기

```java
Optional<Insurance> optInsurance = Optional.ofNullable(insurance);
Optional<String> name = optInsurance.map(Insurance::getName);
```

* `Optional`의 `map`메서드는 스트림의 `map` 메서드와 개념적으로 비슷하다. `Optional` 객체를 최대 요소의 개수가 한 개 이하인 데이터 컬렉션으로 생각할 수 있다.
* `Optional`이 값을 포함하면 `map`의 인수로 제공된 함수가 값을 바꾼다. `Optional`이 비어있으면 아무 일도 일어나지 않는다.

### flatMap 으로 Optional 객체 연결

```java
Optional<Person> optPerson = Optional.of(person);
Optional<String> name = optPerson.map(Person::getCar)
        .map(Car::getInsurance)
        .map(Insurance::getName);
```
* 위 코드는 컴파일되지 않는다. `optPeople`의 형식은 `Optional<People>` 이므로 `map` 메서드를 호출할 수 있다.
* 하지만 `getCar`는 `Optional<Car>`형식 객체를 반환한다. 즉 `map` 연산의 결과는 `Optional<Optional<Car>>` 형식의 객체다.
* `getInsurance`는 또 다른 `Optional` 객체를 반환하므로 `getInsurance` 메서드를 지원하지 않는다.
* 이러한 문제는 `Optional`의 `flatMap`을 이용해 해결할 수 있다. `flatMap` 메서드로 전달된 함수는 하나의 `Optional`로 바뀐다.

```java
public String getCarInsuranceName(Optional<Person> person) {
    return person.flatMap(Person::getCar)
        .flatMap(Car::getInsurance)
        .map(Insurance::getName)
        .orElse("Unknown");  // 비어있으면 기본값 사용
}
```

* `map`대신 `flatMap`을 이용하여 두 `Optional`을 합치는 기능(평준화)을 수행하면서 둘 중 하나라도 `null`이면 빈 `Optional`을 생성하는 연산을 한다.
* 마지막 `Insurance.getName()`은 `String`을 반환하므로 `flatMap`을 사용할 필요가 없다.

### 도메인 모델에 Optional을 사용했을 때 데이터를 직렬화를 할 수 없는 이유

* 자바 언어 아키텍트인 브라이언 고츠는 `Optional`의 용도가 선택형 반환값을 지원하는 것이라고 명확하게 못박았다.
* `Optional` 클래스는 필드 형식으로 사용할 것을 가정하지 않았으므로 `Serializable` 인터페이스를 구현하지 않는다. 따라서
 우리 도메인 모델에 `Optional`을 사용한다면 직렬화 모델을 사용하는 도구나 프레임워크에서 문제가 생길 수 있다.
* 이와 같은 단점에도 불구하고 여전히 `Optional`을 사용해서 도메인 모델을 구성하는 것이 바람직하다고 생각한다.
  * 특히 객체 그래프에서 일부 또는 객체가 `null`일수 있는 상황이라면 더욱 그렇다.
  * 직렬화 모델이 필요하다면 다음 예제에서 보여주는 것처럼 `Optional`로 값을 반환받을 수 있는 메서드를 추가하는 방식을 권장한다.
  ```java
  public class Person {
    private Car car;
    public Optional<Car> getCarAsOptional() {
        return Optional.ofNullable(car);
    }
  }
  ```
  
### Optional 스트림 조작

* 자바 9에서는 `Optional`을 포함하는 스트림을 쉽게 처리할 수 있도록 `Optional`에 `stream()` 메서드를 추가했다.
```java
Stream<Optional<String>> stream = ...
Set<String> result = stream.filter(Optional::isPresent)
    .map(Optional::get)
    .collect(toSet());
```

### 디폴트 액션과 Optional 언랩

* **get()**
  * 값을 읽는 가장 간단한 메서드면서 동시에 가장 안전하지 않은 메서드다.
  * 래핑된 값이 있으면 해당 값을 반환하고 값이 없으면 `NoSuchElementException`을 발생시킨다.
  * 따라서 `Optional`에 값이 반드시 있다고 가정할 수 있는 상황이 아니면 `get` 메서드를 사용하지 않는 것이 바람직하다.
   결국 상황은 중첩된 `null` 확인 코드를 넣는 상황과 크게 다르지 않다.
* **orElse(T other)**
  * `Optional`이 값을 포함하지 않을 때 기본값을 제공할 수 있다.
* **orElseGet(Supplier<? extends T> other)**
  * `orElse` 메서드에 대응하는 게으른 버전의 메서드다.
  * `Optional`에 값이 없을 때만 `Supplier`가 실행되기 때문이다. 디폴트 메서드를 만드는 데 시간이 걸리거나(효율성 때문에) `Optional`이 비어있을 때만
   기본값을 생성하고 싶다면(기본값이 반드시 필요한 상황) `orElseGet(Supplier<? extends T> other)`를 사용해야 한다.
* **orElseThrow(Supplier<? extends x> exceptionSupplier)**
  * `Optional`이 비어있을 때 예외를 발생시킨다는 점에서 `get` 메서드와 비슷하다. 하지만 이 메서드는 발생시킬 예외의 종류를 선택할 수 있다.
* **ifPresent(Consumer<? super T> consumer)**
  * 값이 존재할 때 인수로 넘겨준 동작을 실행할 수 있다. 값이 없으면 아무 일도 일어나지 않는다.

자바9에서 추가

* **ifPresentOrElse(Consumer<? super T> action, Runnable emptyAction)**
  * 이 메서드는 `Optional`이 비었을 때 실행할 수 있는 `Runnable`을 인수로 받는다는 점만 `ifPresent`와 다르다.

### 두 Optional 합치기

```java
public Optional<Insurance> nullSafeFindCheapestInsurance(Optional<Person> person, Optional<Car> car) {
    if (person.isPresent() && car.isPresent()) {
        return Optional.of(findCheapestInsurance(person.get(), car.get()));
    } else {
        return Optional.empty();
    }
}
```

* `null` 체크 코드와 크게 다른 점이 없다. `Optional` 클래스에서 제공하는 기능을 이용해서 이 코드를 개선하자

```java
public Optional<Insurance> nullSafeFindCheapestInsurance(Optional<Person> person, Optional<Car> car) {
    return person.flatMap(p -> car.map(c -> findCheapestInsurance(p, c)));
}
```

### 필터로 특정값 거르기 

```java
Optional<Insurance> optInsurance = ...
optInsurance.filter(insurance -> "CambridgeInsurance".equals(insurance.getName()))
    .ifPresent(x -> System.out.printLn("ok"));
```

* `filter` 메서드는 프레디케이트를 인수로 받는다. `Optional` 객체가 값을 가지며 프레디케이트와 일치하면 `filter` 메서드는 그 값을 반환하고
 그렇지 않으면 빈 `Optional` 객체를 반환한다.
* 스트림의 `filter` 연산과 같다고 이해하면 쉽다.

## Optional을 사용한 실용 예제

### 잠재적으로 null이 될 수 있는 대상을 Optional로 감싸기

* 기존 자바 API에서는 `null`을 반환하면서 요청한 값이 없거나 어떤 문제로 계산에 실패했음을 알린다.
  * 예를 들어 `Map`의 `get` 메서드는 요청한 키에 대응하는 값을 찾지 못했을 때 `null`을 반환한다. `get` 메서드의 시그니처는 우리가 고칠 수 없지만
   반환값은 `Optional`로 감쌀 수 있다.
  ```java
   Optional<Object> value = Optional.ofNullalbe(map.get("key"));
  ```
  
### 예외와 Optional 클래스

* 자바 API는 어떤 이유에서 값을 제공할 수 없을 때 `null` 대신 예외를 발생시킬 경우가 있다. 
```java
public static Optional<Integer> stringToInt(String s) {
    try {
        return Optional.of(Integer.parseInt(s));
    } catch (NumberFormatException e) {
        return Optional.empty();
    }
}
```

* 위 코드와 같이 예외가 발생할 때 빈 `Optional`을 반환할 수 있다.

### 기본형 Optional을 사용하지 말아야 하는 이유

* 스트림처럼 `Optional`도 기본형으로 특화된 `OptionalInt, OptionalLong, OptionalDouble`등의 클래스를 제공한다.
* 하지만 `Optional`의 최대 요소 수는 한 개이므로 `Optional`에서는 기본형 특화 클래스로 성능을 개선할 수 없다.
* 기본형 특화 `Optional`은 `map, flatMap, filter`등을 지원하지 않으므로 권장하지 않는다.
* 게다가 스트림과 마찬가지로 기본형 특화 `Optional`로 생성한 결과는 다른 일반 `Optional`과 혼용할 수 없다.
