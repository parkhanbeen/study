# 람다를 이용한 도메인 전용 언어

## 도메인 전용 언어(DSL:domain-specific language)

* 특정 비즈니스 도메인의 문제를 해결하려고 만든 언어다.
* DSL이란 특정 비즈니스 도메인을 인터페이스로 만든 API라고 생각할 수 있다.
* DSL은 범용 프로그래밍 언어가 아니다. DSL에서 동작과 용어는 특정 도메인에 국한되므로 다른 문제는 걱정할 필요가 없고 오직 자신의 앞에
 놓인 문제를 어떻게 해결할지에만 집중할 수 있다.
* 두 가지 필요성을 생각하면서 DSL을 개발해야 한다.
  * **의사 소통의 왕** : 우리의 코드의 의도가 명확히 전달되어야 하며 프로그래머가 아닌 사람도 이해할 수 있어야 한다. 이런 방식으로 코드가
   비즈니스 요구사항에 부합하는지 확인할 수 있다.
  * **한 번 코드를 구현하지만 여러번 읽는다** : 가독성은 유지보수의 핵심이다. 즉 항상 우리의 동료가 쉽게 이해할 수 있도록 코드를 구현해야 한다.

### DSL의 장점과 단점

#### 장점

* **간결함** : API는 비즈니스 로직을 간편하게 캡슐화하므로 반복을 피할 수 있고 코드를 간결하게 만들 수 있다.
* **가독성** : 도메인 영역의 용어를 사용하므로 비 도메인 전문가도 코드를 쉽게 이해할 수 있다. 결과적으로 다양한 조직 구성원 간에 코드와 도메인 영역이 공유될 수 있다.
* **유지보수** : 잘 설계된 DSL로 구현한 코드는 쉽게 유지보수하고 바꿀 수 있다. 유지보수는 비즈니스 관련 코드 즉 가장 빈번히 바뀌는 애플리케이션 부분에 특히 중요하다.
* **높은 수준의 추상화** : DSL은 도메인과 같은 추상화 수준에서 동작하므로 도메인의 문제와 직접적으로 관련되지 않은 세부 사항을 숨긴다.
* **집중** : 비즈니스 도메인의 규칙을 표현할 목적으로 설계된 언어이므로 프로그래머 가 특정 코드에 집중할 수 있다. 결과적으로 생산성이 좋아진다.
* **관심사분리(Separation of concerns)** : 지정된 언어로 비즈니스 로직을 표현함으로 애플리케이션의 인프라구조와 관련된 문제와 독립적으로
 비즈니스 관련된 코드에서 집중하기 용이하다. 결과적으로 유지보수가 쉬운 코드를 구현한다.

#### 단점

* **DSL 설계의 어려움** : 간결하게 제한적인 언어에 도메인 지식을 담는 것이 쉬운 작업은 아니다.
* **개발 비용** : 코드에 DSL을 추가하는 작업은 초기 프로젝트에 많은 비용과 시간이 소모되는 작업이다. 또한 DSL 유지보수와 변경은 프로젝트에 부담을 주는 요소다.
* **추가 우회 계층** : DSL은 추가적인 계층으로 도메인 모델을 감싸며 이 때 계층을 최대한 작게 만들어 성능 문제를 회피한다.
* **새로 배워야 하는 언어** : 요즘에는 한 프로젝트에도 여러가지 언어를 사용하는 추세다. 하지만 DSL을 프로젝트에 추가하면서 팀이 배워야 하는 언어가 한 개 더 
 늘어난다는 부담이 있다. 여러 비즈니스 도메인을 다루는 개별 DSL을 사용하는 상황이라면 이들을 유기적으로 동작하도록 합치는 일은 쉬운 일이 아니다.
 개별 DSL이 독립적으로 진화할 수 있기 때문이다.
* **호스팅 언어 한계** : 일부 자바 같은 범용 프로그래밍 언어는 장황하고 엄격한 문법을 가졌다. 이런 언어로는 사용자 친화적 DSL을 만들기가 힘들다. 사실 장황한
 프로그래밍 언어를 기반으로 만든 DSL은 성가신 문법의 제약을 받고 읽기가 어려워진다. 자바 8의 람다 표현식은 이 문제를 해결할 강력한 새 도구다.

### JVM에서 이용할 수 있는 다른 DSL 해결책

