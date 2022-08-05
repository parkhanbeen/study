# jvm 정리

## jvm 이란?

* JVM은 Java Virtual Machine의 약자로, 자바 가상 머신이다.
* 자바와 운영체제 사이에서 중개자 역할을 수행하며, 자바가 운영체제에 종속 받지 않고 프로그램을 실행할 수 있도록 도와준다.
* 또한, 가비지 컬렉터를 사용한 메모리 관리도 자동으로 수행하며, 다른 하드웨어와 다르게 레지스터 기반이 아닌 스택 기반으로 동작한다.
* .java 파일은 cpu가 인식하지 못하므로 기계어로 컴파일해줘야 하지만 Java는 jvm을 거쳐 os에 도달하기 때문에 os가 인식할 수 잇는 기계어로 바로 컴파일되는게 아니라 jvm이 인식할 수 있는 java byte code(.class)로 변환된다.
* 여기서 자바 컴파일러(javac.exe)가 .java 파일을 .class로 변환한다.

## jvm 메모리 구조

### Class Loader

* 클래스 로더는 런타임 중 java 클래스를 JVM에 동적으로 로드하는 역할을 한다.
* 클래스 로더에는 로딩, 링크, 초기화 단계로 나누어져 있다.
* 로딩
    * 자바 바이트 코드(.class)를 메소드 영역에 저장
    * 바이트코드(.class)는 JVM에 의해 메소드 영역에 다음 정보들을 저장
        * 로드된 클래스를 비롯한 상위 클래스 정보
        * 클래스 파일과 Class, Interface, Enum의 관련 여부
        * 변수나 메소드 등의 정보
    * Bootstrap, Extension, Application - 해당 컴포넌트에 의해 클래스들이 로드된다. 이 세가지 클래스 로더들은 모두 상속관계로 정의되어 있으며 delegate(위임) 방식으로 작업을 진행
        * Bootstrap ClassLoader - jre의 lib폴더에 있는 rt.jar 파일을 탐색 기본 자바 API 라이브러리를 로드. 가장 최우선으로 로드됨
        * Extension ClassLoader - jre의 lib 폴더에 있는 ext 폴더에 있는 모든 확장 코어 클래스 파일을 로드. 최근에는 Platform classLoader라고 한다. 
        Bootstrap ClassLoader의 자식이며 Extension 클래스 로더는 jdk 확장 디렉토리(JAVA_HOME/lib/ext 혹은 java.ext.dirs에 저장된 경로)에서 로드.
        * Application ClassLoader - Extension ClassLoader의 자식이며 시스템 클래스로더(System ClassLoader)라고도 불린다. 어플리케이션 레벨에 있는 클래스들을 로드한다. 즉, 사용자가 직접 저으이한 클래스 파일들을 로드한다. Classpath 환경변수에 있는 클래스 파일이나 -classpath 또는 -cp 명령어 옵션이 있는 파일들을 로드
* 링크
    * 검증 - 읽어 들인 클래스가 자바 언어 명세 및 JVM 명세에 명시된 대로 잘 구성되어 있는지 검사
    * 준비 - 클래스가 필요로 하는 메모리를 할당하고, 클래스에서 정의된 필드, 메소드, 인터페이스를 나타내는 데이터 구조를 준비
    * 분석 - 심볼릭 메모리 레퍼런스를 메소드 영역에 있는 실제 레퍼런스로 교체
* 초기화
    * 링크 과정을 거친 후 초기화 단계에서 클래스 파일의 코드를 읽게 된다.
    * java 코드에서의 class와 인터페이스의 값들을 지정된 값으로 초기화 및 초기화 메서드를 실행, 이때 JVM은 멀티 스레딩으로 동작하며, 같은 시간에 한번에 초기화 하는 경우가 있기 때문에 초기화 단계에서도 동시성을 고려해야 한다.
    * Class Loader를 통한 클래스 탑재 과정이 끝나면 본격적으로 JVM에서 클래스 파일을 구동시킬 준비가 끝난다.

### Execution Engine

* Execution Engine은 class Loader에 의해 JVM으로 로드된 바이트코드들은 Runtime Data Area의 Method Area에 배치되는데, JVM은 Method Area의 바이트 코드를 Execution Engine에 제공하여,
  Class에 정의된 내용대로 바이트 코드를 실행시킨다. 이때 로드된 바이트코드를 실행하는 Runtime Module이 Execution Engine이다.

* 구성 요소는 interpreter, JIT 컴파일러, GC등이 있다.

* **실행 방식**
    * Execution Engine은 바이트코드를 명령어 단위로 읽어서 실행하는데 두가지 방식을 혼합하여 사용

    1. interpreter 방식
        * 바이트 코드를 한 줄씩 해석하여 순차적으로 실행하는 방식이다.
        * 초기 방식으로, 속도가 느리다는 단점이 있다.

    2. JIT(Just In Time) 컴파일 방식 또는 동적 변역(Dynamic Translation)
        * 인터프리터 방식의 단점을 극복하기위해 나온 방식이 JIT 컴파일 방식이다.
        * 바이트 코드를 JIT 컴파일러를 통해 프로그램을 실제 실행하는 시점(바이트 코드를 실행하는 시점)에 각 OS에 맞는 Native Code로 변환하여 실행 속도를 개선하였다. 하지만
        , 바이트 코드를 Native Code로 변환하는데에도 비용이 소요되어, JVM은 모든 코드를 JIT 컴파일러 방식으로 실행하지 않고, 인터프리터 방식을 사용하다 일정 기준이 넘어가면 JIT 컴파일 방식으로 명령어를 실행한다.
        * JIT 컴파일러는 같은 코드를 매번 해석하지 않고, 실행할 때 컴파일을 하면서 해당 코드를 캐싱한 후 바뀐 부분만 컴파일하고, 나머지는 캐싱된 코드를 사용한다.

### Runtime Data Area

* Runtime Data Area는 JVM이 프로그램을 수행하기 위해 OS로 부터 할당 받은 메모리 공간
* 5가지 영역으로 나눌수 있다.
    * PC Register, JVM stack, Native Method stack - 각 스레드마다 고유하게 할당
    * Heap, Method Area - 모든 스레드가 공유

#### Methed Area(Static Area)

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
        * eden 영역의 unreachable 객체는 메모리에 해제
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
    * region이라는 영역으로 메모리를 잘게 쪼개서 관리
    * G1(Garbage First) GC는 바둑판의 각 영역에 객체를 할당하고 GC를 실행한다. 그러다가, 해당 영역이 꽉 차면 다른 빈 영역에, 객체를 할당하고 GC를 실행한다.
    * STOP THE WORLD 시간이 짧다.
    * Compaction를 사용한다.

* **STOP THE WORLD**
    * GC를 실행하기 위해 JVM이 애플리케이션 실행을 멈추는 것이다.
    * STOP THE WORLD가 발생하면 GC를 실행하는 스레드를 제외한 나머지     스레드는 모두 작업을 멈춘다.
    * GC 작업을 완료한 이후에 중단된 작업을 다시 시작한다.