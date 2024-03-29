# 리액티브 프로그래밍

* 리액티브 프로그래밍 패러다임의 중요성이 증가하는 이유는 아래와 같다.
  1. **빅데이터** : 보통 빅데이터는 페타바이트 단위로 구성되며 매일 증가한다.
  2. **다양한 환경** : 모바일 디바이스에서 수천 개의 멀티 코어 프로세서로 실행되는 클라우드 기반 클러스터에 이르기까지 다양한 환경에 
   애플리케이션이 배포된다.
  3. **사용 패턴** : 사용자는 1년 내내 항상 서비스를 이용할 수 있으며 밀리초 단위의 응답 시간을 기대한다.

## 리액티브 매니패스토

* 리액티브 매니패스토는 리액티브 애플리케이션과 시스템 개발의 핵심 원칙을 공식적으로 정의한다.
* **반응성(responsive)**
  * 리액티브 시스템은 빠를 뿐 아니라 더 중요한 특징으로 일정하고 예상할 수 있는 반응 시간을 제공한다.
  * 결과적으로 사용자가 기대치를 가질 수 있다. 기대치를 통해 사용자의 확신이 증가하면서 사용할 수 있는 애플리케이션이라는 확인을 제공할 수 있다.
  
* **회복성(resilient)**
  * 장애가 발생해도 시스템은 반응해야 한다.
* **탄력성(elastic)**
  * 애플리케이션의 생명주기 동안 다양한 작업 부하를 받게 되는데 이 다양한 작업 부하로 애플리케이션의 반응이 위협받을 수 있다. 리액티브 시스템에서는
   무거운 작업 부하가 발생하면 자동으로 관련 컴포넌트에 할당된 자원 수를 늘린다.
* **메시지 주도(Message-driven)**
  * 회복성과 탄력성을 지원하려면 약한 결합, 고립, 위치 투명성등을 지원할 수 있도록 시스템을 구성하는 컴포넌트의 경계를 명확하게 정의해야한다.
  * 비동기 메시지를 전달해 컴포넌트 끼리의 통신이 이루어진다. 이 덕분에 회복성(장애를 메시지로 처리)과 탄력성(주고 받은 메시지의 수를 감시하고 메시지의 양에 따라
   적절하게 리소스를 할당)을 얻을 수 있다.

### 애플리케이션 수준의 리액티브

* 애플리케이션 수준 컴포넌트의 리액티브 프로그래밍의 주요 기능은 비동기로 작업을 수행할 수 있다는 점이다.
* 이벤트 스트림을 블록하지 않고 비동기로 처리하는 것이 최신 멀티 코어 CPU의 사용률을 극대화할 수 있는 방법이다.
* 이들 기술은 스레드보다 가벼울 뿐 아니라 개발자에게 큰 이득을 제공한다. 개발자 입장에서는 이들 기술을 이용함으로 동기, 비동기 애플리케이션
 구현의 추상 수준을 높일 수 있으므로 동시기 블록, 경쟁 조건, 데드락 같은 저 수준의 멀티스레드 문제를 직접 처리할 필요가 없어지면서 비즈니스 요구사항을
 구현하는 데 더 집중할 수 있다.

### 시스템 수준의 리액티브

* **리액티브 시스템**은 여러 애플리케이션이 한 개의 일관적인, 회복할 수 있는 플랫폼을 구성할 수 있게 해줄 뿐 아니라 이들 애플리케이션 중 하나가 실패해도 전체
 시스템은 계속 운영될 수 있도록 도와주는 소프트웨어 아키텍처다.
* 리액티브 시스템은 애플리케이션을 조립하고 상호소통을 조절한다. 리액티브 시스템의 주요 속성으로 메시지 주도를 꼽을 수 있다.
* 메시지는 정의된 목적지 하나를 향하는 반면, 이벤트는 관련 이벤트를 관찰하도록 등록한 컴포넌트가 수신한다는 점이 다르다.
* 리액티브 시스템에서는 수신자와 발신자가 각각 수신 메시지, 발신 메시지와 결합하지 않도록 이들 메시지를 비동기로 처리해야 한다. 각 컴포넌트를 완전히 고립하려면
 이들이 결합되지 않도록 해야 하며 그래야만 시스템이 장애(**회복성**)와 높은 부하(**탄력성**)에도 **반응성**을 유지할 수 있다.

