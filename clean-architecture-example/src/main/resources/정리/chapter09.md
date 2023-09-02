# 애플리케이션 조립하기

## 왜 조립까지 신경 써야 할까?

* 왜 유스케이스와 어댑터를 그냥 필요할 때 인스턴스화하면 안 되는 걸까?
  * 코드 의존성이 올바른 방향을 가리키게 하기 위해서다.
  * 모든 의존성은 안쪽으로, 애플리케이션의 도메인 코드 방향으로 향해야 도메인 코드가 바깥 계층의 변경으로부터 안전하다는 점을 기억하자.
* 우리의 객체 인스턴스를 생성할 책임은 누구에게 있을끼? 그리고 어떻게 의존성 규칙을 어기지 않으면서 그렇게 할 수 있을끼?
  * 해답은 아키텍처에 대해 중립적이고 인스턴스 생성을 위해 모든 클래스에 대한 의존성을 가지는 설정 컴포넌트가 있어야 한다는 것이다.
* 설정 컴포넌트는 우리가 제공한 조각들로 애플리케이션을 조립하는 것을 책임진다. 이 컴포넌트는 다음과 같은 역할을 수행해야 한다.
  * 웹 어댑터 인스턴스 생성
  * HTTP 요청이 실제로 웹 어댑터로 전달되도록 보장
  * 유스케이스 인스턴스 생성
  * 웹 어댑터에 유스케이스 인스턴스 제공
  * 영속성 어댑터 인스턴스 생성
  * 유스케이스에 영속성 어댑터 인스턴스 제공
  * 영속성 어댑터가 실제로 데이터베이스에 접근할 수 있도록 보장
* 더불어 설정 컴포넌트는 설정 파일이나 커맨드라인 파라미터 등과 같은 설정 파라미터의 소스에도 접근할 수 있어야 한다.
* 애플리케이션이 조립되는 동안 설정 컴포넌트는 이러한 파라미터를 애플리케이션이 조립되는 동안 설정 컴포넌트는 이러한 파라미터를 애플리케이션
 컴포넌트에 제공해서 어떤 데이터베이스에 접근하고 어떤 서버를 메일 전송에 사용할지 등의 행동 양식을 제어한다.
* 이것은 단일 책임원칙을 위반하지만 애플리케이션의 나머지 부분을 깔끔하게 유지하고 싶다면 이처럼 구성요소들을 연결하는 바깥쪽 컴포넌트가 필요하다.

## 평범한 코드로 조립하기

```java
public class Application {
    public static void main(String[] args) {
        AccountRepository accountRepository = new AccountRepository();
        ActivityRepository activityRepository = new ActivityRepository();

        AccountPersistenceAdapter accountPersistenceAdapter = new AccountPersistenceAdapter(accountRepository, activityRepository);

        SendMoneyUseCase sendMoneyUseCase = new SendMoneyService(
                accountPersistenceAdapter,
                accountPersistenceAdapter
        );

        SendMoneyController sendMoneyController = new SendMoneyController(sendMoneyUseCase);
        
        startProcessingWebRequests(sendMoneyController);
    }
}
```

* main 메서드 안에서 웹 컨트롤러부터 영속성 어댑터까지, 필요한 모든 클래스의 인스턴스를 생성한 후 함께 연결한다.
* 이 평법한 코드 방식은 애플리케이션을 조립하는 가장 기본적인 방법이다. 하지만 몇 가지 단점이 있다.
  1. 앞의 코드는 웹 컨트롤러, 유스케이스, 영속성 어댑터가 단 하나씩만 있는 애플리케이션을 예로 든 것이다. 완전한 엔터프라이즈 애플리케이션이라면 
   얼마나 많은 코드를 만들어야 할까..
  2. 각 클래스가 속한 패키지 외부에서 인스턴스를 생성하기 때문에 이 클래스들은 전부 `public`이어야 한다. 이렇게 되면 가령 유스케이스가
   영속성 어댑터에 직접 접근하는 것을 막지 못한다.
* 다행이도 `package-private` 의존성을 유지하면서 이처럼 지저분한 작업을 대신해줄 수 있는 의존성 주입 프레임워크 스프링이 있다.

