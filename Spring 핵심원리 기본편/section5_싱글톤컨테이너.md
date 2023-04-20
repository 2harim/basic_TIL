**목차**
- [🖥 웹 애플리케이션과 싱글톤](#웹-애플리케이션과-싱글톤)
- [🚛 싱글톤 패턴](#싱글톤-패턴)
  - [싱글톤 패턴의 문제점](#싱글톤-패턴의-문제점)
- [🧳 싱글톤 컨테이너](#싱글톤-컨테이너)
  - [싱글톤 컨테이너 적용 후](#싱글톤-컨테이너-적용-후)
- [⚠️ 싱글톤 방식의 주의점](#️싱글톤-방식의-주의점)
  - [상태 유지할 경우 발생하는 문제점](#상태-유지할-경우-발생하는-문제점)
- [@Configuration과 싱글톤](#configuration과-싱글톤)
- [@Configuration과 바이트코드 조작의 마법](#configuration과-바이트코드-조작의-마법)
  - [@Configuration 없이 @Bean 만 적용한다면 ?](#configuration-없이-bean-만-적용한다면-)
  - [CGLIB 프록싱 작동 규칙](#cglib-프록싱-작동-규칙)

<br>

## 🖥 웹 애플리케이션과 싱글톤

대부분의 스프링 애플리케이션은 **웹 애플리케이션**

웹 애플리케이션은 보통 여러 고객이 동시에 요청

![Untitled](/img/basic/section5/web_lots_of_request.png)

호출할 때마다 `memberService` 객체 생성 

요청이 많아지면 더 많은 객체를 생성하고 소멸하기 때문에 메모리 낭비 심함

해결방안 : 해당 객체를 딱 1개만 생성하고 공유하도록 설계 ⇒ **싱글톤 패턴**

<br>

## 🚛 싱글톤 패턴

**클래스의 인스턴스가 딱 1개만 생성되는 것을 보장하는 디자인 패턴**

객체 인스턴스를 2개 이상 생성하지 못하도록 막아야함

- `private` 생성자 → 외부에서 임의로 new 사용하지 못하도록 막음

```java
public class SingletonService {

    private static final SingletonService instance = new SingletonService();

    public static SingletonService getInstance() {
        return instance;
    }
    private SingletonService() { }
}
```

1. static 영역에 객체 instance 미리 하나 생성
2. 해당 객체는 `getInstance()` 메서드 통해서만 조회 가능, 항상 같은 인스턴스 반환
3. 생성자를 private으로 막아서 외부에서 new 키워드로 객체 인스턴스가 생성되는 것을 막음

> *💡 `same` 은 ==(주소값 비교) 비교와 같은 의미, `equal` 은 java에서 equals(데이터 값 비교)와 같은 의미*
> 

<br>

호출할 때마다 같은 객체 인스턴스 반환

고객의 요청이 올 때마다 객체 생성하는 것이 아니라 이미 만들어진 객체를 공유해서 효율적으로 사용 가능

> *싱글톤 패턴 구현 방법은 다양, 여기서는 객체 미리 생성해두는 가장 단순하는 안전한 방법 선택함*
> 

<br>

### 싱글톤 패턴의 문제점

- 싱글톤 패턴 구현 코드 자체가 많이 들어감
- 의존관계상 클라이언트가 구체 클래스에 의존 → DIP 위반, OCP 위반가능성 ↑
- 테스트 어려움
- 내부 속성 변경, 초기화 어려움
- private 생성자로 인해 자식 클래스 생성 어려움

⇒ 유연성 떨어짐, 안티 패턴으로 불리기도 함

<br>

## 🧳 싱글톤 컨테이너

싱글톤 패턴 적용하지 않아도 객체 인스턴스를 싱글톤으로 관리함

스프링 컨테이너는 싱글톤 컨테이너 역할을 함

- 싱글톤 레지스트리 : 싱글톤 객체 생성하고 관리하는 기능

스프링 컨테이너의 기능 덕분에 싱글톤 패턴의 단점 해결하면서 객체 싱글톤 유지 가능

- 싱글톤 패턴 위한 지저분한 코드 X
- DIP, OCP, test, private 생성자로부터 자유롭게 싱글톤 사용 O

<br>

### 싱글톤 컨테이너 적용 후

![Untitled](/img/basic/section5/singleton_container.png)

고객 요청이 올 때마다 객체 생성하는 것이 아니라 이미 만들어진 객체 공유해서 효율적으로 재사용 가능

> *스프링의 기본 빈 등록 방식은 싱글톤, 하지만 스프링이 싱글톤 방식만 지원하는 것은 아님*
> 

<br>

## ⚠️ 싱글톤 방식의 주의점

**객체 인스턴스를 하나만 생성해서 공유하는 싱글톤 방식은 싱글톤 객체 상태를 stateful하게 설계하면 안된다**

즉, **무상태(stateless)** 로 설계해야 함

- 특정 클라이언트에 의존적인 필드 X
- 특정 클라이언트가 값 변경할 수 있는 필드 X
- 가급적 읽기만 가능
- 필드 대신 자바에서 공유되지 않는, 지역 변수, 파라미터, ThreadLocal 등 사용해야 함

<br>

### 상태 유지할 경우 발생하는 문제점

```java
public class StatefulService {

    private int price; //상태 유지하는 필드

    public void order(String name, int price) {
        System.out.println("name = " + name + " price = " + price);
        this.price = price; // 여기가 문제
    }

    public int getPrice() {
        return price;
    }
}

	@Test
	void statefulServiceSingleton() {
	    ApplicationContext ac = new AnnotationConfigApplicationContext(TestConfig.class);
	    StatefulService statefulService1 = ac.getBean(StatefulService.class);
	    StatefulService statefulService2 = ac.getBean(StatefulService.class);
	
	    //ThreadA : 사용자A 10000원 주문
	    statefulService1.order("userA", 10000);
	    //ThreadB : 사용자B 20000원 주문
	    statefulService2.order("userB", 20000);
	
	    //ThreadA : 사용자A 주문 금액 조회
	    int price = statefulService1.getPrice();
	    System.out.println("price = " + price);
	
	    Assertions.assertThat(statefulService1.getPrice()).isEqualTo(20000);
	}
```

`StatefulService` 의 `price` 필드는 공유되는 필드인데 특정 클라이언트가 값을 변경함

사용자의 주문금액이 잘못 출력되는 것을 확인

→ 공유 필드는 진짜진짜 조심! 항상 **무상태**로 설계 필요

<br>

**[변경]**

```java
public int order(String name, int price) {
    System.out.println("name = " + name + " price = " + price);
    return price; //이렇게 고쳐서 공유 필드 변경을 막음
}
```

<br>

## @Configuration과 싱글톤

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
        return new RateDiscountPolicy();
    }
}
```

@Bean `memberService` -> `new MemoryMemberRepository()`
<br>
@Bean `orderService` -> `new MemoryMemberRepository()` 

new 키워드를 사용한 객체 생성이 두 번 일어남 → 싱글톤 ?

**실제로 한 번만 호출돼서 같은 인스턴스임**

<br>

**[출력]**

```bash
call AppConfig.memberService
11:37:25.328 [main] DEBUG org.springframework.beans.factory.support.DefaultListableBeanFactory - Creating shared instance of singleton bean 'memberRepository'
call AppConfig.memberRepository
11:37:25.328 [main] DEBUG org.springframework.beans.factory.support.DefaultListableBeanFactory - Creating shared instance of singleton bean 'orderService'
call AppConfig.orderService
11:37:25.329 [main] DEBUG org.springframework.beans.factory.support.DefaultListableBeanFactory - Creating shared instance of singleton bean 'discountPolicy'
call AppConfig.discountPolicy
```

`memberService` → `memberRepository` → `orderService` → `discountPolicy`

spring이 싱글톤 보장해줌!

<br>

## @Configuration과 바이트코드 조작의 마법

스프링 컨테이너는 싱글톤 레지스트리

따라서 스프링 빈이 싱글톤 되도록 보장 필요

→ 스프링은 클래스의 바이트코드를 조작하는 라이브러리 사용 `@Configuration` 

```java
ApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);
AppConfig bean = ac.getBean(AppConfig.class);

System.out.println("bean = " + bean.getClass());
```

<br>

**[출력]**

```bash
bean = class hello.core.AppConfig$$EnhancerBySpringCGLIB$$75206e4d
```

순수한 클래스라면 `class hello.core.AppConfig` 로 출력되어야 함

하지만 xxxCGLIB가 붙으면서 복잡해짐 

- 스프링이 **CGLIB** 라는 바이트코드 조작 라이브러리를 사용해서 `AppConfig` 클래스를 상속받은 임의의 다른 클래스를 만들고 그 다른 클래스를 스프링 빈으로 등록한 것
- 임의의 다른 클래스가 싱글톤 보장하도록 해줌!

![Untitled](/img/basic/section5/cglib.png)

<br>

**동작방식**

등록하고자 하는 빈이 이미 스프링 컨테이너에 등록되어 있다면 ? 스프링 컨테이너에서 찾아서 반환

없다면 ? 객체 생성하고 스프링 컨테이너에 등록 후 반환

> *AppConfig@CGLIB 는 AppConfig의 자식 타입이어서 AppConfig 타입으로 조회 가능*
> 

<br>

### @Configuration 없이 @Bean 만 적용한다면 ?

안붙여도 스프링 빈으로 다 등록됨

하지만 CGLIB 기술 없이 순수한 `AppConfig` 로 스프링 빈에 등록됨 → 싱글톤 보장 X

스프링 설정 정보는 항상 `@Configuration` 사용하자

<br>

### CGLIB 프록싱 작동 규칙

스프링 컨테이너 확인할 때 intercept 하여 새로운 빈을 생성할지를 확인

1. 상속을 통해 작동
    - @Configuration 클래스는 final 이면 X
    - @Bean 메서드는 final 이면 X
    - @Bean 메서드는 private 이면 X

1. 정적 @Bean 메서드
   
    @Bean을 static 선언하는 경우 스프링은 @Configuration으로 둘러싼 클래스를 초기화 할 필요 없어짐
    
    자바는 static 메서드에 대한 오버라이딩 허용 X → CGLIB proxing은 static bean definition에 대해 작동 X
    

[참고] [http://www.javabyexamples.com/cglib-proxying-in-spring-configuration](http://www.javabyexamples.com/cglib-proxying-in-spring-configuration)