## 리액티브 스트림과 플로 API

* 리액티브 스트림은 잠재적으로 무한의 비동기 데이터를 순서대로 그리고 블록하지 않는 역압력을 전제해 처리하는 표준 기술이다.
* 역압력은 발행-구독 프로토콜에서 이벤트 스트림의 구독자가 발행자가 이벤트를 제공하는 속도보다 느린 속도로 이벤트를 소비하면서 문제가 발생하지 않도록
 보장하는 장치다.
* 넷플릭스, 레드햇, 트위터, 라이트벤드 및 기타 회사들이 참여한 리액티브 스트림 프로젝트에서는 모든 리액티브 스트림 구현이 제공해야 하는 최소 기능 집합을
 네 개의 관련 인터페이스로 정의했다.
* 자바 9의 새로운 `java.util.concurrent.Flow` 클래스뿐 아니라 `Akka`스트림(라이트벤드), 리액터(피보탈), RxJava(넷플릭스), Vert.x(레드햇)
 등 많은 서드 파티 라이브러리에서 이들 인터페이스를 구현한다.

### Flow 클래스 소개

* 자바 9에서는 리액티브 프로그래밍을 제공하는 클래스 `java.util.concurrent.Flow`를 추가했다. 이 클래스는 정적 컴포넌트 하나를 포함하고 있으며 
 인스턴스화할 수 없다. 리액티브 스트림 프로젝트의 표준에 따라 프로그래밍 발행-구독 모델을 지원할 수 있도록 `Flow` 클래스는 중첩된 인터페이스 네 개를 포함한다.

#### Publisher

* `Publisher`가 항목을 발행하면 `Subscriber`가 한 개씩 또는 한 번에 여러 항목을 소비하는데 `Subscription`이 이 과정을 관리할 수 있도록 `Flow` 클래스는 관련된 인터페이스와
 정적 메서드를 제공한다.
```java
@FunctionalInterface
public interface Publisher<T> {
    void subscribe(Subscriber<? super T> s);
}
```

#### Subscriber

* `Subscriber` 인터페이스는 `Publisher`가 관련 이벤트를 발행할 때 호출할 수 있도록 콜백 메서드 네 개를 정의한다.

```java
public interface Subscriber<T> {
    void onSubscribe(Subscription s);
    void onNext(T t);
    void onError(Throwable t);
    void onComplete();
}
```

* 이들 이벤트는 다음 프로토콜에서 정의한 순서로 지정된 메서드 호출을 통해 발행되어야 한다.
  * `onSubscribe onNext* (onError | onComplete)?`
  * `onSubscribe` 메서드가 항상 처음 호출되고 이어서 `onNext`가 여러 번 호출될 수 있음을 의미한다.
  * 이벤트 스트림은 영훤히 지속되거나 아니면 `onComplete` 콜백을 통해 더 이상의 데이터가 없고 종료됨을 알릴 수 있으며 또는 `Publisher`에 장애가 발생했을 때는
   `onError`를 호출할 수 있다.

#### Subscription

* `Subscriber`가 `Publisher`에 자신을 등록할 때 `Publisher`는 처음으로 `onSubscribe` 메서드를 호출해 `Subscription` 객체를 전달한다.
```java
public interface Subscription {
    void request(long n);
    void cancel();
}
```
* `Subscription`은 첫 번째 메서드로 `Publisher`에게 주어진 개수의 이벤트를 처리할 준비가 되었음을 알릴 수 있다.
* 두 번째 메서드로는 `Subscription`을 취소, 즉 `Publisher`에게 더 이상 이벤트를 받지 않음을 통지한다.

### Flow 인터페이스 협력 규칙 집합

* `Publisher`는 반드시 `Subscription`의 `request` 메서드에 정의된 개수 이하의 요소만 `Subscriber`에 전달해야 한다. 하지만 `Publisher`는
 지정된 개수보다 적은 수의 요소를 `onNext`로 전달할 수 있으며 동작이 성공적으로 끝났으면 `onComplete`를 호출하고 문제가 발생하면 `onError`를 호출해 
 `Subscription`을 종료할 수 있다.