* DSL 카테고리를 구분하는 가장 흔한 방법은 내부 DSL, 외부 DSL을 나누는 것이다.
* 내부 DSL(임베디드 DSL이라고 불림)은 순수 자바 코드같은 기존 호스팅 언어를 기반으로 구현하는 반면, 스탠드어론(standalone)이라 불리는 
 외부 DSL은 호스팅 언어와 독립적으로 자체의 문법을 가진다.
* 더욱이 JVM으로 인해 내부 DSL과 외부 DSL의 중간 카테고리에 해당하는 DSL이 만들어질 가능성이 생겼다. 스칼라나 그루비처럼 자바가 아니지만 JVM에서 실행되며
 더 유연하고 표현력이 강력한 언어도 있다. 우리는 이들을 다중 DSL이라는 세 번째 카테고리로 칭한다.

#### 내부 DSL

* 내부 DSL이란 자바로 구현한 DSL을 의미한다. 람다 표현식이 등장하면서 읽기 쉽고, 간단하고, 표현력 있는 DSL을 만들 수 있다.
* 자바 문법이 큰 문제가 아니라면 순수 자바로 DSL을 구현함으로 다음과 같은 장점을 얻을 수 있다.
  * 기존 자바 언어를 이용하면 외부 DSL에 비해 새로운 패턴과 기술을 배워 DSL을 구현하는 노력이 현저하게 줄어든다.
  * 순수 자바로 DSL을 구현하면 나머지 코드와 함께 DSL을 컴파일할 수 있다. 따라서 다른 언어의 컴파일러를 이용하거나 외부 DSL을 만드는 도구를
   사용할 필요가 없으므로 추가로 비용이 들지 않는다.
  * 여러분의 개발 팀이 새로운 언어를 배우거나 또는 익숙하지 않고 복잡한 외부 도구를 배울 필요가 없다.
  * DSL 사용자는 기본의 자바 IDE를 이용해 자동 완성, 자동 리팩터링 같은 기능을 그대로 즐길 수 있다. 최신 IDE는 다른 유명한 JVM 언어도 지원하지만
   자바 만큼의 기능을 지원하진 못한다.
  * 한 개의 언어로 한 개의 도메인 또는 여러 도메인을 대응하지 못해 추가로 DSL을 개발해야 하는 상황에서 자바를 이용한다면 추가 DSL을 쉽게 합칠 수 있다.
* 같은 자바 바이트코드를 사용하는 JVM 기반 프로그래밍 언어를 이용함으로 DSL 합침 문제를 해결하는 방법도 있다. 이런 언어를 다중 DSL이라고 부른다.

#### 다중 DSL

* 요즘 JVM에서 실행되는 언어는 100개가 넘는다. 스칼라, 루비처럼 유명한 언어라면 쉽게 개발자를 찾을 수 있다.
* DSL은 기반 프로그래밍 언어의 영향을 받으므로 간결한 DSL을 만드는데 새로운 언어의 특성들이 아주 중요하다.
* 이러한 DSL은 친화적이지만 다음과 같은 불편함도 초래한다.
  * 새로운 프로그래밍 언어를 배우거나 또는 팀의 누군가가 이미 해당 기술을 가지고 있어야 한다.
  * 두 개 이상의 언어가 혼재하므로 여러 컴파일러로 소스를 빌드하도록 빌드 과정을 개선해야 한다.
  * JVM에서 실행되는 거의 모든 언어가 자바와 백 퍼센트 호환을 주장하고 있지만 자바와 호환성이 완벽하지 않을 때가 많다.
   이런 호환성 때문에 성능이 손실될 때도 있다.

#### 외부 DSL

* 자신만의 문법과 구문으로 새 언어를 설계해야 한다. 새 언어를 파싱하고, 파서의 결과를 분석하고, 외부 DSL을 실행할 코드를 만들어야 한다.
* 외부 DSL을 개발하는 가장 큰 장점은 외부 DSL이 제공하는 무한한 유연성이다. 우리에게 필요한 특성을 완벽하게 제공하는 언어를 설계할 수 있다는 것이 장점이다.
* 자바로 개발된 인프라구조 코드와 외부 DSL로 구현한 비즈니스 코드를 명확하게 분리한다는 거도 장점이다.
* 하지만 이 분리로 인해 DSL고 호스트 언어 사이에 인공 계층이 생기므로 이는 양날의 검과 같다.

## 최신 자바 API의 작은 DSL

