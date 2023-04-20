**목차**
- [🐥 새로운 할인 정책 개발](#새로운-할인-정책-개발)
  - [RateDiscountPolicy 추가](#ratediscountpolicy-추가)
- [🚨 새로운 할인 정책 적용과 문제점](#새로운-할인-정책-적용과-문제점)
  - [문제점](#문제점)
  - [문제 해결](#문제-해결)
  - [해결 방안](#해결-방안)
- [🤔 관심사의 분리](#관심사의-분리)
  - [AppConfig](#appconfig)
  - [AppConfig 실행](#appconfig-실행)
  - [정리](#정리)
- [⛑ AppConfig 리팩터링](#appconfig-리팩터링)
- [🌊 새로운 구조와 할인 정책 적용](#새로운-구조와-할인-정책-적용)
- [🥝 전체 흐름 정리](#전체-흐름-정리)
  - [새로운 할인 정책 개발](#새로운-할인-정책-개발-1)
  - [새로운 할인 정책 적용과 문제점](#새로운-할인-정책-적용과-문제점-1)
  - [관심사 분리](#관심사-분리)
  - [AppConfig 리팩터링](#appconfig-리팩터링-1)
  - [새로운 구조와 할인 정책 적용](#새로운-구조와-할인-정책-적용-1)
- [🚧 좋은 객체 지향 설계 5가지 원칙 적용](#좋은-객체-지향-설계-5가지-원칙-적용)
  - [SRP 단일 책임 원칙](#srp-단일-책임-원칙)
  - [DIP 의존관계 역전 원칙](#dip-의존관계-역전-원칙)
  - [OCP 개방-폐쇄 원칙](#ocp-개방-폐쇄-원칙)
- [🏭 IoC, DI, 그리고 컨테이너](#ioc-di-그리고-컨테이너)
  - [제어의 역전 Inversion Of Control](#제어의-역전-inversion-of-control)
  - [의존관계 주입 Dependency Injection](#의존관계-주입-dependency-injection)
  - [IoC 컨테이너, DI 컨테이너](#ioc-컨테이너-di-컨테이너)
- [🌱 스프링으로 전환하기](#스프링으로-전환하기)
  - [스프링 컨테이너](#스프링-컨테이너)


<br>

## 🐥 새로운 할인 정책 개발

정액 할인 정책에서 정률 할인 정책으로 변경 필요 → `RateDiscountPolicy` 추가

<br>
<h3>
<details>
<summary>애자일 소프트웨어 개발 선언</summary>

```markdown
공정과 도구보다 개인과 상호작용을
포괄적인 문서보다 작동하는 소프트웨어를
계약 협상보다 고객과의 협력을
계획을 따르기보다 변화에 대응하기를
가치 있게 여긴다
```

</details>
</h3>

<br>

### RateDiscountPolicy 추가

RateDiscountPolicy → VIP는 금액의 10% 할인

```java
public class RateDiscountPolicy implements DiscountPolicy {

    private int discountPercent = 10; //할인율

    @Override
    public int discount(Member member, int price) {
        if (member.getGrade() == Grade.VIP) {
            return price * discountPercent / 100;
        } else {
            return 0;
        }
    }
}
```

RateDiscountPolicyTest

```java
class RateDiscountPolicyTest {

    RateDiscountPolicy discountPolicy = new RateDiscountPolicy();

    @Test
    @DisplayName("VIP는 10%할인이 적용되어야 한다")
    void vip_o() {
        //given
        Member member = new Member(1L, "memberA", Grade.VIP);

        //when
        int discount = discountPolicy.discount(member, 10000);

        //then
        Assertions.assertThat(discount).isEqualTo(1000);
    }

		@Test
    @DisplayName("VIP가 아니면 할인이 적용되지 않아야 한다")
    void vip_x() {
        //given
        Member member = new Member(2L, "memberBASIC", Grade.BASIC);

        //when
        int discount = discountPolicy.discount(member, 10000);

        //then
        Assertions.assertThat(discount).isEqualTo(0);
    }
}
```

작성한 로직이 맞는지 확인하기 위해서 **성공, 실패 테스트 모두 작성** 필요

> 💡`option` + `Enter` →  Add on-demand static import for assertThat 적용
>

<br>

## 🚨 새로운 할인 정책 적용과 문제점

추가한 `RateDiscountPolicy` 를 애플리케이션에 적용

```java
public class OrderServiceImpl implements OrderService {

    private final MemberService memberService = new MemberServiceImpl();
//    private final DiscountPolicy discountPolicy = new FixDiscountPolicy();
    private final DiscountPolicy discountPolicy = new RateDiscountPolicy();
	...
```

위와 같이 클라이언트인 `OrderServiceImpl` 코드 변경 필요 😡

<br>

### 문제점

역할과 구현 분리 O

다형성 활용, 인터페이스와 구현 객체 분리 O

OCP, DIP .. 객체지향 설계 원칙 준수 ? X

- **DIP 위반** 
: OrderServiceImpl은
    
    추상 - `DiscountPolicy`
    구현 - `FixDiscountPolicy`, `RateDiscountPolicy` 
    
    모두 의존하고 있음
    
- **OCP 위반**
    
    현재 코드는 기능을 확장해서 변경하면 클라이언트 코드인 `OrderServiceImpl` 영향 줌
    
<br>

실제 의존 관계 (DIP 위반)

![Untitled](/img/basic/section3/dip_x.png)

정책 변경하면 (OCP 위반)

![Untitled](/img/basic/section3/ocp_x.png)

<br>

### 문제 해결

추상(인터페이스)에만 의존하도록 변경

![Untitled](/img/basic/section3/solve.png)

OrderServiceImpl 코드 변경

```java
public class OrderServiceImpl implements OrderService {
	
	private DiscountPolicy discountPolicy;
	...
```

하지만 구현체가 없기 때문에 **NPE** 발생

<br>

### 해결 방안

‘누군가’ 클라이언트인 `OrderServiceImpl` 에 `DiscountPolicy` 의 구현 객체를 대신 생성하고 주입해야 함

<br>

## 🤔 관심사의 분리

애플리케이션 ⇒ 하나의 공연으로 생각

배역을 정하는 것은 배우들이 하는 것이 아님

공연도 하고 주인공도 섭외하는 것은 **다양한 책임**을 갖게 되는 것

<br>

**관심사를 분리하자**

배우는 본인 역할인 배역 수행하는 것에만 집중

공연 구성하고 배우 섭외하고 역할 지정하는 것은 `공연기획자` 의 역할

각각의 책임을 확실히 분리

<br>

### AppConfig

애플리케이션 전체 동작 방식을 구성(config)하기 위해 **구현 객체 생성하고 연결**하는 책임 갖는 별도의 설정 클래스 생성

AppConfig

```java
public class AppConfig {

    public MemberService memberService() {
        return new MemberServiceImpl(new MemoryMemberRepository());
    }
	...
}
```

애플리케이션 실제 동작에 필요한 구현 객체 생성

- MemberServiceImpl
- MemoryMemberRepository
- OrderServiceImpl
- FixDiscountPolicy

생성한 객체 인스턴스의 참조를 생성자를 통해 주입 

- MemberServiceImpl → MemoryMemberRepository
- OrderServiceImpl → MemoryMemberRepository, FixDiscountPolicy

<br>

MemberServiceImpl

```java
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    public MemberServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }
	...
}
```

`MemoryMemberRepository` 에 대한 코드 사라짐, interface에만 의존

어떤 구현 객체를 주입할지는 외부에서 결정(AppConfig)

의존관계에 대한 고민은 외부에 맡기고 실행에만 집중 ⇒ 관심사 분리!

![클래스다이어그램](/img/basic/section3/appconfig_cld.png)

클래스다이어그램

DIP 완성 → 추상에만 의존, 구체 클래스 몰라도 됨

<br>

![회원 객체 인스턴스 다이어그램](/img/appconfig_od.png)

회원 객체 인스턴스 다이어그램

`appConfig`가 `memoryMemberRepository` 객체 생성 후 참조값을 `memberServiceImpl` 생성하면서 생성자로 전달

클라이언트 `memberServiceImpl` 입장에서 보면 의존관계를 마치 외부에서 주입해주는 것 같음 ⇒ **DI 의존관계 주입**

<br>

### AppConfig 실행

MemberApp

```java
AppConfig appConfig = new AppConfig();
MemberService memberService = appConfig.memberService();
```

MemberServiceTest

```java
public class MemberServiceTest {

    MemberService memberService;

    @BeforeEach
    public void beforeEach() {
        AppConfig appConfig = new AppConfig();
        memberService = appConfig.memberService();
    }
	...
}
```

> 💡 @BeforeEach : 테스트 메서드 실행 이전에 수행, 보통 초기화 작업
> 

<br>

### 정리

`AppConfig` 통해 관심사 분리 → 구현 클래스 선택, 애플리케이션이 어떻게 동작하는지 전체 구성 책임

<br>

## ⛑ AppConfig 리팩터링

현재 `AppConfig` 엔 중복 존재, 역할에 따른 구현 잘 안보임

<br>

AppConfig

```java
public class AppConfig {

    public MemberService memberService() {
        return new MemberServiceImpl(memberRepository());
    }

    **private static MemoryMemberRepository memberRepository() { //중복 제거
        return new MemoryMemberRepository();
    }**

    public OrderService orderService() {
        return new OrderServiceImpl(memberRepository(), discountPolicy());
    }
    
    **public DiscountPolicy discountPolicy() {
        return new FixDiscountPolicy();
    }**
}
```

역할과 구현 클래스가 한 눈에 들어오도록 변경

<br>

## 🌊 새로운 구조와 할인 정책 적용

정액 할인 정책 → 정률 할인 정책 변경

![Untitled](/img/basic/section3/use_config.png)

`AppConfig` 등장으로 애플리케이션 → **사용 영역 / 객체 생성 및 구성 영역**으로 분리

따라서 할인 정책을 바꾸고 싶다면 `AppConfig` 만 수정하면 됨

<br>

AppConfig

```java
public DiscountPolicy discountPolicy() {
//        return new FixDiscountPolicy();
      return new RateDiscountPolicy();
  }
```

클라이언트인 `OrderServiceImpl` 을 포함한 사용 영역의 어떤 코드도 변경할 필요 없음

**DIP, OCP 만족**

<br>

## 🥝 전체 흐름 정리

### 새로운 할인 정책 개발

> 다형성으로 새로운 정률 할인 정책 코드 추가 가능

<br>

### 새로운 할인 정책 적용과 문제점

> 새로운 정률 할인 정책 적용하려고 하니 클라이언트 코드인 주문 서비스 구현체도 함께 변경 필요
<br>
클라이언트가 인터페이스뿐만 아니라 구현 클래스에도 함께 의존 → **DIP위반**

<br>

### 관심사 분리

> 공연 기획자인 AppConfig 등장 
<br>
애플리케이션 전체 동작 방식을 구성하기 위해 `구현 객체 생성하고 연결하는 책임` 담당
<br>
클라이언트 객체는 자신의 역할을 실행하는 것만 집중, 책임 명확

<br>

### AppConfig 리팩터링

> 구성 정보에서 역할, 구현 명확하게 분리 → 역할 잘 드러남, 중복 제거

<br>

### 새로운 구조와 할인 정책 적용

> 정액 할인 정책 → 정률 할인 정책 변경
<br>
AppConfig 등장으로 애플리케이션 사용 영역, 구성 영역으로 분리됨
<br>
따라서 할인 정책 변경해도 구성 영역만 변경하면 됨

<br>

## 🚧 좋은 객체 지향 설계 5가지 원칙 적용

여기서 SRP, DIP, OCP 적용

<br>

### SRP 단일 책임 원칙

**한 클래스는 하나의 책임만 가져야 한다**

- 기존 클라이언트는 다양한 책임 가지고 있었음
- SRP 원칙 따르면서 관심사 분리 → `AppConfig` 등장 (구현 객체 생성, 연결)
- 클라이언트 객체는 **실행하는 책임**만 담당

<br>

### DIP 의존관계 역전 원칙

**프로그래머는 추상화에 의존해야지, 구체화에 의존하면 안된다**

- 새로운 할인 정책 적용하려 하니 클라이언트 코드도 함께 변경해야 했음 → 추상화, 구현 클래스 모두 의존
- 클라이언트 코드가 추상화 인터페이스에만 의존하도록 코드 변경
- 구현 객체가 필요하기 때문에 `AppConfig` 가 대신 생성해서 **의존관계 주입**

<br>

### OCP 개방-폐쇄 원칙

**소프트웨어요소는 확장에는 열려 있으나 변경에는 닫혀 있어야 한다**

- 다형성을 사용하여 DIP 지킬 수 있음
- 애플리케이션을 사용/구성 영역으로 구분
- AppConfig의 의존 관계를 변경하여 클라이언트 코드에 주입 → **확장해도 사용 영역의 변경은 닫혀 있음**

<br>

## 🏭 IoC, DI, 그리고 컨테이너

### 제어의 역전 Inversion Of Control

- 기존 프로그램은 클라이언트 객체가 스스로 필요한 구현 객체 생성, 연결, 실행
- 제어의 역전은 외부에서 제어 흐름을 갖게 되는 것

> **프레임워크 vs 라이브러리**
*프레임워크가 내가 작성한 코드를 제어하고 실행하면 프레임워크 
<br>
ex) JUnit - @Test 통해 자신만의 라이프사이클 속에서 개발자가 작성한 코드 실행
<br>
내가 작성한 코드가 직접 제어의 흐름을 담당하면 라이브러리
> 

<br>

### 의존관계 주입 Dependency Injection

- 클라이언트 객체가 어떤 구현 객체를 사용할지를 모름
- 의존 관계는 `정적인 클래스 의존 관계` 와 실행 시점에 결정되는 `동적인 객체(인스턴스) 의존 관계` 를 분리해서 생각해야 함

**정적인 클래스 의존 관계**

```
💡 클래스가 사용하는 import 코드를 보고 의존 관계를 파악할 수 있음
애플리케이션 실행 없이 분석 가능
이러한 클래스 의존관계만으로는 실제 어떤 객체가 주입될지 알 수 없음
```


클래스 다이어그램
![Untitled](/img/basic/section3/cld.png)

**동적인 객체 인스턴스 의존 관계**

```
💡 애플리케이션 실행 시점(런타임)에 실제 생성된 객체를 생성해서 클라이언트에 전달하여 클라이언트와 서버의 실제 의존 관계가 연결되는 것 = 의존 관계 주입
객체 인스턴스 생성 후 참조값 전달해서 연결

의존관계 주입을 사용하면 정적인 클래스 의존관계 변경 없이 동적인 객체 인스턴스 의존관계를 변경할 수 있음
```

객체 다이어그램
![Untitled](/img/basic/section3/od.png)

<br>

### IoC 컨테이너, DI 컨테이너

AppConfig 처럼 객체 생성, 관리, 의존관계 연결해 주는 것 

의존관계 주입에 초점을 맞추어 최근 주로 DI 컨테이너라고 함 (어샘블러, 오브젝트 팩토리 ..)

<br>

## 🌱 스프링으로 전환하기

위에서는 순수한 자바 코드로 DI 적용함 → 스프링 사용!

AppConfig

```java
@Configuration
public class AppConfig {

    @Bean
    public MemberService memberService() {
        return new MemberServiceImpl(memberRepository());
    }

    @Bean
    public static MemoryMemberRepository memberRepository() {
        return new MemoryMemberRepository();
    }

    @Bean
    public OrderService orderService() {
        return new OrderServiceImpl(memberRepository(), discountPolicy());
    }

    @Bean
    public DiscountPolicy discountPolicy() {
//        return new FixDiscountPolicy();
        return new RateDiscountPolicy();
    }
}
```

`@Configuration` : AppConfig에 설정을 구상한다는 의미

`@Bean` : 해당 어노테이션 붙인 메서드를 통해 스프링 컨테이너에 스프링 빈으로 등록

<br>

MemberApp

```java
ApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);
MemberService memberService = applicationContext.getBean(MemberService.class);
```

스프링 컨테이너인 `ApplicationContext` 을 통해 스프링 빈인 `MemberService` 가져옴

```bash
17:04:40.508 [main] DEBUG org.springframework.beans.factory.support.DefaultListableBeanFactory - Creating shared instance of singleton bean 'appConfig'
17:04:40.510 [main] DEBUG org.springframework.beans.factory.support.DefaultListableBeanFactory - Creating shared instance of singleton bean 'memberService'
17:04:40.518 [main] DEBUG org.springframework.beans.factory.support.DefaultListableBeanFactory - Creating shared instance of singleton bean 'memberRepository'
17:04:40.519 [main] DEBUG org.springframework.beans.factory.support.DefaultListableBeanFactory - Creating shared instance of singleton bean 'orderService'
17:04:40.519 [main] DEBUG org.springframework.beans.factory.support.DefaultListableBeanFactory - Creating shared instance of singleton bean 'discountPolicy'
```

@Bean을 통해 호출된 객체들이 모두 스프링 컨테이너에 등록됨

<br>

### 스프링 컨테이너

- ApplicationContext
- 이제 스프링 컨테이너를 통해 객체 사용
- 스프링 컨테이너는 `@Configuration` 이 붙은 `AppConfig` 를 설정 정보로 사용
- 여기서 `@Bean` 이 적힌 메서드 모두 호출해서 반환된 객체를 스프링 컨테이너에 등록 → 스프링 빈
    
    스프링 빈은 `@Bean` 붙은 메서드명을 스프링 빈의 이름으로 사용 
    
    `applicationContext.getBean()` 메서드로 찾을 수 있음