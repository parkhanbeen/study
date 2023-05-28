# 새로운 날짜와 시간 API

## LocalDate, LocalTime, Instant, Duration, Period 클래스

* `java.time` 패키지는 `LocalDate, LocalTime, LocalDateTime, Instant, Duration, Period`등 새로운 클래스를 제공한다.

### LocalDate와 LocalTime 사용

* 새로운 날짜와 시간 API를 사용할 때 처음 접하게 되는 것이 `LocalDate`다. `LocalDate` 인스턴스는 시간을 제외한 날짜를 표현하는 불변 객체다.
* 특히 `LocalDate` 객체는 어떤 시간대 정보도 포함하지 않는다.
* `of` 정적 팩터리 메서드로 인스턴스를 만들 수 있다.
```java
LocalDate date = LocalDate.of(2017, 9, 21);    // 2017-09-21
int year = date.getYear(); // 2017
Month month = date.getMonth();  // SEPTEMBER
int day = date.getDayOfMonth();   // 21
DayOfWeek dow = date.getDayOfWeek(); // THURSDAY
int len = date.lengthOfMonth();  // 31(3월의 일 수)
boolean leap = date.isLeapYear();  // false(윤년이 아님)

LocalDate today = LocalDate.now();
```

* `get` 메서드에 `TemporalField`를 전달해서 정보를 얻는 방법도 있다.
* `TemporalField`는 시간 관련 객체에서 어떤 필드의 값에 접근할지 정의하는 인터페이스다.
* 열거자 `ChronoField`는 `TemporalField` 인터페이스를 정의하므로 다음 코드에서 보여주는 것처럼 `ChronoField`의 열거자 요소를 이용해서 
 원하는 정보를 쉽게 얻을 수 있다.
```java
int year = date.get(ChronoField.YEAR);
int month = date.get(ChronoField.MONTH_OF_YEAR);
int day = date.get(ChronoField.DAY_OF_MONTH);
```

* 시간은 `LocalTime` 클래스로 표현할 수 있다.
```java
LocalTime time = LocalTime.of(13, 45, 20);  // 13:45:20
int hour = time.getHour();  // 13
int minute = time.getMinute();  // 45
int second = time.getSecond();  // 20
```

* 날짜와 시간 문자열로 `LocalDate`와 `LocalTime`의 인스턴스를 만드는 방법도 있다.
```java
LocalDate date = LocalDate.parse("2017-09-21");
LocalTime time = LocalTime.parse("13:45:20");
```

### 날짜와 시간 조합

* `LocalDateTime`은 `LocalDate`와 `LocalTime`을 쌍으로 갖는 복합 클래스다. 즉, `LocalDateTime`은 날짜와 시간을 모두 표현할 수 있다.
```java
// 2017-09-21T13:45:20
LocalDateTime dt1 = LocalDateTime.of(2017, Month.SEPTEMBER, 21, 13, 45, 20);
LocalDateTime dt2 = LocalDateTime.of(date, time);
LocalDateTime dt3 = date.atTime(13, 45, 20);
LocalDateTime dt4 = date.atTime(time);
LocalDateTIme dt5 = time.atDate(date);
```

### Instant 클래스: 기계의 날짜와 시간

* 사람은 보통 주, 날짜, 시간, 분으로 날짜와 시간을 계산한다. 하지만 기계에서는 이와 같은 단위로 시간을 표현하기가 어렵다. 
* 기계의 관점에서는 연속된 시간에서 특정 지점을 하나의 큰 수로 표현하는 것이 가장 자연스러운 시간 표현 방법이다.
* 새로운 `java.time.Instant` 클래스에서는 이와 같은 기계적인 관점에서 시간을 표현한다. 즉, `Instant` 클래스는 유닉스 에포크 시간(1970년 1월 1일 0시 0분 0초 UTC)을
 기준으로 특정 지점까지의 시간을 초로 표현한다.