* 자바의 새로운 기능의 장점을 적용한 첫 API는 네이티브 자바 API 자신이다. 자바 8 이전의 네이티브 자바 API는 이미 한 개의 추상 메서드를 가진 인터페이스를
 갖고 있었다. 
* 하지만 무명 내부 클래스를 구현하려면 불필요한 코드가 추가되어야 하지만 람다와 메서드 참조가 등장하면서 게임의 규칙이 바뀌었다.
```java
// 내부 클래스를 이용했던 방법
Collections.sort(persons, new Comparator<Person>() {
    public int compare(Person p1, Person p2) {
        return p1.getAge() - p2.getAge();
    }
})

// 람다 표현식
Collections.sort(people, (p1, p2) -> p1.getAge() - p2.getAge());

// 정적 유틸리티 메서드 사용
Collections.sort(persons, comparing(p -> p.getAge()));

// 람다를 메서드 참조로
Collections.sort(persons, comparing(person::getAge));

// 만약 역순으로 정렬하고 싶은 경우
Collections.sort(persons, comparing(person::getAge).reverse());

// 나이가 같을 경우 이름을 알파벳 순으로 정렬하고 싶은 경우
    Collections.sort(persons, comparing(person::getAge)
    .thenComparing(Person::getName));
    
// List 인터페이스에 추가된 sort 메서드 이용
persons.sort(comparing(Person::getAge)
    .thenComparing(Person::getName));
```

### 스트림 API는 컬렉션을 조작하는 DSL

* `Stream`은 컬렉션의 항목을 필터, 정렬, 변환, 그룹화, 조작하는 작지만 강력한 DSL로 볼 수 있다.
* 스트림 API의 플루언트 형식은 잘 설계된 DSL의 또 다른 특징이다. 모든 중간 연산은 게으르며 다른 연산으로 파이프라인될 수 있는 스트림으로 반환된다.
 최종 연산은 적극적이며 전체 파이프라인이 계산을 일으킨다.

## 자바로 DSL을 만드는 패턴과 기법

* DSL은 특정 도메인 모델에 적용할 친화적이고 가독성 높은 API를 제공한다.

### 메서드 체인

* 메서드 체인 방법을 이용하면 한 개의 메서드 호출 체인으로 비즈니스를 정의할 수 있다.
* 이러한 접근 방법은 정적 메서드 사용을 최소화하고 메서드 이름이 인수의 이름을 대신하도록 만듦으로 DSL 가독성을 개선하는 효고ㅘ를 더한다.
* 이런 기법을 적용한 플루언트 DSL엔느 분법적 잡음이 최소화된다.
* 단점은 빌더를 구현해야 한다. 상위 수준의 빌더를 하위 수준의 빌더와 연결할 접착 많은 접착 코드가 필요하다.
* 도메인의 객체의 중첩 구조와 일치하게 들여쓰기를 강제하는 방법이 없다는 것도 단점이다.

### 중첩된 함수 이용

* 중첩된 함수 DSL 패턴은 이름에서 알 수 있듯이 다른 함수 안에 함수를 이용해 도메인 모델을 만든다.
* 메서드 체인에 비해 함수의 중첩 방식이 도메인 객체 계층 구조에 그대로 반영된다는 것이 장점이다.
* 문제점은 결과 DSL에 더 많은 괄호를 사용해야 한다는 사실이다. 더욱이 인수 목록을 정적 메서드에 넘겨줘야 한다는 제약도 있다.
* 도메인 객체에 선택 사항 필드가 있으면 인수를 생략할 수 있으므로 이 가능성을 처리할 수 있도록 여러 메서드 오버라이드를 구현해야 한다.
* 인수의 의미가 이름이 아니라 위치에 의해 정의된다.

### 람다 표현식을 이용한 함수 시퀀싱

* 이 패턴은 이전 두 가지 DSL 형식의 두 가지 장점을 더한다. 
  * 메서드 체인 패턴처럼 플루언트 방식으로 거래 주문을 정의할 수 있다.
  * 또한 중첩 함수 형식처럼 다양한 람다 표현식의 중첩 수준과 비슷하게 도메인 객체의 계층 구조를 유지한다.
* 단점은 많은 설정 코드가 필요하며 DSL 자체가 자바8 람다 표현식 문법에 의한 잡음의 영향을 받는다는 것이다.
* 어떤 DSL 형식을 사용할 것인지는 각자의 기호에 달렸다. 자신이 만들려는 도메인 언어에 어떤 도메인 모델이 맞는지 찾으려면 실험을 해봐야 한다.