## 스프링의 클래스패스 스캐닝으로 조립하기

* 스프링 프레임워크를 이용해서 애플리케이션을 조립한 결과물을 애플리케이션 컨텍스트라고 한다. 애플리케이션 컨텍스트는 애플리케이션을 구성하는 모든 객체(bean)를 포함한다.
* 스프링은 클래스패스 스캐닝으로 클래스패스에서 접근 가능한 모든 클래스를 확인해서 `@Compnent` 애너테이션이 붙은 클래스를 찾는다. 그러고 나서 
 이 애너테이션이 붙은 각 클래스의 객체를 생성한다.

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface PersistenceAdapter {

  @AliasFor(annotation = Component.class)
  String value() default "";
}
```

* 스프링이 인식할 수 있는 애너테이션을 직접 만들어 사용할 수 있다.
* 메타 에너테이션으로 `@Component`를 포함하고 있어서 스프링이 클래스패스 스캐닝을 할 때 인스턴스를 생성할 수 있도록 한다. 이 애너테이션 덕분에 코드를 읽는 
 사람들은 아키텍처를 더 쉽게 파악할 수 있다.
* 하지만 클래스패스 스캐닝 방식에는 단점이 있다.
  1. 클래스에 프레임워크에 특화된 애너테이션을 붙여야 한다는 점에서 침투적이다.
  2. 마법 같은 일이 일어날 수 있다. 스프링 전문가가 아니라면 원인을 찾는 데 수일이 걸릴 수 있는 숨겨진 부수효과를 야기할 수도 있다는 뜻이다.

## 스프링의 자바 컨피그로 조립하기

* 이 방식에서는 애플리케이션 컨텍스트에 추가할 빈을 생성하는 설정 클래스를 만든다.

```java
@Configuration
@EnableJpaRepositories
class PersistenceAdapterConfiguration {

    @Bean
    AccountPersistenceAdapter accountPersistenceAdapter(
        AccountRepository accountRepository,
        ActivityRepository activityRepository,
        AccountMapper accountMapper) {

        return new AccountPersistenceAdapter(
            accountRepository,
            activityRepository,
            accountMapper
        );
    }

    @Bean
    AccountMapper accountMapper() {
        return new AccountMapper();
    }
}
```

* `@Configuration` 애너테이션을 통해 이 클래스가 스프링의 클래스패스 스캐닝에서 발견해야 할 설정 클래스임을 표시해둔다. 그러므로 사실 
 클래스패스 스캐닝을 사용하고 있는 것이기는 하다.
* 하지만 모든 빈을 가져오는 대신 설정 클래스만 선택하기 때문에 해로운 마법이 일어날 확률이 줄어든다.
* 비슷한 방법으로 웹 어댑터, 혹은 애플리케이션 계층의 특정 모듈을 위한 설정 클래스를 만들면 특정 모듈만 포함하고, 그 외의 다른 모듈의 빈은
모킹해서 애플리케이션 컨텍스트를 만들 수 있다.
* 이렇게 하면 테스트에 큰 유연성이 생긴다. 심지어 리팩터링을 많이 하지 않고도 각 모듈의 코드를 자체 코드베이스, 자체 패키지, 자체 `JAR` 파일로 밀어넣을 수 있다.
* 또한 이 방식에서는 클래스패스 스캐닝 방식과 달리 `@Component` 애너테이션을 코드 여기 저기에 붙이도록 강제하지 않는다. 그래서 애플리케이션 계층을 스프링 프레임워크(그 외의
 어떤 프레임워크)에 대한 의존성 없이 깔끔하게 유지할 수 있다.
* 하지만 이 방법에도 문제가 있다. 설정 클래스가 생성하는 빈이 설정 클래스와 같은 패키지에 존재하지 않는다면 이 빈들을 `pulbic`으로 만들어야 한다.
* 가시성을 제한하기 위해 패키지를 모듈 경계로 사용하고 각 패키지 안에 전용 클래스를 만들 수는 있지만 하위 패키지를 사용할 수 없다.