* 팩터리 메서드 `ofEpochSecond`에 초를 넘겨줘서 `Instant` 클래스 인스턴스를 만들 수 있다. `Instant` 클래스는 나노초(10억분의 1초)의 정밀도를 제공한다.
* 또한 오버로드된 `ofEpochSecond` 메서드 버전에서는 두 번째 인수를 이용해서 나노초 단위로 시간을 보정할 수 있다. 두 번째 인수에서 0에서 999.999.999 
 사이의 값을 지정할 수 있다.
```java
Instant.ofEpochSecond(3);
Instant.ofEpochSecond(3, 0);
Instant.ofEpochSecond(2, 1_000_000_000);   // 2초 이후의 1억 나노초(1초)
Instant.ofEpochSecond(4, -1_000_000_000);  // 4초 이전의 1억 나노초(1초)
```

* `Instant` 클래스도 사람이 확인할 수 있도록 시간을 표시해주는 정적 팩터리 메서드 `now`를 제공한다. 하지만 `Instant`는 초와 나노초 정볼르 포함한다.
 따라서 `Instant`는 사람이 읽을 수 있는 시간 정보를 제공하지 않는다.

### Duration과 Period 정의

* `Duration` 클래스의 정적 팩터리 메서드 `between`으로 두 시간 객체 사이의 지속시간을 만들 수 있다.
```java
Duration d1 = Duration.between(time1, time2);  // LocalTime
Duration d1 = Duration.between(dateTime1, dateTime2);  // LocalDateTime
Duration d2 = Duration.between(instant1, instant2);     // Instant
```

* `Duration` 클래스는 초와 나노초로 시간 단위를 표현하므로 `between`메서드에 `LocalDate`를 전달할 수 없다. 년, 월, 일로 시간을 표현할 때는
 `Period` 클래스를 사용한다. 즉, `Period` 클래스의 팩토리 메서드 `between`을 이용하면 두 `LocalDate`의 차이를 확인할 수 있다.
```java
Period tenDays = Period.between(
    LocalDate.of(2017, 9, 11), LocalDate.of(2017, 9 ,21));
```

* `Duration`과 `Period` 클래스는 자신의 인스턴스를 만들 수 있도록 다양한 팩터리 메서드를 제공한다.
```java
Duration threeMinutes = Duration.ofMinutes(3);
Duration threeMinutes = Duration.of(3, ChronoUnit.MINUTES);

Period tenDays = Period.ofDays(10);
Period threeWeeks = Period.ofWeeks(3);
Period twoYearsSixMonthsOneDay = Period.of(2, 6, 1);
```

## 날짜 조정, 파싱, 포매팅
* `withAttribute` 메서드로 기존의 `LocalDate`를 바꾼 버전을 직접 간단하게 만들 수 있다.
* 모든 메서드는 기존 객체를 바꾸지 않는다.
```java
LocalDate date1 = LocalDate.of(2017, 9, 21); // 2017-09-21
LocalDate date2 = date1.withYear(2011); // 2011-09-21
LocalDate date3 = date2.withDayOfMonth(25);  // 2011-09-25
LocalDate date4 = date3.with(ChronoField.MONTH_OF_YEAR, 2); // 2011-02-25
```

* 마지막 `with` 메서드는 `get` 메서드와 쌍을 이룬다. 이들 두 메서드는 날짜와 시간 API의 모든 클래스가 구현하는 `Temporal` 인터페이스가 정의되어 있다.
* `Temporal`인터페이스는 `LocalDate, LocalTime, LocalDateTime` 인터페이스에 정의되어 있다. 정확히 포현하자면 `get`과 `with` 메서드로 
 `Temporal` 객체의 필드값을 읽거나 고칠 수 있다.
* 어떤 `Temporal` 객체가 지정된 필드를 지원하지 않으면 `UnsupportedTemporalTypeException`이 발생한다.
  * 예를 들어 `Instant`에 `ChronoField.MONTH_OF_YEAR`를 사용하거나 `LocalDate`에 `ChronoField.NANO_OF_SECOND`를 사용하면 예외가 발생한다.