### 조합하기

* 살펴본 세가지 DSL 패턴 각자가 장단점을 갖고 있다. 하지만 한 DSL에 한 개의 패턴만 사용하라는 법은 없다.
* 세 가지 DSL 패턴을 혼용해 가독성 있는 DSL을 만들 수 있지만 이 기법에도 결점이 있다.
* 결과 DSL이 여러 가지 기법을 혼용하고 있으므로 한 가지 기법을 적용한 DSL에 비해 사용자가 DSL을 배우는데 오랜 시간이 걸린다는 것이다.

### DSL 패턴의 장점과 단점

 패턴 이름          | 장점                                                                                                                                                   | 단점                                                                                   
----------------|------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------|
| 메서드 체인         | - 메서드 이름이 키워드 인수 역할을 한다. <br/> - 선택형 파라미터와 잘 동작한다. <br/> - DSL 사용자가 정해진 순서로 메서드를 호출하도록 강제할 수 있다.<br/> - 정적 메서드를 최소화하거나 없앨 수 있다.<br/> - 문법적 잡을 최소화한다. | - 구현이 장황하다.<br/> - 빌드를 연결하는 접착 코드가 필요하다.<br/> - 들여쓰기 규칙으로만 도메인 객체 계층을 정의한다.          |
| 중첩 함수          | - 구현의 장황함을 줄일 수 있다.<br/> - 함수 중첩으로 도메인 객체 계층을 반영한다.                                                                                                  | - 정적 메서드의 사용이 빈번하다.<br/> - 이름이 아닌 위치로 인수를 정의한다.<br/> - 선택형 파라미터를 처리할 메서드 오버로딩이 필요하다. |
| 람다를 이용한 함수 시퀀싱 | - 선택형 파라미터와 잘 동작한다.<br/> - 정적 메서드를 최소화하거나 없앨 수 있다.<br/> - 람다 중첩으로 도메인 객체 계층을 반영한다.<br/> - 빌더의 접착 코드가 없다.                                             | - 구현이 장황하다.<br/> - 람다 표현식으로 인한 문법적 잡음이 DSL에 존재한다.                                    |

## 자바8 DSL

* SQL 매핑 도구, 동작 주도 개발 프레임워크, 엔터프라이즈 통합 패턴을 구현하는 도구 세 가지 자바 라이브러리를 확인해보자.

### JOOQ

* `JOOQ`는 SQL을 구현하는 내부적 DSL로 자바에 직접 내장된 형식 안전 언어다.
* 데이터베이스 스키마를 역공학하는 소스코드 생성기 덕분에 자바 컴파일러가 복잡한 `SQL` 구문의 형식을 확인할 수 있다.

```sql
select * from book
where published_in = 2016
order by title;
```

```java
// JOOQ DSL
create.selectFrom(BOOK)
    .where(BOOK.PUBLISHED_IN.eq(2016))
    .orderBy(BOOK.TITLE)
```

* 스트림 API와 조합해 사용할 수 있다는 것이 JOOQ DSL의 또다른 장점이다.
```java
Class.forName("org.h2.Driver");
// sql 데이터베이스 연결 만들기
try (Connection c = getConnection("jdbc:h2:~/sql-goodies-with-mapping", "sa", "")) {
    DSL.using(c)
    .select(BOOK.AUTHOR, BOOK.TITLE)  // 만들어진 데이터베이스 연결을 이용해 jOOQ SQL 문 시작
    .where(BOOK.PUBLISHED_IN.eq(2016))
    .orderBy(BOOK.TITLE)
    .fetch()
    .stream()
    .collect(groupingBy(      // 스트림 API로 데이터베이스에서 가져온 데이터 처리 시작
        r -> r.getValue(BOOK.AUTHOR),
    LinekdHashMap::new,
    mapping(r -> r.getValue(BOOK.TITLE), toList())))
    .forEach((author, titles) -> 
    System.out.println(author + " is author of " + titles));
}
```

### 큐컴버

* 둥작 주도 개발(Behavior-driven development(BDD))은 테스트 주도 개발의 확장으로 다양한 비즈니스 시나리오를 구조적으로 서술하는 간단한 도메인 전용 슼크립팅
 언어를 사용한다.
