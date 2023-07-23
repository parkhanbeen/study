# CompletableFuture : 안정적 비동기 프로그래밍

## Future의 단순 활용

* 자바 5부터는 미래의 어느 시점에 결과를 얻는 모델에 활용할 수 있도록 `Future` 인터페이스를 제공하고 있다.
* 비동기 계산을 모델링하는 데 `Future`를 이용할 수 있으며, `Future`는 계산이 끝났을 때 결과에 접근할 수 있는 참조를 제공한다. 시간이 걸릴 수 있는
 작업을 `Future` 내부로 설정하면 호출자 스레드가 결과를 기다리는 동안 다른 유용한 작업을 수행할 수 있다.
* `Future`는 저수준의 스레드에 비해 직관적으로 이해하기 쉽다는 장점이 있다.
* `Future`를 이용하려면 시간이 오래 걸리는 작업을 `Callable` 객체 내부로 감싼 다음에 `ExecutorService`에 제출해야 한다.

```java
// Future로 오래 걸리는 작업을 비동적으로 실행하기
ExcutorService executor = Executors.newCachedThreadPool();
Future<Double> future = executor.submit(new Callable<Double>() {
    public Double call() {
        return doSomeLongComputation();  // 시간이 오래걸리는 작업은 다른 스레드에서 비동기적으로 실행
    }
});
doSomethingElse();   // 비동기 작업을 수행하는 동안 다른 작업을 한다.
try {
    Double result = future.get(1, TimeUnit.SECONDS);  // 비동기 작업의 결과를 가져온다.
} catch (ExecutionException ee) {                     // 결과가 준비되어 있지 않으면 호출 스레드가 블록된다.
    // 계산 중 예외 발생                                  // 하지만 최대 1초까지만 기다린다.
} catch (InterruptedException ie) {
    // 현재 스레드에서 대기 중 인터럽트 발생
} catch (TimeoutException te) {
    // Future가 완료되기 전에 타임아웃 발생
}
```

* `ExecutorService`에서 제공하는 스레드가 시간이 오래 걸리는 작업을 처리하는 동안 우리 스레드로 다른 작업을 동시에 실행할 수 있다.
* 다른 작업을 처리하다가 시간이 오래 걸리는 작업의 결과가 필요한 시점이 되었을 때 `Future`의 `get` 메서드로 결과를 가져올 수 있다.
* `get` 메서드를 호출했을 때 이미 계산이 완료되어 결과가 준비되었다면 즉시 결과를 반환하지만 결과가 준비되지 않았다면 작업이 완료될 때까지 우리 스레드를 블록시킨다.
* 작업이 끝나지 않는 문제가 있을 수 잇으므로 `get` 메서드를 오버로드해서 우리 스레드가 대기할 최대 타임아웃 시간을 설정하는 것이 좋다.

### Future 제한

* `Future` 인터페이스가 비동기 계산이 끝났는지 확인할 수 있는 `isDone`메서드, 계산이 끝나길 기다리는 메서드, 결과 회수 메서드 등을 제공하지만
 이들 메서드만으로는 간결하 동시 실행 코드를 구현하기에 충분하지 않다.
* 다음과 같은 선언형 기능이 있다면 유용할 것이다.
  * 두 개의 비동기 계산 결과를 하나로 합친다. 두 가지 계산 결과는 서로 독립적일 수 있으며 또는 두 번째 결과가 첫 번째 결과에 의존하는 상황일 수 있다.
  * `Future` 집합이 실행하는 모든 태스크의 완료를 기다린다.
  * `Future` 집합에서 가장 빨리 완료되는 태스크를 기다렸다가 결과를 얻는다(예를 들어 여러 태스크가 다양한 방식으로 같은 결과를 구하는 상황)
  * 프로그램적으로 `Future`를 완료시킨다(즉, 비동기 동작에 수동으로 결과 제공).
  * `Future` 완료 동작에 반응한다(즉, 결과를 기다리면서 블록되지 않고 결과가 준비되었다는 알림을 받은 다음에 `Future`의 결과로 원하는 추가 동작을 수행할 수 있음)
* 자바 8에서 새로 제공하는 `CompletableFuture` 클래스(`Future` 인터페이스를 구현한 클래스)를 통해 `Stream`과 `CompletableFuture`는
 비슷한 패턴, 즉 람다 표현식과 파이프라이닝을 활용할 수 있다.