<br></br>
* `Subscriber`는 요소를 받아 처리할 수 있음을 `Publisher`에 알려야 한다. 이런 방식으로 `Subscriber`는 `Publisher`에 역압력을 행사할 수 있고 `Subscriber`가
 관리할 수 없이 너무 많은 요소를 받는 일을 피할 수 있다. 더욱이 `onComplete`나 `onError` 신호를 처리하는 상황에서 `Subscriber`는 
 `Publisher`나 `Subscription`의 어떤 메서드도 호출할 수 없으며 `Subscription`이 취소되었다고 가정해야 한다. 마지막으로 `Subscriber`는 `Subscription.request()`
 메서드 호출이 없이도 언제든 종료 시그널을 받을 준비가 되어있어야 하며 `Subscription.cancel()`이 호출된 이후에라도 한 개 이상의 `onNext`를 받을 준비가 되어있어야 한다.
<br></br>
* `Publisher`와 `Subscriber`는 정확하게 `Subscription`을 공유해야 하며 각각이 고유한 역할을 수행해야 한다. 그러려면 `onSubscribe`와 `onNext`
 메서드에서 `Subscriber`는 `request` 메서드를 동기적으로 호출할 수 있어야 한다. 표준에서는 `Subscription.cancel()` 메서드는 몇 번을 호출해도
 한 번 호출한 것과 같은 효과를 가져야 하며, 여러 번 이 메서드를 호출해도 다른 추가 호출에 별 영향이 없도록 스레드에 안전해야 한다고 명시한다. 같은
 `Subscriber` 객체에 다시 가입하는 것은 권장하지 않지만 이런 상황에서 예외가 발생해야 한다고 명세서가 강제하진 않는다. 이전의 취소된 가입이 영구적으로 적용되었다면
 이후의 기능에 영향을 주지 않을 가능성도 있기 때문이다.

## 리액티브 라이브러리 RxJava 사용하기

* `RxJava`는 자바로 리액티브 애플리케이션을 구현하는 데 사용하는 라이브러리다.

### Observable 만들고 사용하기

* `Observable, Flowable` 클래스는 다양한 종류의 리액티브 스트림을 편리하게 만들 수 있도록 여러 팩토리 메서드를 제공한다.
* `just()` 팩토리 메서드는 한 개 이상의 요소를 이용해 이를 방출하는 `Observable`로 변환한다.
* `Observable`의 구독자는 `onNext("first"), onNext("second"), onComplete()`의 순서로 메시지를 받는다.
* 사용자와 실시간으로 상호작용하면서 지정된 속도로 이벤트를 방출하는 상황에서 유용하게 사용할 수 있는 다른 `Observable` 팩토리 메서드 `interval`도 있다.
  * ```java
    Observable<Long> onePerSec = Observable.interval(1, TimeUnit.SECONDS);
    ```
* 0에서 시작해 1초 간격으로 long 형식의 값을 무한으로 증가시키며 값을 방출한다. 매 초마다 온도 보고를 방출하는 다른 `Observable`을 `onePerSec`로 대신할 것이다.
```java
public interface Observer<T> {
    void onSubscribe(Disposavle d);
    void onNext(T t);
    void onError(Throwable t);
    void onComplete();
}
```

* `RxJava`의 API는 자바 9 네이티브 플로 API보다 유연하다(많은 오버로드된 기능을 제공).
* 즉 이벤트를 수신하는 `Consumer`의 `onNext` 메서드만 구현하고 나머지 완료, 에러 처리 메서드는 아무것도 하지 않는 기본 동작을 가진 `Observer`를
만들어 `Observable`에 가입할 수 있다.
```java
onPerSec.subscribe(i -> System.out.println(TempInfo.fetch("New York")));
```

### Observable을 변환하고 합치기

* `RxJava`나 기타 리액티브 라이브러리는 자바 9 플로 API에 비해 스트림을 합치고, 만들고, 거르는 등의 풍부한 도구상자를 제공하는 것이 장점이다.
* 이런 변환, 합치기 함수는 상당히 복잡하므로 말로 설명하기는 상당히 어렵다. 리액티브 스트림 커뮤니티는 마블 다이어그램이라는 시각적 방법을 이용해 이런 어려움을 해결하고자 노력한다.
* **마블 다이어그램**은 수평선으로 표시된 리액티브 스트림에 임의의 순서로 구성된 요소가 기하학적 모형이 나타난다. 특수 기호는 에러나 완료 신호를 나타낸다.
 박스는 해당 연산이 요소를 어떻게 변화하거나 여러 스트림을 어떻게 합치는지 보여준다.