* 큐컴버는 다른 BDD 프레임워크와 마찬가지로 이들 명령문을 실행할 수 있는 테스트 케이스로 변환한다.
* 결과적으로 이 개발 기법으로 만든 스크립트 결과물은 실행할 수 있는 테스트임과 동시에 비즈니스 기능의 수용 기준이 된다.
* BDD는 우선 순위에 따른, 확인할 수 있는 비즈니스 가치를 전달하는 개발 노력에 집중하며 비즈니스 어휘를 공유함으로 도메인 전문가와 프로그래머 사이의 간격을 줄인다.
* 개발자가 비즈니스 시나리오를 평문 영어로 구현할 수 있도록 도와주는 BDD 도구인 큐컴버를 이용한 실용적인 예제를 통해 이 추상적 개념을 조금 더 명확하게 정리할 수 있다.
* 큐컴버는 세 가지 구분되는 개념을 사용한다.
  * 전제 조건 정의(Given)
  * 시험하려는 도메인 객체의 실질 호출(When)
  * 테스트 케이스의 결과를 확인하는 어설션(Then)

```java
import java.util.HashMap;

public class BuyStocksStep {
    private Map<String, Integer> stockUnitPrices = new HashMap<>();
    private Order order = new Order();
    
    @Given("^the price of a \"(.*?)\" stock is (\\d+)\\$$")  // 시나리오의 전제 조건인 주식 단가 정의
    public void setunitPrice(String stockName, int unitPrice) {
        stockUnitValues.put(stockName, unitPrice);  // 주식 단가 저장
    }
    
    @When("^I buy (\\d+) \"(.*?)\"$")
    public void buyStocks(int quantity, String stockName) {
        Trade trade = new Trade();
        trade.setType(Trade.Type.BUY);
        
        Stock stock = new Stock();
        stock.setSymbol(stockName);
        
        trade.setStock(stock);
        trade.setPrice(stockUnitPrices.get(stockName));
        trade.setQuantity(quantity);
        order.addTrade(trade);
    }
    
    @Then("^the order value should be (\\d+)\\$$")
    public void checkOrderValue(int expectedValue) {   // 예상되는 시나리오 결과 정의
        assertEquals(expectedValue, order.getValue());
    }
}

// 람다 표현식 이용
public class BuyStockSteps implements cucumber.api.java8.En {
    private Map<String, Integer> stockUnitPrices = new HashMap<>();
    private Order order = new Order();
    public BuyStocksSteps() {
        Given("^the price of a \"(.*?\" stock is (\\d+)\\$$",
            (String stockName, int unitPrice) -> {
                stockUnitValues.put(stockName, unitPrice);
            });
        // ....
    }
}
```

### 스프링 통합

* **스프링 통합(Spring Integration)** 은 유명한 엔터프라이즈 통합 패턴을 지원할 수 있도록 의존성 주입에 기반한 스프링 프로그래밍 모델을 확장한다.
* 스프링 통합의 핵심 목표는 복잡한 엔터프라이즈 통합 솔루션을 구현하는 단순한 모델을 제공하고 비동기, 메시지 주도 아키텍처를 쉽게 적용할 수 있게 돕는 것이다.
* 스프링 통합은 스프링 기반 애플리케이션 내의 경량의 원격, 메시징, 스케쥴링을 지원한다.
  * 단비같은 풍부하고 유창한 DSL을 통해 기존의 스프링 XML 설정 파일 기반에도 이들 기능을 지원한다.
* 스프링 통합은 채널, 엔드포인트, 폴러, 채널 인터셉터등 메시지 기반의 애플리케이션에 필요한 가장 공통 패턴을 모두 구현한다.

```java
import java.beans.BeanProperty;

@Configuration
@EnableIntegration
public class MyConfiguration {
    
    @Bean
    public MessageSource<?> integerMessageSource() {
        MethodInvokingMessageSource source = new MethodInvokingMessageSource(); 
        source.setObject(new AtomicInteger());
        source.setMethodName("getAndIncrement");
        return source;
    }
    
    @Bean
    public DirectChannel inputChannel() {
        return new DIrectChannel();
    }
    
    @Bean
    public IntegrationFlow myFlow() {
        return IntegrationFlows
            .from(this.integerMessageSource(),
                c -> c.poller(Pollers.fixedRate(10)))
            .channel(this.inputChannel())
            .filter((Integer p) -> p % 2 == 0)
            .transform(Object::toString)
            .channel(MessageChannels.queue("queueChannel"))
            .get();
    }
}
```