### 동기 API와 비동기 API

* **동기 API**
  * 에서는 메서드를 호출한 다음에 메서드가 계산을 완료할 때까지 기다렸다가 메서드가 반환되면 호출자는 반환된 값으로 계속 다른 동작을 수행한다.
  * 호출자와 피호출자가 각각 다른 스레드에서 실행되는 상황이었더라도 호출자는 피호출자의 동작 완료를 기다렸을 것이다. 이처럼 동기 API를 사용하는 상황을 
  **블록 호출**이라고 한다.
* **비동기 API**
  * 메서드가 즉시 반환되며 끝내지 못한 나머지 작업을 호출자 스레드와 동기적으로 실행될 수 있도록 다른 스레드에 할당한다. 이와 같은 비동기 API를 사용하는
   상황을 **비블록 호출**이라고 한다.
  * 다른 스레드에 할당된 나머지 계산 결과는 콜백 메서드를 호출해서 전달하거나 호출자가 `계산 결과가 끝날 때가지 기다림` 메서드를 추가로 호출하면서 전달된다.

## 비동기 API 구현

```java
public Future<Double> getPriceAsync(String product) {
    CompletableFuture<Double> futurePrice = new CompletableFuture<>();
    new Thread(() -> {
        double price = calculatePrice(product); // 다른 스레드에서 비동기적으로 계산
        futurePrice.complete(price); // 오랜 시간이 걸리는 계산이 완료되면 Future에 값을 설정
    }).start();
    return futurePrice;  // 계산 결과가 완료되길 기다리지 않고 Future를 반환
}

public static void main(String[]args){
    Shop shop = new Shop("BestShop");
    long start = System.nanoTime();
    Future<Double> futurePrice = shop.getPriceAsync("my favorite product");
    long invocationTime = ((System.nanoTime() - start) / 1_000_000);
    System.out.println("Invocation returned after " + invocationTime + " msecs");
    
    doSomethingElse();
    
    try {
        double price = futurePrice.get();
        System.out.printf("Price is %.2f%n", price);
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
    long retrievalTime = ((System.nanoTime() - start) / 1_000_000);
    System.out.println("Price returned after " + retrievalTime + " msecs");
}
```

### 에러 처리 방법

* 만약 위 코드 중 가격을 계산하는 동안 에러가 발생하면 어떻게 될까?
* 예외가 발생하면 해당 스레드에만 영향을 미친다. 즉, 에러가 발생해도 가격 계산은 계속 진행되며 일의 순서가 꼬인다. 결과적으로 클라이언트는 `get`
 메서드가 반환될 때까지 영원히 기다리게 될 수도 있다.
* 클라이언트는 타임아웃값을 받는 `get` 메서드의 오버로드 버전을 만들어 이 문제를 해결할 수 있다. 이처럼 블록 문제가 발생할 수 있는 상황에서는 타임아웃을 활용하는 것이 좋다.
* 하지만 이때 제품가격 계산이 왜 에러가 발생했는지 알 수 있는 방법이 없다. 따라서 `completeExceptionally` 메서드를 이용해 `CompletableFuture` 내부에서 발생한
 예외를 클라이언트로 전달해야 한다.

```java
// CompletableFuture 내부에서 발생한 에러 전파
public Future<Double> getPriceAsync(String product) {
    CompletableFuture<Double> futurePrice = new CompletableFuture<>();
    new Thread(() -> {
        try {
            double price = calculatePrice(product);
            futurePrice.complete(price); // 계산이 정상적으로 종료되면 Future에 가격 정보를 저장한 채로 Future를 종료
        } catch (Exception ex) {
            futurePrice.completeExceptionally(ex);  // 도중에 문제가 발생하면 발생한 에러를 포함시켜 Future를 종료
        }
    }).start();
    return futurePrice;
}
```

### 팩토리 메서드 supplyAsync로 CompletableFuture 만들기

* 간단하게 `CompletableFuture`를 만드는 방법을 알아보자.
```java
public Future<Double> getPriceAsync(String product) {
    return CompletableFuture.supplyAsync(() -> calculatePrice(product));
}
```

