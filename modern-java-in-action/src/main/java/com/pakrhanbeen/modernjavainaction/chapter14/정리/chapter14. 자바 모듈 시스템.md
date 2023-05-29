# 자바 모듈 시스템

## 압력 : 소프트웨어 유추

### 관심사분리(SOC, Separation of concerns)

* 관심사분리는 컴퓨터 프로그램을 고유의 기능으로 나누는 동작을 권장하는 원칙이다. 클래스를 그룹화한 모듈을 이용해 애플리케이션의 클래스 간의 관계를
 시각적으로 보여줄 수 있다.
* SOC 원칙은 모델, 뷰, 컨트롤러 같은 아키텍처 관점 그리고 복구 기법을 비즈니스 로직과 분리하는 등의 하위 수준 접근 등의 상황에 유용하다.
* SOC 원칙 장점
  * 개별 기능을 따로 작업할 수 있으므로 팀이 쉽게 협업할 수 있다.
  * 개별 부분을 재사용하기 쉽다.
  * 전체 시스템을 쉽게 유지보수할 수 있다.

### 정보 은닉

* **정보 은닉**은 세부 구현을 숨기도록 장려하는 원칙이다.
* 세부 구현을 숨김으로 프로그램의 어떤 부분을 바꿧을 때 다른 부분까지 영향을 미칠 가능성을 줄일 수 있다. 즉 코드를 관리하고 보호하는 데 유용한 원칙이다.
* **캡슐화**는 특정 코드 조각이 애플리케이션의 다른 부분과 고립되어 있음을 의미한다. 캡슐화된 코드의 내부적인 변화가 의도치 않게 외부에 영향을 미칠 가능성이 줄어든다.
* 자바 9이전까지는 **클래스와 패키지가 의도된 대로 공개되었는지**를 컴파일러로 확인할 수 있는 기능이 없었다.

### 자바 소프트웨어

* 잘 설계된 소프트웨어를 만들려면 이 두 가지 원칙을 따르는 것이 필수다.
* 자바는 객체 지향 언어로 우리는 클래스, 인터페이스를 이용한다. 특정 문제와 관련된 패키지, 클래스, 인터페이스를 그룹으로 만들어 코드를 그룹화할 수 있다.
* 코드 자체를 보고 소프트웨어의 동작을 추론하긴 현실적으로 어렵다. 따라서 UML 다이어그램같은 도구를 이용하면 그룹 코드 간의 의존성을 시각적으로 보여줄 수 있으므로 소프트웨어를
 추론하는데 도움이 된다.

## 자바 모듈 시스템을 설계한 이유

### 모듈화의 한계

* 자바 9이전까지는 모듈화된 소프트웨어 프로젝트를 만드는 데 한계가 있었다. 자바는 클래스, 패키지, JAR 세 가지 수준의 코드 그룹화를 제공한다.
* 클래스와 관련해 자바는 접근 제한자와 캡슐화를 지원했다. 하지만 패키지와 JAR 수준에서는 캡슐화를 거의 지원하지 않았다.

#### 제한된 가시성 제어

* `public, protected, 패키지 수준, private` 이렇게 네 가지 가시성 접근자가 있다.
* 한 패키지의 클래스와 인터페이스를 다른 패키지로 공개하려면 `public`으로 이들을 선언해야 한다. 결과적으로 이들 클래스와 인터페이스는 모두에게 공개된다.
* 이런 상황에서 보통 패키지 내부의 접근자가 `public`이므로 사용자가 이 내부 구현을 마음대로 사용할 수 있다.
* 내부적으로 사용할 목적으로 만든 구현을 다른 프로그래머가 임시적으로 사용해서 정착해버릴 수 있으므로 결국 기존의 애플리케이션을 망가뜨리지 않고 라이브러리 코드를 바꾸기가 어려워진다.
* 보안 측면에서 볼 때 코드가 노출되었으므로 코드를 임의로 조작하는 위협에 더 많이 노출될 수 있다.

#### 클래스 경로

