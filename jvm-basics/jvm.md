# jvm 정리

## jvm 이란?

* JVM은 Java Virtual Machine의 약자로, 자바 가상 머신이다.
* 자바와 운영체제 사이에서 중개자 역할을 수행하며, 자바가 운영체제에 종속 받지 않고 프로그램을 실행할 수 있도록 도와준다.
* 또한, 가비지 컬렉터를 사용한 메모리 관리도 자동으로 수행하며, 다른 하드웨어와 다르게 레지스터 기반이 아닌 스택 기반으로 동작한다.
* .java 파일은 cpu가 인식하지 못하므로 기계어로 컴파일해줘야 하지만 Java는 jvm을 거쳐 os에 도달하기 때문에 os가 인식할 수 잇는 기계어로 바로 컴파일되는게 아니라 jvm이 인식할 수 있는 java byte code(.class)로 변환된다.
* 여기서 자바 컴파일러(javac.exe)가 .java 파일을 .class로 변환한다.

## jvm 메모리 구조

### Class Loader

### Execution Engine

### Runtime Data Area

#### Mthed Area

* 모든 스레드가 공유됨
* static 변수, 전역 변수, 코드에서 사용되는 Class 메타 정보 등이 할당

#### Heap 영역

* 런타임 시점에 생성되는 객체들이 동적으로 할당되는 영역
* 모든 스레드가 공유됨
* GC에 의해 관리되는 영역
* heap 영역에는 new generation, old generation 영역이 있음
  * New generation(Young generation) - eden, survival0, survival1 영역
    * eden - 새로운 객체가 할당, 메모리가 다 사용되면 가비지 컬렉터가 발생(minor GC) minor GC에서 살아남을 때마다 age 1씩 증가.
        * minor GC mark and sweep 과정이 일어남 여기서 살아남은 객체는  survival0로 옮겨진다.
        * eden 영역의 unreachable 객체는 메모리에해제
    * survival0 - eden 영역에서 살아남은 객체가 옮겨짐 
        * 여기서 survival0 메모리가 다사용되면 mark and sweep 과정이 일어남 여기서 살아남은 객체는 survival1으로 이동
    * survival1 - survival0의 메모리가 가득차 mark and sweep 과정       살아남은 객체가 이동, 이때 객체의 age 값이 증가
        * 똑같이 메모리가 가득 차게되면 mark and sweep 과정이 일거나고 살아남은 객체는 survival0 영역으로 이동, 이때 객체의 age 값이 증가.
    * survival 영역의 규칙은 survival0 혹은 survival1 둘중 하나는 꼭   비어있어야 한다.
    * 해당 과정 반복 후 특정 Age 값 이상이 되면 Old generation 영역으로     옮겨진다. 이 과정을 promotion이라한다.

  * Old generation - promotion 과정으로 옮겨진 객체들의 영역
     * promotion 과정이 계속해서 일어나게 되어 메모리를 다 사용하게 되면 가비지 컬렉터(Major GC) 발생

#### JVM Stacks

* 각 스레드마다 고유하게 할당되며 스레드 종류시 소멸됨
* 지역 변수, 함수(메서드) 등이 할당되는 LIFO(Last In First Out) 방식의 메모리

#### Pc Registers

* 각 스레드마다 고유하게 할당되며 스레드 종류시 소멸됨
* 스레드가 어떤 부분을 무슨 명령으로 실행해야할 지에 대한 기록을 하는 부분으로 현재 수행중인 jvm 명령의 주소를 갖는다.

#### Native Method Stacks

* 각 스레드마다 고유하게 할당되며 스레드 종류시 소멸됨
* 자바 언어외 다른 언어로 작성된 네이티브 코드를 위한 메모리 영역


## Jvm GC

* jvm GC는 기본적으로 mark and sweep 방식이다.
* mark and sweep 방식은
    * 의도적으로 GC를 실행시켜야 한다.
    * 애플리케이션 실행과 GC 실행이 병행된다.

## Garbage Collector 종류

1. Serial GC
    * GC를 처리하는 스레드가 1개이다.
    * CPU 코어가 1개만 있을 때 사용하는 방식
    * Mark-Compat collection 알고리즘 사용
        * Mark-Compat collection 알고리즘은 mark and sweep 과정 중 Compat 이라는 과정이 추가되어 sweep과정 후 데이터 파편화가 일어나는데 이 데이터를 한군데로 몰아서 메모리 파편화를 방지할 수 있다.

2. Parallel GC (java 8 default)
    * GC를 처리하는 스레드가 여러개 이다.
    * Serial GC보다 빠르게 객체를 처리할 수 있다.
    * Parallel GC는 메모리가 충분하고 코어 개수가 많을 때 유리하다.
    * java 8 기준 age-bit가 15가 되면 promotion진행.

3. Concurrent Mark Sweep GC (CMS GC)
    * STOP THE WORLD 시간을 줄인 GC
    * 응답시간이 빨라야 하는 애플리케이션에서 사용
    * 다른 GC 방식보다 더 많은 메모리와 CPU 자원을 사용한다.
    * Compaction 단계를 제공하지 않는다.

4. G1(Garbage First) GC (java 9 부터 default)
    * region이라는 영역으로 메모리를 잘게 쪼개서 관리
    * G1(Garbage First) GC는 바둑판의 각 영역에 객체를 할당하고 GC를 실행한다. 그러다가, 해당 영역이 꽉 차면 다른 빈 영역에, 객체를 할당하고 GC를 실행한다.
    * STOP THE WORLD 시간이 짧다.
    * Compaction를 사용한다.

* **STOP THE WORLD**
    * GC를 실행하기 위해 JVM이 애플리케이션 실행을 멈추는 것이다.
    * STOP THE WORLD가 발생하면 GC를 실행하는 스레드를 제외한 나머지     스레드는 모두 작업을 멈춘다.
    * GC 작업을 완료한 이후에 중단된 작업을 다시 시작한다.