* `supplyAsync` 메서드는 `supplier`를 인수로 받아서 `CompletableFuture`를 반환한다.
* `CompletableFuture`는 `supplier`를 실행해서 비동기적으로 결과를 생성한다. `ForkJoinPool`의 `Executor` 중 하나가 `Supplier`를 실행할 것이다.
* 하지만 두 번째 인수를 받는 오버로드 버전의 `supplyAsync` 메서드를 이용해서 다른 `Executor`를 지정할 수 있다. 결국 
 모든 다른 `CompletableFuture`의 팩토리 메서드에 `Executor`를 선택적으로 전달할 수 있다.

## 비블록 코드 만들기

```java
// 병렬 스트림을 이용한 병렬 처리
public List<String> findPrices(String product) {
    return shops.parallelStream()
    .map(shop -> String.format("%s price is %.2f", shop.getName(), shop.getPrice(product)))
    .collect(toList());
}

// CompletableFuture로 구현
public List<String> findPrices(String product) {
    List<CompletableFuture<String>> priceFutures = shops.stream()
    .map(shop -> CompletableFuture.supplyAsynce(                        // CompletableFuture로 각각의 가격을 비동기적으로 계산
        () -> shop.getName() + " price is " + shop.getPrice(product)))
    .collect(Collectors.toList());
    
    return priceFutures.stream()
    .map(CompletableFuture::join)  // 모든 비동기 동작이 끝나길 기다림
    .collect(toList());
}
```

* `CompletableFuture`를 리스트로 모은 다음에 다른 작업과는 독립적으로 각자의 작업을 수행한다.
* 두 가지 버전 모두 내부적으로 `Runtime.getRuntime().availableProcessors()`가 반환하는 스레드 수를 사용하면서 비슷한 결과가 된다.
* `CompletableFuture`는 병렬 스트림 버전에 비해 작업에 이용할 수 있는 다양한 `Excutor`를 지정할 수 있다는 장점이 있다.
* 따라서 `Excutor`로 스레드 풀의 크기를 조절하는 등 애플리케이션에 맞는 최적화된 설정을 만들 수 있다.

### 커스텀 Excutor 사용하기

#### 스레드 풀 크기 조절

* 스레드 풀이 너무 크면 CPU와 메모리 자원을 서로 경쟁하느라 시간 낭비할 수 있다.
* 반면 스레드 풀이 너무 작으면 CPU의 일부 코어는 활용되지 않을 수 있다.

```
N(threads) = N(CPU) * U(CPU) * (1 + W/C)
N(CPU)는 Runtime.getRunTime().availableProcessors()가 반환하는 코어 수
U(CPU)는 0과 1 사이의 값을 갖는 CPU 활용 비율
W/C는 대기시간과 계산시간의 비율
```

#### 스트림 병렬화와 CompletableFuture 병렬화

* I/O가 포함되지 않은 계산 중심의 동작을 실행할 때는 스트림 인터페이스가 가장 구현하기 간단하며 효율적일 수 있다(모든 스레드가 계산 작업을 수행하는
 상황에서는 프로세서 코어수 이상의 스레드를 가질 필요가 없다).
* 반면 작업이 I/O를 기다리는 작업을 병렬로 실행할 때는 `CompletableFuture`가 더 많은 유연성을 제공하며 대기/계산(W/C)의 비율에 적합한 스레드 수를
 설정할 수 있다. 특히 스트림의 게으른 특성 때문에 스트림에서 I/O를 실제로 언제 처리할지 예측하기 어려운 문제도 있다.

## 비동기 작업 파이프라인 만들기

* 자바 8의 `CompletableFuture` API는 이와 같이 두 비동기 연산을 파이프라인으로 만들 수 있도록 `thenCompose` 메서드를 제공한다.
* `thenCompose` 메서드는 첫 번째 연산의 결과를 두 번째 연산으로 호출하고 `Function`에 넘겨주는 식으로 두 `CompletableFuture`를 조합할 수 있다.
* `Function`은 첫 번째 `CompletableFuture` 반환 결과를 인수로 받고 두 번째 `CompletableFuture`를 반환하는데, 두 번째 `CompletableFuture`는 첫 번째
 `CompletableFuture`의 결과를 계산의 입력으로 샤용한다.