* 애플리케이션을 번들하고 실행하는 기능과 관련해 자바는 태생적으로 약점을 갖고 있다.
* 클래스를 모두 컴파일한 다음 보통 한 개의 평범한 JAR 파일에 넣고 클래스 경로에 이 JAR 파일을 추가해 사용할 수 있다. 그러면 JVM이 동적으로 클래스 경로에 정의된 클래스를 필요할 때 읽는다.
* 안타깝게도 클래스 경로와 JAR 조합에는 몇 가지 약점이 존재한다.
  1. 클래스 경로에는 같은 클래스를 구분하는 버전 개념이 없다.
     * 클래스 경로에 두 가지 버전의 같은 라이브러리가 존재할 때 어떤 일이 일어날지 예측할 수 없다.
     * 다양한 컴포넌트가 같은 라이브러리의 다른 버전을 사용하는 상황이 발생할 수 있는 큰 애플리케이션에서 이런 문제가 두드러진다.
  2. 클래스 경로는 명시적인 의존성을 지원하지 않는다.
     * 각각의 JAR 안에 있는 모든 클래스는 `classes`라는 한 주머니로 합쳐진다. 즉, 한 JAR가 다른 JAR에 포함된 클래스 집합을 사용하라고 명시적으로 의존성을 정의하는 기능을 제공하지 않는다.
     * 자바 9 이전에는 자바, JVM 누구도 명시적인 의존성 정의를 지원하지 않아 JVM이 `ClassNotFoundException`같은 에러를 발생시키지 않고 애플리케이션을
      정상적으로 실행할 때까지 클래스 경로에 클래스 파일을 더하거나 클래스 경로에서 클래스를 제거해보는 수밖에 없다.
     * 자바 9 모듈 시스템을 이용하면 컴파일 타임에 이런 종류의 에러를 모두 검출할 수 있다.

### 거대한 JDK

* **자바 개발 키트(JDK)** 는 자바 프로그램을 만들고 실행하는 데 도움을 주는 도구의 집합이다.
* 가장 익숙한 도구로 자바 프로그램을 컴파일하는 `javac`, 자바 애플리케이션을 로드하고 실행하는 `java`, 입출력을 포함해 런타임 지원을 제공하는 `JDK`라이브러리, 컬렉션, 스트림 등이 있다.
* JDK 라이브러리의 많은 내부 API는 공개되지 않아야 한다. 예를 들어 스프링, 네티, 모키토 등 여러 라이브러리에서 `sun.misc.Unsafe`라는 클래스를 사용했는데
 이 클래스는 JDK 내부에서만 사용하도록 만든 클래스다. 결과적으로 호환성을 깨지 않고는 관련 API를 바꾸기 아주 어려운 상황이 되었다.
* 이런 문제들 때문에 JDK 자체도 모듈화할 수 있는 자바 모듈 시스템 설계의 필요성이 제기되었다. 즉 JDK에서 필요한 부분만 골라 사용하고, 클래스 경로를 쉽게 유추할 수 있으며,
 플랫폼을 진화시킬 수 있는 강력한 캡슐화를 제공할 새로운 건축 구조가 필요했다.

## 자바 모듈 : 큰 그림

* 자바 8는 **모듈**이라는 새로운 자바 프로그램 구조 단위를 제공한다. 모듈은 `module`이라는 새 키워드에 이름과 바디를 추가해서 정의한다.
* **모듈 디스크립터(module descriptor)** 는 `module-info.java`라는 특별한 파일에 저장된다.
* 모듈 디스크립터는 보통 패키지와 같은 폴더에 위치하며 한 개 이상의 패키지를 서술하고 캡슐화할 수 있지만 단순한 상황에서는 이들 패키지 중 한 개만 외부로 노출시킨다.
  * `module` : 모듈명
  * `exports` : 패키지명
  * `requires` : 모듈명

## 여러 모듈 활용하기

### exports 구문

```yaml
module expenses.readers {
    exports com.example.expenses.readers;
    exports com.example.expenses.readers.file;
    exports com.example.expenses.readers.http; 
}
```

* `exports`는 다른 모듈에서 사용할 수 있도록 특정 패키지를 공개 형식으로 만든다.
* 기본적으로 모듈 내의 모든 것은 캡슐화된다. 다른 모듈에서 사용할 수 있는 기능이 무엇인지 명시적으로 결정해야 한다.

### requires 구분

```yaml
module expenses.readers {
  requires java.base;   // 모듈명

  exports com.example.expenses.readers;
  exports com.example.expenses.readers.file;
  exports com.example.expenses.readers.http;
}
```