### TemporalAdjusters 사용하기

* 좀 더 복잡한 날자 조정 기능이 필요할 경우 오버로드된 버전의 `with` 메서드에 좀 더 댜양한 동작을 수행할 수 있도록 하는 기능을 제공하는 `TemporalAdjuster`
 를 전달하는 방법으로 문제를 해결할 수 있다. 날짜와 시간 API는 다양한 상황에서 사용할 수 있도록 다양한 `TemporalAdjuster`를 제공한다.
```java
import static java.time.temporal.TemporalAdjusters.*;

LocalDate date1 = LocalDate.of(2014, 3, 18); // 2014-03-18 (화)
LocalDate date2 = date1.with(nextOrSame(DayOfWeek.SUNDAY)); // 2014-03-23
LocalDate date3 = date2.with(lastDayOfMonth()); // 2014-03-31
```

* 필요한 기능이 정의되 있지 않을 때는 비교적 쉽게 커스텀 `TemporalAdjuster` 구현을 만들 수 있다.
```java
@FunctionalInterface
public interface TemporalAdjuster {
    Temporal adjustInto(Temporal temporal);
}
```

### 날짜와 시간 객체 출력과 파싱

* `DateTimeFormatter` 클래스는 `BASIC_ISO_DATE`와 `ISO_LOCAL_DATE`등의 상수를 미리 정의하고 있다.
* `DateTimeFormatter`를 이용해서 날짜나 시간을 특정 형식의 문자열로 만들 수 있다.
```java
LocalDate date = LocalDate.of(2014, 3, 18);
String s1 = date.format(DateTimeFormatter.BASIC_ISO_DATE); // 20140318
String s2 = date.format(DateTimeFormatter.ISO_LOCAL_DATE); // 2014-03-18
```

* 반대로 날짜나 시간을 표현하는 문자열을 파싱해서 날짜 객체를 다시 만들 수 있다.
```java
LocalDate date1 = LocalDate.parse("20140318", DateTimeFormatter.BASIC_ISO_DATE);
LocalDate date2 = LocalDate.parse("2014-03-18", DateTimeFormatter.ISO_LOCAL_DATE);
```

* 기존 `java.util.DateFormat` 클래스와 달리 모든 `DateTimeFormatter`는 스레드에서 안전하게 사용할 수 있는 클래스다.
```java
// 패턴으로 DateTimeFormatter 만들기
DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
LocalDate date1 = LocalDate.of(2014, 3, 18);
String formattedDate = date1.format(formatter);
LocalDate date2 = LocalDate.parse(formattedDate, formatter);
```

* 정적 메서드 `parse` 같은 포메터를 적용해서 생성된 문자열을 파싱함으로써 다시 날짜를 생성한다.
```java
// 지역화된 DateTimeFormatter 만들기
DateTimeFormatter italianFormatter = DateTimeFormatter.ofPattern("d. MMMM yyyy", Locale.ITALIAN);
LocalDate date1 = LocalDate.of(2014, 3, 18);
String formattedDate = date.format(italianFormatter);  // 18. marzo 2014
LocalDate date2 = LocalDate.parse(formattedDate, italianFormatter);
```

* `DateTimeFormatterBuilder` 클래스로 복합적인 모패터를 정의해서 좀 더 세부적으로 포매터를 제어할 수 있다.
* 즉, `DateTimeFormatterBuilder` 클래스로 대소문자를 구분하는 파싱, 관대한 규칙을 적용하는 파싱(정해진 형식과 정확하게 일치하지 않는 입력을 해석할 수 있도록
 체험적 방식의 파서 사용), 패딩, 포매터의 선택사항 등을 활용할 수 있다.