* 실전에서는 독립적으로 실행된 두 개의 `CompletableFuture` 결과를 합쳐야 한느 상황이 종종 발생한다.
* 물론 첫 번째 `CompletableFuture`의 동작 완료와 관계없이 두 번째 `CompletableFuture`를 실행할 수 있어야 한다.
* 이런 상황에서는 `thenCombine` 메서드를 사용한다. `thenCombine` 메서드는 `BiFunction`을 두 번째 인수로 받는다.
* `BiFunction`은 두 개의 `CompletableFuture` 결과를 어떻게 합칠지 정의한다. `thenCompose`와 마찬가지로 `thenCombine` 메서드에도 `Async` 버전이 존재한다.
* `thenCombineAsync` 메서드에서는 `BiFunction`이 정의하는 조합 동작이 스레드 풀로 제출되면서 별도의 태스크에서 비동기적으로 수행된다.

### 타임아웃 효과적으로 사용하기

* `Future`의 계산 결과를 읽을 때는 무한정 기다리는 상황이 발생할 수 있으므로 블록을 하지 않는 것이 좋다.
* 자바 9에서는 `CompletableFuture`에서 제공하는 몇 가지 기능을 이용해 이런 문제를 해결할 수 있다.
* `orTimeout`메서드는 지정된 시간이 지난 후에 `CompletableFuture`를 `TimeoutException`으로 완료하면서 또 다른 `CompletableFuture`를 
 반환할 수 있도록 내부적으로 `ScheduledThreadExcecutor`를 활용한다.

```java
// CompletableFuture에 타임아웃 추가
Future<Double> futurePriceInUSD = CompletableFuture.supplyAsync(() -> shop.getPrice(product))
    .thenCombine(
        CompletableFuture.supplyAsync(
            () -> exchangeService.getRate(Money.EUR, Money.USD)),
        (price, rate) -> price * rate
    ))
    .orTimeout(3, TimeUnit.SECONDS);   // 3초뒤 작업이 완료되지 않으면 Future가 TimeoutException 발생하도록 설정
```

```java
// CompletableFuture에 타임아웃이 발생하면 기본값으로 처리
Future<Double> futurePriceInUSD = CompletableFuture.supplyAsync(() -> shop.getPrice(product))
    .thenCombine(
        CompletableFuture.supplyAsync(
        () -> exchangeService.getRate(Money.EUR, Money.USD))
        .completeOnTimeout(DEFAULT_RATE, 1, TimeUnit.SECONDS),   // 환전 서비스가 일 초 안에 결과를 제공하지 않으면 기본 환율값을 사용
        (price, rate) -> price * rate
    ))
    .orTimeout(3, TimeUnit.SECONDS);
```

## CompletableFuture의 종료에 대응하는 방법

* 실제 서비스는 얼마나 지연될지 예측하기 어렵다. 서버 부하에서 네트워크 문제에 이르기까지 다양한 지연 요소가 있기 때문이다. 또한 질의당 얼마를 더 지불하느냐에 따라
 우리 애플리케이션이 제공하는 서비스의 질이 달라질 수도 있다.
* 자바 8의 `CompletableFuture` API는 `thenAccept`라는 메서드는 연산 결과를 소비하는 `Consumer`를 인수로 받는다. `CompletableFuture`가 생성한 결과를
 어떻게 소비할지 미리 지정한다.
* `thenAcceptAsync` 메서드는 `CompletableFuture`가 완료된 스레드가 아니라 새로운 스레드를 이용해 `Consumer`를 실행한다. 
 불필요한 콘텍스트 변경응ㄴ 피하는 동시에 `CompletableFuture`가 완료되는 즉시 응답하는 것이 좋으므로 `thenAcceptAsync`를 사용하지 않는다(오히려 
 `thenAcceptAsync`를 사용하면 새로운 스레드를 이용할 수 있을 때까지 기다려야 하는 상황이 일어날 수 있다).
* 팩토리 메서드 `allOf`는 `CompletableFuture` 배열을 입력으로 받아 `CompletableFuture<void>`를 반환한다. 전달된 모든 `CompletableFuture`가
 완료되어야 `CompletableFuture<void>` 완료된다.
* 팩토리 메서드 `anyOf`는 `CompletableFuture` 배열을 입력받아 `CompletableFuture<Object>`를 반환한다. `CompletableFuture<Object>`는
 처음으로 완료한 `CompletableFuture`의 값으로 동작을 완료한다.
