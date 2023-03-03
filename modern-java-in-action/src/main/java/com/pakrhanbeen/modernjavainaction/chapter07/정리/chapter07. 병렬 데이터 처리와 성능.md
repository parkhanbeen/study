# 병렬 데이터 처리와 성능

자바 7에서 추가된 **포크/조인 프레임워크**를 이용해 더 쉽게 병렬화를 수행하면서 에러를 최소화할 수 있다.

## 병렬 스트림

* 컬렉션에 `parallelStream`을 호출하면 **병렬 스트림**이 생성된다.
* 병렬 스트림이란 각각의 스레드에서 처리할 수 있도록 스트림 요소를 여러 청크로 분할한 스트림이다.
* 따라서 병렬 스트림을 이용하면 모든 멀티코어 프로세서가 각각의 청크를 처리하도록 할당할 수 있다.

```java
public long sequentialSum(long n) {
    return Stream.iterate(1L, i -> i + 1)   // 무한 자연수 스트림 생성
        .limit(n) // n개 이하로 제한
        .reduce(0L, Long::sum);   // 모든 숫자를 더하는 스트림 리듀싱 연산
}

// 병렬 스트림으로 변환
public long parallelSum(long n) {
    return Stream.iterate(1L, i -> i + 1)
    .limit(n)
    .parallel()  // 스트림을 병렬 스트림으로 변환
    .reduce(0L, Long::sum);
}
```

* `parallel` 메서드를 호출하면 기존의 함수형 리듀싱 연산(숫자 합계 계산)이 병렬로 처리된다.
* 순차 스트림에 `parallel`을 호출해도 스트림 자체에는 아무 변화도 일어나지 않는다.
* 내부적으로 `parallel`을 호출하면 이후 연산이 병렬로 수행해야 함을 의미하는 불리언 플래그가 설정된다.
* 반대로 `sequential`로 병렬 스트림을 순차 스트림으로 바꿀 수 있다.
```java
stream.parallel()
    .filter(...)
    .sequential()
    .map(...)
    .parallel()
    .reduce();   
```
* `parallel, sequential` 두 메서드 중 최종적으로 호출된 메서드가 전체 파이프라인에 영향을 미친다.
  * 마지막 호출은 `parallel`이므로 파이프라인은 전체적으로 병렬로 실행된다.

> ### 병렬 스트림에서 사용하는 스레드 풀 설정
> * 병렬 스트림은 내부적으로 `ForkJoinPool`을 사용한다.
> * 기본적으로 `ForkJoinPool`은 프로세스 수, 즉 `Runtime.getRuntime().availableProcessors()`가 반환하는 값에 상응하는 스레드를 갖는다.
> ```java
> System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", 12);
> ```
> * 전역 설정 코드로 모든 병렬 스트림 연산에 영향을 준다.
> * 현재는 하나의 병렬 스트림에 사용할 수 있는 특정한 값을 지정할 수 없다.
> * 일반적으로 기기의 프로세서 수와 같으므로 특별한 이유가 없다면 `ForkJoinPool`의 기본값을 그대로 사용할 것을 권장한다.

### 스트림 성능 측정

* 