# 이펙티브 자바 정리
* 본 게시물은 이펙티브 자바 서적을 정리한 내용입니다.
* 서적 : [이펙티브 자바](http://www.yes24.com/Product/Goods/65551284)

## 2장. 객체 생성과 파괴

* [item 1 : 생성자 대신 정적 팩토리 메서드를 고려하라](https://github.com/parkhanbeen/study/blob/master/effective-java/2%EC%9E%A5/1.%EC%83%9D%EC%84%B1%EC%9E%90%20%EB%8C%80%EC%8B%A0%20%EC%A0%95%EC%A0%81%20%ED%8C%A9%ED%84%B0%EB%A6%AC%20%EB%A9%94%EC%84%9C%EB%93%9C%EB%A5%BC%20%EA%B3%A0%EB%A0%A4%ED%95%98%EB%9D%BC.md)
* [item 2 : 생성자에게 매개변수가 많다면 빌더를 고려하라](https://github.com/parkhanbeen/study/blob/master/effective-java/2%EC%9E%A5/2.%EC%83%9D%EC%84%B1%EC%9E%90%EC%97%90%20%EB%A7%A4%EA%B0%9C%EB%B3%80%EC%88%98%EA%B0%80%20%EB%A7%8E%EB%8B%A4%EB%A9%B4%20%EB%B9%8C%EB%8D%94%EB%A5%BC%20%EA%B3%A0%EB%A0%A4%ED%95%98%EB%9D%BC.md)
* [item 3 : private 생성자나 열거 타입으로 싱글톤임을 보증하라](https://github.com/parkhanbeen/study/blob/master/effective-java/2%EC%9E%A5/3.private%20%EC%83%9D%EC%84%B1%EC%9E%90%EB%82%98%20%EC%97%B4%EA%B1%B0%20%ED%83%80%EC%9E%85%EC%9C%BC%EB%A1%9C%20%EC%8B%B1%EA%B8%80%ED%86%A4%EC%9E%84%EC%9D%84%20%EB%B3%B4%EC%A6%9D%ED%95%98%EB%9D%BC.md)
* [item 4 : 인스턴스화를 막으려거든 private 생성자를 사용하라](https://github.com/parkhanbeen/study/blob/master/effective-java/2%EC%9E%A5/4.%EC%9D%B8%EC%8A%A4%ED%84%B4%EC%8A%A4%ED%99%94%EB%A5%BC%20%EB%A7%89%EC%9C%BC%EB%A0%A4%EA%B1%B0%EB%93%A0%20private%20%EC%83%9D%EC%84%B1%EC%9E%90%EB%A5%BC%20%EC%82%AC%EC%9A%A9%ED%95%98%EB%9D%BC.md)
* [item 5 : 자원을 직접 명시하지 말고 의존 객체 주입을 사용하라](https://github.com/parkhanbeen/study/blob/master/effective-java/2%EC%9E%A5/5.%EC%9E%90%EC%9B%90%EC%9D%84%20%EC%A7%81%EC%A0%91%20%EB%AA%85%EC%8B%9C%ED%95%98%EC%A7%80%20%EB%A7%90%EA%B3%A0%20%EC%9D%98%EC%A1%B4%20%EA%B0%9D%EC%B2%B4%20%EC%A3%BC%EC%9E%85%EC%9D%84%20%EC%82%AC%EC%9A%A9%ED%95%98%EB%9D%BC.md)
* [item 6 : 불필요한 객체 생성을 피하라](https://github.com/parkhanbeen/study/blob/master/effective-java/2%EC%9E%A5/6.%EB%B6%88%ED%95%84%EC%9A%94%ED%95%9C%20%EA%B0%9D%EC%B2%B4%20%EC%83%9D%EC%84%B1%EC%9D%84%20%ED%94%BC%ED%95%98%EB%9D%BC.md)
* [item 7 : 다 쓴 객체 참조를 해제하라](https://github.com/parkhanbeen/study/blob/150e8ecda5fc3dcabfd854e10f0c32729fdcf01a/effective-java/2%EC%9E%A5/7.%EB%8B%A4%20%EC%93%B4%20%EA%B0%9D%EC%B2%B4%20%EC%B0%B8%EC%A1%B0%EB%A5%BC%20%ED%95%B4%EC%A0%9C%ED%95%98%EB%9D%BC.md)
* [item 8 : finalizer 와 cleaner 사용을 피하라](https://github.com/parkhanbeen/study/blob/master/effective-java/2%EC%9E%A5/8.finalizer%EC%99%80%20cleaner%20%EC%82%AC%EC%9A%A9%EC%9D%84%20%ED%94%BC%ED%95%98%EB%9D%BC.md)
* [item 9 : try-finally 보다는 try-with-resources 를 사용하라](https://github.com/parkhanbeen/study/blob/master/effective-java/2%EC%9E%A5/9.try-finally%20%EB%B3%B4%EB%8B%A4%EB%8A%94%20try-with-resources%20%EB%A5%BC%20%EC%82%AC%EC%9A%A9%ED%95%98%EB%9D%BC.md)

## 3장. 모든 객체의 공통 메서드

* [item 10 : equals는 일반 규약을 지켜 재정의하라](https://github.com/parkhanbeen/study/blob/master/effective-java/3%EC%9E%A5/10.equals%EB%8A%94%20%EC%9D%BC%EB%B0%98%20%EA%B7%9C%EC%95%BD%EC%9D%84%20%EC%A7%80%EC%BC%9C%20%EC%9E%AC%EC%A0%95%EC%9D%98%ED%95%98%EB%9D%BC.md)
* [item 11 : equals를 재정의하려거든 hashcode도 재정의하라](https://github.com/parkhanbeen/study/blob/master/effective-java/3%EC%9E%A5/11.equals%EB%A5%BC%20%EC%9E%AC%EC%A0%95%EC%9D%98%ED%95%98%EB%A0%A4%EA%B1%B0%EB%93%A0%20hashcode%EB%8F%84%20%EC%9E%AC%EC%A0%95%EC%9D%98%ED%95%98%EB%9D%BC.md)
* [item 12 : toString을 항상 재정의하라](https://github.com/parkhanbeen/study/blob/master/effective-java/3%EC%9E%A5/12.toString%EC%9D%84%20%ED%95%AD%EC%83%81%20%EC%9E%AC%EC%A0%95%EC%9D%98%ED%95%98%EB%9D%BC.md)
* [item 13 : clone 재정의는 주의해서 진행하라](https://github.com/parkhanbeen/study/blob/master/effective-java/3%EC%9E%A5/13.clone%20%EC%9E%AC%EC%A0%95%EC%9D%98%EB%8A%94%20%EC%A3%BC%EC%9D%98%ED%95%B4%EC%84%9C%20%EC%A7%84%ED%96%89%ED%95%98%EB%9D%BC.md)
* [item 14 : Comparable을 구현할지 고려하라](https://github.com/parkhanbeen/study/blob/master/effective-java/3%EC%9E%A5/14.Comparable%EC%9D%84%20%EA%B5%AC%ED%98%84%ED%95%A0%EC%A7%80%20%EA%B3%A0%EB%A0%A4%ED%95%98%EB%9D%BC.md)

## 4장. 클래스와 인터페이스

* [item 15 : 클래스와 멤버의 접근 권한을 최소화하라](https://github.com/parkhanbeen/study/blob/master/effective-java/4%EC%9E%A5/15.%ED%81%B4%EB%9E%98%EC%8A%A4%EC%99%80%20%EB%A9%A4%EB%B2%84%EC%9D%98%20%EC%A0%91%EA%B7%BC%20%EA%B6%8C%ED%95%9C%EC%9D%84%20%EC%B5%9C%EC%86%8C%ED%99%94%ED%95%98%EB%9D%BC.md)
* [item 16 : public 클래스에서는 public 필드가 아닌 접근자 메서드를 사용하라](https://github.com/parkhanbeen/study/blob/master/effective-java/4%EC%9E%A5/16.public%20%ED%81%B4%EB%9E%98%EC%8A%A4%EC%97%90%EC%84%9C%EB%8A%94%20public%20%ED%95%84%EB%93%9C%EA%B0%80%20%EC%95%84%EB%8B%8C%20%EC%A0%91%EA%B7%BC%EC%9E%90%20%EB%A9%94%EC%84%9C%EB%93%9C%EB%A5%BC%20%EC%82%AC%EC%9A%A9%ED%95%98%EB%9D%BC.md)
* [item 17 : 변경 가능성을 최소화하라](https://github.com/parkhanbeen/study/blob/master/effective-java/4%EC%9E%A5/17.%EB%B3%80%EA%B2%BD%20%EA%B0%80%EB%8A%A5%EC%84%B1%EC%9D%84%20%EC%B5%9C%EC%86%8C%ED%99%94%ED%95%98%EB%9D%BC.md)
* [item 18 : 상속보다는 컴포지션을 사용하라](https://github.com/parkhanbeen/study/blob/master/effective-java/4%EC%9E%A5/18.%EC%83%81%EC%86%8D%EB%B3%B4%EB%8B%A4%EB%8A%94%20%EC%BB%B4%ED%8F%AC%EC%A7%80%EC%85%98%EC%9D%84%20%EC%82%AC%EC%9A%A9%ED%95%98%EB%9D%BC.md)
* [item 19 : 상속을 고려해 설계하고 문서화하라. 그러지 않았다면 상속을 금지하라](https://github.com/parkhanbeen/study/blob/master/effective-java/4%EC%9E%A5/19.%EC%83%81%EC%86%8D%EC%9D%84%20%EA%B3%A0%EB%A0%A4%ED%95%B4%20%EC%84%A4%EA%B3%84%ED%95%98%EA%B3%A0%20%EB%AC%B8%EC%84%9C%ED%99%94%ED%95%98%EB%9D%BC.%20%EA%B7%B8%EB%9F%AC%EC%A7%80%20%EC%95%8A%EC%95%98%EB%8B%A4%EB%A9%B4%20%EC%83%81%EC%86%8D%EC%9D%84%20%EA%B8%88%EC%A7%80%ED%95%98%EB%9D%BC.md)
* [item 20 : 추상 클래스보다는 인터페이스를 우선하라](https://github.com/parkhanbeen/study/blob/master/effective-java/4%EC%9E%A5/20.%EC%B6%94%EC%83%81%20%ED%81%B4%EB%9E%98%EC%8A%A4%EB%B3%B4%EB%8B%A4%EB%8A%94%20%EC%9D%B8%ED%84%B0%ED%8E%98%EC%9D%B4%EC%8A%A4%EB%A5%BC%20%EC%9A%B0%EC%84%A0%ED%95%98%EB%9D%BC.md)
* [item 21 : 인터페이스는 구현하는 쪽을 생각해 설계하라](https://github.com/parkhanbeen/study/blob/master/effective-java/4%EC%9E%A5/21.%EC%9D%B8%ED%84%B0%ED%8E%98%EC%9D%B4%EC%8A%A4%EB%8A%94%20%EA%B5%AC%ED%98%84%ED%95%98%EB%8A%94%20%EC%AA%BD%EC%9D%84%20%EC%83%9D%EA%B0%81%ED%95%B4%20%EC%84%A4%EA%B3%84%ED%95%98%EB%9D%BC.md)
* [item 22 : 인터페이스는 타입을 정의하는 용도로만 사용하라](https://github.com/parkhanbeen/study/blob/master/effective-java/4%EC%9E%A5/22.%EC%9D%B8%ED%84%B0%ED%8E%98%EC%9D%B4%EC%8A%A4%EB%8A%94%20%ED%83%80%EC%9E%85%EC%9D%84%20%EC%A0%95%EC%9D%98%ED%95%98%EB%8A%94%20%EC%9A%A9%EB%8F%84%EB%A1%9C%EB%A7%8C%20%EC%82%AC%EC%9A%A9%ED%95%98%EB%9D%BC.md)
* [item 23 : 태그 달린 클래스보다는 클래스 계층구조를 활용하라](https://github.com/parkhanbeen/study/blob/master/effective-java/4%EC%9E%A5/23.%ED%83%9C%EA%B7%B8%20%EB%8B%AC%EB%A6%B0%20%ED%81%B4%EB%9E%98%EC%8A%A4%EB%B3%B4%EB%8B%A4%EB%8A%94%20%ED%81%B4%EB%9E%98%EC%8A%A4%20%EA%B3%84%EC%B8%B5%EA%B5%AC%EC%A1%B0%EB%A5%BC%20%ED%99%9C%EC%9A%A9%ED%95%98%EB%9D%BC.md)
* [item 24 : 멤버 클래스는 되도록 static으로 만들라](https://github.com/parkhanbeen/study/blob/master/effective-java/4%EC%9E%A5/24.%EB%A9%A4%EB%B2%84%20%ED%81%B4%EB%9E%98%EC%8A%A4%EB%8A%94%20%EB%90%98%EB%8F%84%EB%A1%9D%20static%EC%9C%BC%EB%A1%9C%20%EB%A7%8C%EB%93%A4%EB%9D%BC.md)
* [item 25 : 톱레벨 클래스는 한 파일에 하나만 담으라](https://github.com/parkhanbeen/study/blob/master/effective-java/4%EC%9E%A5/25.%ED%86%B1%EB%A0%88%EB%B2%A8%20%ED%81%B4%EB%9E%98%EC%8A%A4%EB%8A%94%20%ED%95%9C%20%ED%8C%8C%EC%9D%BC%EC%97%90%20%ED%95%98%EB%82%98%EB%A7%8C%20%EB%8B%B4%EC%9C%BC%EB%9D%BC.md)

## 5장. 제네릭

* [item 26 : 로 타입은 사용하지 말라](https://github.com/parkhanbeen/study/blob/master/effective-java/5%EC%9E%A5/26.%EB%A1%9C%20%ED%83%80%EC%9E%85%EC%9D%80%20%EC%82%AC%EC%9A%A9%ED%95%98%EC%A7%80%20%EB%A7%90%EB%9D%BC.md)
* [item 27 : 비검사 경고를 제거하라](https://github.com/parkhanbeen/study/blob/master/effective-java/5%EC%9E%A5/27.%EB%B9%84%EA%B2%80%EC%82%AC%20%EA%B2%BD%EA%B3%A0%EB%A5%BC%20%EC%A0%9C%EA%B1%B0%ED%95%98%EB%9D%BC.md)
* [item 28 : 배열보다는 리스트를 사용하라](https://github.com/parkhanbeen/study/blob/master/effective-java/5%EC%9E%A5/28.%EB%B0%B0%EC%97%B4%EB%B3%B4%EB%8B%A4%EB%8A%94%20%EB%A6%AC%EC%8A%A4%ED%8A%B8%EB%A5%BC%20%EC%82%AC%EC%9A%A9%ED%95%98%EB%9D%BC.md)
* [item 29 : 이왕이면 제네릭 타입으로 만들라](https://github.com/parkhanbeen/study/blob/master/effective-java/5%EC%9E%A5/29.%EC%9D%B4%EC%99%95%EC%9D%B4%EB%A9%B4%20%EC%A0%9C%EB%84%A4%EB%A6%AD%20%ED%83%80%EC%9E%85%EC%9C%BC%EB%A1%9C%20%EB%A7%8C%EB%93%A4%EB%9D%BC.md)
* [item 30 : 이왕이면 제네릭 메서드로 만들라](https://github.com/parkhanbeen/study/blob/master/effective-java/5%EC%9E%A5/30.%EC%9D%B4%EC%99%95%EC%9D%B4%EB%A9%B4%20%EC%A0%9C%EB%84%A4%EB%A6%AD%20%EB%A9%94%EC%84%9C%EB%93%9C%EB%A1%9C%20%EB%A7%8C%EB%93%A4%EB%9D%BC.md)
* [item 31 : 한정적 와일드카드를 사용해 API 유연성을 높이라](https://github.com/parkhanbeen/study/blob/master/effective-java/5%EC%9E%A5/31.%ED%95%9C%EC%A0%95%EC%A0%81%20%EC%99%80%EC%9D%BC%EB%93%9C%EC%B9%B4%EB%93%9C%EB%A5%BC%20%EC%82%AC%EC%9A%A9%ED%95%B4%20API%20%EC%9C%A0%EC%97%B0%EC%84%B1%EC%9D%84%20%EB%86%92%EC%9D%B4%EB%9D%BC.md)
* [item 32 : 제네릭과 가변인수를 함께 쓸 때는 신중하라](https://github.com/parkhanbeen/study/blob/master/effective-java/5%EC%9E%A5/32.%EC%A0%9C%EB%84%A4%EB%A6%AD%EA%B3%BC%20%EA%B0%80%EB%B3%80%EC%9D%B8%EC%88%98%EB%A5%BC%20%ED%95%A8%EA%BB%98%20%EC%93%B8%20%EB%95%8C%EB%8A%94%20%EC%8B%A0%EC%A4%91%ED%95%98%EB%9D%BC.md)
* [item 33 : 타입 안전 이중 컨테이너를 고려하라](https://github.com/parkhanbeen/study/blob/master/effective-java/5%EC%9E%A5/33.%ED%83%80%EC%9E%85%20%EC%95%88%EC%A0%84%20%EC%9D%B4%EC%A2%85%20%EC%BB%A8%ED%85%8C%EC%9D%B4%EB%84%88%EB%A5%BC%20%EA%B3%A0%EB%A0%A4%ED%95%98%EB%9D%BC.md)

## 6장. 열거 타입과 애너테이션

* [item 34 : int 상수 대신 열거 타입을 사용하라](https://github.com/parkhanbeen/study/blob/master/effective-java/6%EC%9E%A5/34.int%20%EC%83%81%EC%88%98%20%EB%8C%80%EC%8B%A0%20%EC%97%B4%EA%B1%B0%20%ED%83%80%EC%9E%85%EC%9D%84%20%EC%82%AC%EC%9A%A9%ED%95%98%EB%9D%BC.md)
* [item 35 : ordinal 메서드 대신 인스턴스 필드를 사용하라](https://github.com/parkhanbeen/study/blob/master/effective-java/6%EC%9E%A5/35.ordinal%20%EB%A9%94%EC%84%9C%EB%93%9C%20%EB%8C%80%EC%8B%A0%20%EC%9D%B8%EC%8A%A4%ED%84%B4%EC%8A%A4%20%ED%95%84%EB%93%9C%EB%A5%BC%20%EC%82%AC%EC%9A%A9%ED%95%98%EB%9D%BC.md)
* [item 36 : 비트 필드 대신 EnumSet을 사용하라](https://github.com/parkhanbeen/study/blob/master/effective-java/6%EC%9E%A5/36.%EB%B9%84%ED%8A%B8%20%ED%95%84%EB%93%9C%20%EB%8C%80%EC%8B%A0%20EnumSet%EC%9D%84%20%EC%82%AC%EC%9A%A9%ED%95%98%EB%9D%BC.md)
* [item 37 : ordinal 인덱싱 대신 EnumMap을 사용하라](https://github.com/parkhanbeen/study/blob/master/effective-java/6%EC%9E%A5/37.ordinal%20%EC%9D%B8%EB%8D%B1%EC%8B%B1%20%EB%8C%80%EC%8B%A0%20EnumMap%EC%9D%84%20%EC%82%AC%EC%9A%A9%ED%95%98%EB%9D%BC.md)
* [item 38 : 확장할 수 있는 열거 타입이 필요하면 인터페이스를 사용하라](https://github.com/parkhanbeen/study/blob/master/effective-java/6%EC%9E%A5/38.%ED%99%95%EC%9E%A5%ED%95%A0%20%EC%88%98%20%EC%9E%88%EB%8A%94%20%EC%97%B4%EA%B1%B0%20%ED%83%80%EC%9E%85%EC%9D%B4%20%ED%95%84%EC%9A%94%ED%95%98%EB%A9%B4%20%EC%9D%B8%ED%84%B0%ED%8E%98%EC%9D%B4%EC%8A%A4%EB%A5%BC%20%EC%82%AC%EC%9A%A9%ED%95%98%EB%9D%BC.md)