```java
// DateTimeFormatter 만들기
DateTimeFormatter italianFormatter = new DateTimeFormatterBuilder()
        .appendText(ChronoField.DAY_OF_MONTH)
        .appendLiteral(". ")
        .appendText(ChronoField.MONTH_OF_YEAR)
        .appendLiteral(" ")
        .appendText(ChronoField.YEAR)
        .parseCaseInsensitive()
        .toFormatter(Locale.ITALIAN);
```

## 다양한 시간대와 캘린더 활용 방법

* 새로운 날짜와 시간 API의 큰 편리함 중 하나는 시간대를 간단하게 처리할 수 있다는 점이다.
* 기존의 `java.util.TimeZone`을 대체할 수 있는 `java.time.ZoneId` 클래스가 새롭게 등장했다. 새로운 클래스를 이용하면 서머타임(DST)
 같은 복잡한 사항이 자동으로 처리된다.
* 날짜와 시간 API에서 제공하는 다른 클래스와 마찬가지로 ZoneId는 불변 클래스다.

### 시간대 사용하기

* 표준 시간이 같은 지역을 묶어서 **시간대** 규칙 집합을 정의한다. `ZoneRules` 클래스에는 약 40개 정도의 시간대가 있다.
* `ZoneId`의 `getRules()`를 이용해서 해당 시간대의 규정을 획득할 수 있다.
```java
ZoneId romeZone = ZoneId.of("Europe/Rome");
```
* 지역 ID는 `{지역}/{도시}` 형식으로 이루어지며 `IANA Time Zone Database`에서 제공하는 지역 집합 정보를 사용한다. https://www.iana.org/time-zones
* `ZoneId`의 새로운 메서드인 `toZoneId`로 기존의 `TimeZone` 객체를 `ZoneId` 객체로 변환할 수 있다.
```java
ZoneId zoneId = TimeZone.getDefault().toZoneId();
```

* `ZoneId` 객체를 얻은 다음에는 `LocalDate, LocalDateTime, Instant`를 이용해서 `ZonedDateTime` 인스턴스로 변환할 수 있다.
```java
LocalDate date = LocalDate.of(2014, Month.MARCH, 18);
ZonedDateTime zd1 = date.atStartOfDay(romeZone);
LocalDateTime dateTime = LocalDateTime.of(2014, Month.MARCH, 18, 13, 45);
ZonedDateTime zdt2 = dateTime.atZone(romeZone);
Instant instant = Instant.now();
ZonedDateTime zdt3 = instant.atZone(romeZone);
```

* `ZoneId`를 이용해서 `LocalDateTime`을 `Instant`로 바꾸는 방법도 있다.
```java
Instant instant = Instant.now();
LocalDateTIme timeFromInstant = LocalDateTime.ofInstant(instant, romeZone);
```

### UTC/Greenwich 기준의 고정 오프셋

* 때로는 `UTC(Universal Time Coordinated 협정 세계시)/GMT(Greenwich Mean Time 그리니치 표준시)`를 기준으로 시간대를 표현하기도 한다.
  * 예를들어 '뉴욕은 런던보다 5시간 느리다'라고 표현할 수 있다.
* `ZoneId`의 서브클래스인 `ZoneOffSet` 클래스로 런던의 그리니치 0도 자오선과 시간값의 차이를 표현할 수 있다.
```java
ZoneOffset newYorkOffset = ZoneOffset.of("-05:00");
```
* `ZoneOffset`으로 서머타임을 제대로 처리할 수 없으므로 권장하지 않는 방식이다.
* 또한 `ISO-8601` 캘린더 시스템에서 정의하는 `UTC/GMT`와 오프셋으로 날짜와 시간을 표현하는 `OffsetDateTime`을 만드는 방법도 있다.
```java
LocalDateTime dateTime = LocalDateTime.of(2014, Month.MARCH, 18, 13, 45);
OffsetDateTime dateTimeInNewYork = OffsetDateTime.of(date, newYorkOffset);
```
* 새로운 날짜와 시간 API는 ISO 캘린더 시스템에 기반하지 않은 정보도 처리할 수 있는 기능을 제공한다.
