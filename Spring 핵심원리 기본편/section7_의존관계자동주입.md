**목차**
- [🧩 다양한 의존관계 주입 방법](#다양한-의존관계-주입-방법)
  - [생성자 주입](#생성자-주입)
  - [수정자 주입 (setter 주입)](#수정자-주입-setter-주입)
  - [필드 주입](#필드-주입)
  - [일반 메서드 주입](#일반-메서드-주입)
- [📝 옵션 처리](#옵션-처리)
  - [자동 주입 대상을 옵션으로 처리하는 방법](#자동-주입-대상을-옵션으로-처리하는-방법)
- [🕶 생성자 주입을 선택해라](#생성자-주입을-선택해라)
  - [1. 불변](#1-불변)
  - [2. 누락](#2-누락)
  - [3. final 키워드](#3-final-키워드)
- [🌶 롬복과 최신 트렌드](#롬복과-최신-트렌드)
  - [롬복 설정](#롬복-설정)
  - [lombok 제공](#lombok-제공)
- [👿 조회 빈이 2개 이상 - 문제](#조회-빈이-2개-이상---문제)
- [🌟 @Autowired 필드명, @Qualifier, @Primary](#autowired-필드명-qualifier-primary)
  - [@Autowired 필드명 매칭](#autowired-필드명-매칭)
  - [@Qualifier 사용](#qualifier-사용)
  - [@Primary 사용](#primary-사용)
  - [@Primary, @Qualifier 활용](#primary-qualifier-활용)
- [🥣 애노테이션 직접 만들기](#애노테이션-직접-만들기)
- [🚃 조회한 빈이 모두 필요할 때, List, Map](#조회한-빈이-모두-필요할-때-list-map)
- [🕴 자동, 수동의 올바른 실무 운영 기준](#자동-수동의-올바른-실무-운영-기준)
  - [편리한 자동 기능 기본으로 사용하자](#편리한-자동-기능-기본으로-사용하자)
  - [수동 빈 등록은 언제 사용하면 좋을까?](#수동-빈-등록은-언제-사용하면-좋을까)
  - [수동 빈 → 비즈니스 로직 중 다형성 적극 활용할 때](#수동-빈--비즈니스-로직-중-다형성-적극-활용할-때)

<br>

## 🧩 다양한 의존관계 주입 방법

**의존관계 주입 방법**

- 생성자 주입
- 수정자 주입(setter)
- 필드 주입
- 일반 메서드 주입

<br>

### 생성자 주입

생성자를 통해 의존관계 주입 받는 방식

생성자 호출 시점에 **딱 한 번만 호출**되는 것이 보장됨 ⇒ **불변, 필수** 의존관계에 사용

생성자에서 모든 파라미터를 안넣어주면 오류 발생 

생성자가 딱 1개만 있으면 `@Autowired` 생략 가능

```java
@Autowired
public OrderServiceImpl(MemberRepository memberRepository, DiscountPolicy discountPolicy) {
    this.memberRepository = memberRepository;
    this.discountPolicy = discountPolicy;
}
```

> 일반적으로는 스프링 빈 등록 후 의존관계 주입 따로!
반면 생성자 주입은 등록과 의존관계 주입이 동시에 일어남
>

<br>

### 수정자 주입 (setter 주입)

setter라고 불리는 필드 값 변경하는 수정자 메서드 통해 의존관계 주입 방식

**선택, 변경** 가능성 있는 의존관계에 사용

```java
@Autowired
public void setMemberRepository(MemberRepository memberRepository) {
    this.memberRepository = memberRepository;
}
```

> @Autowired 기본 동작은 주입할 대상 없으면 오류 발생, 주입할 대상 없어도 동작하게 하려면 `required=false`  사용
> 

<br>

> ***자바빈 프로퍼티 규약***
필드 값 직접 변경하지 않고 setXxx, getXxx 메서드 사용해서 값 읽거나 수정
>

<br>

### 필드 주입

의존관계를 필드를 통해 바로 주입

코드 간결해서 좋은 것처럼 보이지만 `Field Injection is not recommended` 

DI 프레임워크에 의존적 

외부에서 변경 불가능해서 테스트하기 어렵다는 단점 존재 → 결국 setter를 따로 열어줘야 함

애플리케이션의 실제 코드와 관계 없는 곳(ex 테스트코드)에선 사용 OK

```java
@Autowired private MemberRepository memberRepository;
```

<br>

### 일반 메서드 주입

일반 메서드 통해 주입

한 번에 여러 필드 주입 가능

일반적으로 사용 X

```java
@Autowired
public void init(MemberRepository memberRepository, DiscountPolicy discountPolicy) {
    this.memberRepository = memberRepository;
    this.discountPolicy = discountPolicy;
}
```

<br>

## 📝 옵션 처리

주입할 스프링 빈 없어도 동작해야 할 때가 존재

`@Autowired` 만 사용하면 required 기본값이 true로 되어 있어 자동 주입 대상이 없으면 오류 발생

<br>

### 자동 주입 대상을 옵션으로 처리하는 방법

- `@Autowired(required=false)` : 자동 주입할 대상이 없으면 수정자 메서드 자체가 호출 X
- `org.springframework.lang.@Nullable` : 자동 주입할 대상이 없으면 null
- `Optional<>` : 자동 주입할 대상이 없으면 `Optional.empty`

```java
public class AutowiredTest {

    @Test
    void AutowiredOption() {
        ApplicationContext ac = new AnnotationConfigApplicationContext(TestBean.class);
    }

    static class TestBean {

        @Autowired(required = false)
        public void setNoBean1(Member noBean1) {
            System.out.println("noBean1 = " + noBean1);
        }

        @Autowired
        public void setNoBean2(@Nullable Member noBean2) {
            System.out.println("noBean2 = " + noBean2);
        }

        @Autowired
        public void setNoBean3(Optional<Member> noBean3) {
            System.out.println("noBean3 = " + noBean3);
        }
    }
}
```

<br>

**[출력]**

```bash
noBean2 = null
noBean3 = Optional.empty
```

`Member` 는 스프링 빈이 아님

`setNoBean1()` 은 호출 자체가 안됨  (⍤ `required = false` 이기 때문)

<br>

## 🕶 생성자 주입을 선택해라

최근에는 스프링 포함한 DI 프레임워크 대부분 생성자 주입 권장함

<br>

### 1. 불변

대부분의 의존관계 주입은 한 번 일어나면 애플리케이션 종료시점까지 의존관계 변경할 일 X

**오히려 대부분의 의존관계는 애플리케이션 종료 전까지 변하면 X**

수정자 주입 사용하면 setter를 public으로 열어둬야 함 → 누군가 실수로 바꿀 수 있음 변경하면 안되는 메서드 열어두는 것은 좋은 설계가 아님

생성자 주입은 객체 생성 시 딱 1번 호출되므로 불변하게 설계 가능

<br>

### 2. 누락

프레임워크 없이 순수한 자바 코드를 단위 테스트 하는 경우 수정자 주입인 경우 실행은 되나 NPE 발생(의존관계 주입이 누락되었기 때문)

생성자 주입은 필수로 받아야 하는 것들이 없다면 **컴파일 오류** 발생 → 어떤 값을 필수로 주입해야 하는지 알 수 있음

```java
@Test
void createOrder() {
    MemoryMemberRepository memberRepository = new MemoryMemberRepository();
    memberRepository.save(new Member(1L, "name", Grade.VIP));

    OrderServiceImpl orderService = new OrderServiceImpl(memberRepository, new FixDiscountPolicy());
    Order order = orderService.createOrder(1L, "itemA", 10000);
    Assertions.assertThat(order.getDiscountPrice()).isEqualTo(1000);
}
```

<br>

### 3. final 키워드

생성자 주입을 사용하면 필드에 final 키워드 사용할 수 있음

생성자에서 값이 설정되지 않는 오류를 **컴파일 시점에 막아줌**

수정자 주입 포함 나머지 방식은 생성자 이후 호출되므로 필드에 final 키워드 사용 X

생성자 주입 방식은 프레임워크에 의존하지 않고 순수한 자바 언어의 특징을 잘 살리는 방법

기본으로 생성자 주입 사용하고 필수 값이 아닌 경우에만 수정자 주입 방식을 옵션으로 부여 (동시에 사용 가능)

<br>

## 🌶 롬복과 최신 트렌드

생성자 주입, 대입 코드 등 많은 코드를 써야 함

간단하게 생성자, getter, setter 생성하는 방법!

<br>

### 롬복 설정

build.gradle

```java
plugins {
	id 'java'
	id 'org.springframework.boot' version '2.7.10'
	id 'io.spring.dependency-management' version '1.0.15.RELEASE'
}

group = 'hello'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '11'

//lombok 설정 추가 시작
configurations {
	compileOnly {
		extendsFrom annotationProcessor
	}
}
//lombok 설정 추가 끝

repositories {
	mavenCentral()
}

dependencies {
	implementation 'org.springframework.boot:spring-boot-starter'

	//lombok 라이브러리 추가 시작
	compileOnly 'org.projectlombok:lombok'
	annotationProcessor 'org.projectlombok:lombok'
	testCompileOnly 'org.projectlombok:lombok'
	testAnnotationProcessor 'org.projectlombok:lombok'
	//lombok 라이브러리 추가 끝

	testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

tasks.named('test') {
	useJUnitPlatform()
}
```

`Preferences → plugins → lombok` 설치

`Preferences → Compiler → Annotation Processors → Enable annotation processing`  

<br>

### lombok 제공

- `Getter` : get 메소드 생성
- `Setter` : set 메소드 생성
- `ToString` : 객체를 필드로 구성된 문자열 출력하는 메서드 생성
- `RequiredArgsConstructor` : final 키워드가 있는 필드로 생성자 생성

롬복 라이브러리가 제공하는 `@RequiredArgsConstructor` 사용하면 final 키워드 붙은 필드 모아서 생성자 자동으로 만들어줌 → 간단해짐

최근 **생성자 딱 1개 두고 @Autowired 생략**하는 방법 주로 사용

  \+ Lombok 라이브러리의 **@RequiredArgsConstructor** 함께 사용하면 기능 다 제공하면서 코드 더 깔끔해짐

<br>

## 👿 조회 빈이 2개 이상 - 문제

하위타입 지정할 수 있지만 DIP 위반하고 유연성 떨어짐

이름만 다르고 완전히 똑같은 타입의 스프링 빈 2개 있을 때 해결 X

수동 등록해서 문제 해결해도 되지만 자동 주입에서 해결하는 여러 방법 존재!

<br>

## 🌟 @Autowired 필드명, @Qualifier, @Primary

**조회 대상 빈이 2개 이상일 때 해결 방법**

- `@Autowired` 필드명 매칭
- `@Qualifier` → `@Qualifier` 끼리 매칭 → 빈 이름 매칭
- `@Primary` 사용

<br>

### @Autowired 필드명 매칭

`@Autowired` 는 **타입 매칭** 시도하고 이때 여러 빈이 있다면 **필드 명(파라미터 명)**으로 빈 이름 추가 매칭

```java
private DiscountPolicy discountPolicy //기존 코드

private DiscountPolicy rateDicountPolicy //변경 코드
```

<br>

### @Qualifier 사용

추가 구분자를 붙여주는 방법

빈 이름을 변경하는 것은 아님

생성자, 수정자, 필드 모두 가능

```java
@Autowired
    public OrderServiceImpl(MemberRepository memberRepository, @Qualifier("fixDiscountPolicy") DiscountPolicy discountPolicy) {
        this.memberRepository = memberRepository;
        this.discountPolicy = discountPolicy;
    }
```

<br>

**만약 `Qualifier(”fixDiscountPolicy”)` 를 못찾으면?**

`fixDiscountPolicy` 라는 이름의 스프링 빈을 추가로 찾는다

하지만 `@Qualifier` 는 Qualifier를 찾는 용도로만 사용하는 것이 명확하고 좋다

<br>

1. `@Qualifier` 끼리 매칭
2. 빈 이름 매칭
3. `NoSuchBeanDefinitionException` 발생

<br>

### @Primary 사용

가장 많이 사용!

우선순위를 정하는 방법

여러 빈이 매칭되면 `@Primary` 가 지정되어 있으면 우선권을 가짐

<br>

만약 여러 개에 붙어있다면 ??

```bash
more than one 'primary' bean found among candidates: [fixDiscountPolicy, rateDiscountPolicy]
```

위와 같은 에러 발생

<br>

### @Primary, @Qualifier 활용

**메인** DB 커넥션 획득 스프링 빈은 `@Primary` 적용

**서브** DB 커넥션 획득에는 `@Qualifier` 지정해서 명시적으로 획득하는 방식으로 많이 사용

> *@Qualifier(기본) 가 @Primary(상세) 보다 우선권이 높음*
> 

<br>

## 🥣 애노테이션 직접 만들기

`Qualifier(”fixDiscountPolicy”)` 사용하면 문자열이기 때문에 **컴파일시 타입 체크가 X**

→ **애노테이션** 만들어서 문제 해결

```java
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Qualifier("mainDiscountPolicy")
public @interface MainDiscountPolicy {
}
```

<br>

가져다 쓰는 데서도 `@Qualifier` (→ MainDiscountPolicy) 사용해야 함 

```java
@Autowired
public OrderServiceImpl(MemberRepository memberRepository, @MainDiscountPolicy DiscountPolicy discountPolicy) {
    this.memberRepository = memberRepository;
    this.discountPolicy = discountPolicy;
}
```

<br>

## 🚃 조회한 빈이 모두 필요할 때, List, Map

의도적으로 해당 타입의 스프링 빈이 다 필요한 경우 존재

예를 들어 할인 서비스를 제공하는데 클라이언트가 할인의 종류(rate, fix) 선택할 수 있다고 가정해보자

스프링을 사용하면 전략 패턴 간단하게 구현할 수 있음

```java
public class AllBeanTest {

    @Test
    void findAllBean() {
        ApplicationContext ac = new AnnotationConfigApplicationContext(AutoAppConfig.class, DiscountService.class);

        DiscountService discountService = ac.getBean(DiscountService.class);
        Member member = new Member(1L, "userA", Grade.VIP);
        int discountPrice = discountService.discount(member, 10000, "fixDiscountPolicy");
        int rDiscountPrice = discountService.discount(member, 20000, "rateDiscountPolicy");

        assertThat(discountService).isInstanceOf(DiscountService.class);
        assertThat(discountPrice).isEqualTo(1000);
        assertThat(rDiscountPrice).isEqualTo(2000);
    }

    static class DiscountService {

        private final Map<String, DiscountPolicy> policyMap;
        private final List<DiscountPolicy> policies;

        @Autowired //생략 가능
        public DiscountService(Map<String, DiscountPolicy> policyMap, List<DiscountPolicy> policies) {
            this.policyMap = policyMap;
            this.policies = policies;
            System.out.println("policyMap = " + policyMap);
            System.out.println("policies = " + policies);
        }

        public int discount(Member member, int price, String discountCode) {
            DiscountPolicy discountPolicy = policyMap.get(discountCode);

            return discountPolicy.discount(member, price);
        }
    }
}
```

`DiscountService` 는 `Map` 으로 모든 `DiscountPolicy` 주입받는다 ← `fixDiscountPolicy`  , `rateDiscountPolicy` 

`discount()` 는 `discountCode` 로 넘어온 이름에 맞게 스프링 빈을 찾아 실행한다

<br>

## 🕴 자동, 수동의 올바른 실무 운영 기준

### 편리한 자동 기능 기본으로 사용하자

스프링 나오고 점점 ***자동 선호하는 추세***

최근 스프링 부트는 컴포넌트 스캔을 기본으로 사용하고 다양한 스프링 빈들도 조건이 맞으면 자동 등록되도록 설계했음

개발자 입장에서 스프링 빈 하나 등록할 때 `@Component` 붙여주면 끝날 일을 설정 파일에 가서 `@Bean` 을 붙여 직접 객체 생성하고 주입 대상 쓰는 것은 번거로움

관리할 빈이 많아서 설정 정보가 커지면 설정 정보 관리하는 것 자체가 부담이 됨

자동 빈 등록 사용해도 OCP, DIP 지킬 수 있음!

<br>

### 수동 빈 등록은 언제 사용하면 좋을까?

애플리케이션 → 업무 로직 / 기술 지원 로직으로 구분

- **업무 로직 빈** : 컨트롤러, 서비스, 리포지토리 등 보통 비즈니스 요구사항 개발할 때 추가, 변경되는 빈
- **기술 지원 빈** : 기술적인 문제나 공통 관심사(AOP) 처리할 때 주로 사용, DB 연결, 공통 로그 처리와 같은 기술

업무 로직은 많고 개발하면 어느정도 유사 패턴 존재 → 자동 기능 적극 사용!

*기술 지원 로직은 수가 적고 보통 애플리케이션 전반에 걸쳐 광범위하게 영향을 미침*

업무 로직은 문제 발생했을 때 어디가 문제인지 명확하게 잘 보이지만 기술 지원 로직은 적용이 잘 되는지 여부조차 파악하기 어려운 경우가 많으므로 수동 빈 등록 사용해서 명확하게 보이는게 낫다

<br>

**기술 지원 객체는 수동 빈으로 등록해서 설정 정보에 바로 나타나게 하는 것이 유지보수 하기 좋다**

<br>

### 수동 빈 → 비즈니스 로직 중 다형성 적극 활용할 때

자동 등록을 사용하는 코드를 보면 한 번에 이해하기 어렵고 여러 코드를 찾아봐야 함

이 경우 수동 빈으로 등록하거나 자동으로 하는 경우 특정 패키지에 같이 묶어 두는 것이 좋음!

> *스프링과 스프링 부트가 자동으로 등록하는 빈들은 예외!*
>