# 디폴트 메서드

* 자바8에서는 기본 구현을 포함하는 인터페이스를 정의하는 두 가지 방법을 제공한다. 
  * 첫 번째는 인터페이스 내부에 **정적 메서드**를 사용하는 것이다.
  * 두 번째는 인터페이스의 기본 구현을 제공할 수 있도록 **디폴트 메서드** 기능을 사용하는 것이다.
* 자바8에서는 메서드 구현을 포함하는 인터페이스를 정의할 수 있다.
* 결과적으로 기존 인터페이스를 구현하는 클래스는 자동으로 인터페이스에 추가된 새로운 메서드의 디폴트 메서드를 상속받게 된다.
```java
default void sort(Comparator<? super E> c) {
    Collections.sort(this, c);
}
```

* `default` 키워드는 해당 메서드가 디폴트 메서드임을 가리킨다.
* 디폴트 메서드를 이용하면 자바 API의 호환성을 유지하면서 라이브러리를 바꿀 수 있다.
* 디폴트 메서드가 없던 시절에는 인터페이스에 메서드를 추가하면서 여러 문제가 발생했다. 인터페이스에 새로 추가된 메서드를 구현하도록 인터페이스를 구현하는
 기존 클래스를 고쳐야 했기 때문이다.

## 디폴트 메서드란 무엇인가?

* 디폴트 메서드는 `default`라는 키워드로 시작하며 다른 클래스에 선언된 메서드처럼 메서드 바디를 포함한다.

### 추상 클래스와 자바 8의 인터페이스

* 추상 클래스와 인터페이스의 차이점
  1. 클래스는 하나의 추상 클래스만 상속받을 수 있지만 인터페이스는 여러 개 구현할 수 있다.
  2. 추상 클래스는 인스턴스 변수로 공통 상태를 가질 수 있다. 하지만 인터페이스는 인스턴스 변수를 가질 수 없다.

## 디폴트 메서드 활용 패턴

* 디폴트 메서드를 이용하는 두 가지 방식 **선택형 메서드(optional method)** 와 **동작 다중 상속(multiple inheritance of behavior)** 이 있다.

### 선택형 메서드

* 자바 8이전의 인터페이스를 구현하는 클래스에서 기능을 사용하지 않는 메서드의 내용을 비워둔채로 구현하였다. 
* 디폴트 메서드를 이용하면 메서드의 기본 구현을 제공할 수 있으므로 인터페이스를 구현하는 클래스에서 빈 구현을 제공할 필요가 없어졌고 불필요한 코드를 줄일 수 있다.

### 동작 다중 상속

* 디폴트 메서드를 이용하면 기존에는 불가능했던 동작 다중 상속 기능도 구현할 수 있다. 중복되지 않는 최소한의 인터페이스를 유지한다면 우리 코드에서 
 동작을 쉽게 재사용하고 조합할 수 있다.

## 해석 규칙

* 자바 8에는 디폴트 메서드가 추가되었으므로 같은 시그니처를 갖는 디폴트 메서드를 상속받는 상황이 생길 수 있다.

```java
public interface A {
    default void hello() {
        System.out.println("Hello from A");
    }
}

public interface B extends A {
    default void hello() {
        System.out.println("Hello from B");
    }
}

public class C implements B, A {
    public static void main(String[] args) {
        new C().hello();
    }
}
```

* 다음 예제에서 클래스 C는 누구의 Hello를 호출할까?

### 알아야 할 세 가지 해결 규칙

* 다른 클래스나 인터페이스로부터 같은 시그니처를 갖는 메서드를 상속받을 때는 세 가지 규칙을 따라야 한다.

1. 클래스가 항상 이긴다. 클래스나 슈퍼클래스에서 정의한 메서드가 디폴트 메서드보다 우선권을 갖는다.
2. 1번 규칙 이외의 상황에서는 서브인터페이스가 이긴다. 상속관계를 갖는 인터페이스에서 같은 시그니처를 갖는 메서드를 정의할 때는 서브인터페이스가 이긴다.
 즉, B가 A를 상속받는다면 B가 A를 이긴다.
3. 여전히 디폴트 메서드의 우선순위가 결정되지 않았다면 여러 인터페이스를 상속받는 클래스가 명시적으로 디폴트 메서드를 오버라이드하고 호출해야 한다.

### 디폴트 메서드를 제공하는 서브인터페이스가 이긴다

```java
// 하나의 클래스를 상속받아 두 개의 인터페이스를 구현
public class D implements A {}
public class C extends D implements B, A {
    public static void main(String[] args) {
        new C().hello();
    }
}
```

* 1번 규칙은 클래스의 메서드 구현이 이긴다고 설명한다. D는 hello를 오버라이드하지 않았고 단순히 인터페이스 A를 구현했다. 따라서
 D는 인터페이스 A의 디폴트 메서드 구현을 상속받는다.
* 2번 규칙에서는 클래스나 슈퍼클래스에 메서드 정의가 없을 때는 디폴트 메서드를 정의하는 서브인터페이스가 선택된다. 따라서 컴파일러는 
 인터페이스 A의 hello나 인터페이스 B의 hello 둘 중 하나를 선택해야한다.
* 여기서 B가 A를 상속받는 관계이므로 이번에도 `Hello from B`가 출력된다.

### 충돌 그리고 명시적인 문제 해결

```java
public interface A {
    default void hello() {
        System.out.println("Hello from A");
    }
}

public interface B {
    default void hello() {
        System.out.println("Hello from B");
    }
}

public class C implements B, A {}
```
* 이번에는 인터페이스간 상속관계가 없으므로 2번 규칙을 적용할 수 없다.
* 자바 컴파일러는 어떤 메서드를 호출해야 할지 알 수 없으므로 `Error: class C inherits unrelated defaults for hello() from types B and A`
같은 에러가 발생한다.

#### 충돌 해결

* 클래스와 메서드 관계로 디폴트 메서드를 선택할 수 없는 상황에서는 선택할 수 있는 방법이 없다. 개발자가 직접 클래스 C에서 사용하려는 메서드를 명시적으로
 선택해야 한다.
* 즉, 클래스 C에서 hello 메서드를 오버라이드한 다음에 호출하려는 메서드를 명시적으로 선택해야 한다.
* 자바 8에서는 `X.super.m(...)` 형태의 새로운 문법을 제공한다. 여기서 X는 호출하려는 메서드 m의 슈퍼인터페이스다.
```java
public class C implements B, A {
    void hello() {
        B.super.hello();  // 명시적으로 인터페이스 B의 메서드를 선택한다.
    }
}
```

### 다이아몬드 문제

```java
public interface A {
    default void hello() {
        System.out.println("Hello from A");
    }
}
public interface B extends A {}
public interface C extends A {}
public class D implements B, C {
    public static void main(String... args) {
        new D().hello();
    }
}
```

* 실제로 선택할 수 있는 메서드 선언은 하나뿐이다. 결국 `Hello from A`가 출력된다.