* `requires`는 의존하고 있는 모듈을 지정한다. 기본적으로 모든 모듈은 `java.base`라는 플랫폼 모듈에 의존하는데 이 플랫폼 모듈은 `net, io, util`
 등의 자바 메인 패키지를 포함한다. 항상 기본적으로 필요한 모듈이므로 `java.base`는 명시적으로 정의할 필요가 없다.
* 따라서 `java.base`외의 모듈을 임포트할 때 `requires`를 사용한다.

### 이름 정하기

* 오라클은 패키지명처럼 인터넷 도메인명을 역순(ex com.iteratrlearning.training)으로 모듈의 이름을 정하도록 권고한다.
* 더욱이 모듈명은 노출된 주요 API 패키지와 이름이 같아야 한다는 규칙도 따라야 한다.
* 모듈이 패키지를 포함하지 않거나 어떤 다른 이유로 노출된 패키지 중 하나와 이름이 일치하지 않는 상황을 제외하면 모듈명은 작성자의 인터넷 도메인명을 역순으로 시작해야 한다.

## 모듈 정의와 구문들

### requires

* `requires` 구문은 컴파일 타임과 런타임에 한 모듈이 다른 모듈에 의존함을 정의한다.
```
module com.iteratrlearning.application {
    requires com.iteratrlearning.ui;
}
```
* `com.iteratrlearning.application`은 `com.iteratrlearning.ui` 모듈에 의존한다.

### exports

* `exports` 구문은 지정한 패키지를 다른 모듈에서 이용할 수 있도록 공개 형식으로 만든다. 
* 아무 패키지도 공개하지 않는 것이 기본 설정이다. 
* 어떤 패키지를 공개할 것인지를 명시적으로 지정함으로 캡슐화를 높일 수 있다.
```
module com.iteratrlearning.ui {
    requires com.iteratrlearning.core;
    exports com.iteratrlearning.ui.panels;
    exports com.iteratrlearning.ui.widgets;
}
```

* `exports com.iteratrlearning.ui.panels`와 `exports com.iteratrlearning.ui.widgets`를 공개했다(문법이 비슷함에도 불구하고 
`exports`는 **패키지명**을 인수로 받지만 `requires`는 **모듈명**을 인수로 받는다는 사실에 주의하자).

### requires transitive

* 다른 모듈이 제공하는 공개 형식을 한 모듈에서 사용할 수 있다고 지정할 수 있다.
```
module com.iteratrlearning.ui {
    requires transitive com.iteratrlearning.core;
    
    exports com.iteratrlearning.ui.panels;
    exports com.iteratrlearning.ui.widgets;
}
module com.iteratrlearning.application {
    requires com.iteratrlearning.ui;
}
```

* `requires`를 `requires transitive`로 바꿀 수 있다.
* 결과적으로 `com.iteratrlearning.application` 모듈은 `com.iteratrlearning.core`에서 노출한 공개 형식에 접근할 수 있다.
* 필요로 하는 모듈(`com.iteratrlearning.ui`)이 다른 모듈(`com.iteratrlearning.core`)의 형식을 반환하는 상황에서 전이성 선언을 유용하게 사용할 수 있다.

### exports to

* `exports to` 구문을 이용해 사용자에게 공개할 기능을 제한함으로 가시성을 좀 더 정교하게 제어할 수 있다.
```
module com.iteratrlearning.ui {
    requires com.iteratrlearning.core;
    
    exports com.iteratrlearning.ui.panels;
    exports com.iteratrlearning.ui.widgets to
        com.iteratrlearning.ui.widgetuser;
}
```

### open과 opens

* 모듈 선언에 `open` 한정자를 이용하면 모든 패키지를 다른 모듈에 반사적으로 접근을 허용할 수 있다.
```
open module com.iteratrlearning.ui {
}
```

* 자바 9 이전에는 리플렉션으로 객체의 비공개 상태를 확인할 수 있었다. 즉 진정한 캡슐화는 존재하지 않았다.
* 자바 9에서는 기본적으로 리플렉션이 이런 기능을 허용하지 않는다.
* 리플렉션 때문에 전체 모듈을 개방하지 않고도 `opens` 구분을 모듈 선언에 이용해 필요한 개별 패키지만 개방할 수 있다.
* `exports-to`로 노출한 패키지를 사용할 수 있는 모듈을 한정했던 것처럼, `open`에 `to`를 붙여서 반사적인 접근을 특정 모듈에만 허용할 수 있다.
