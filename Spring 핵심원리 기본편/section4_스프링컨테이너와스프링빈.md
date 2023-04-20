**목차**
- [🧳 스프링 컨테이너 생성](#스프링-컨테이너-생성)
  - [스프링 컨테이너 생성 과정](#스프링-컨테이너-생성-과정)
  - [스프링 빈 등록](#스프링-빈-등록)
  - [스프링 빈 의존관계 설정 - 준비](#스프링-빈-의존관계-설정---준비)
  - [스프링 빈 의존관계 설정 - 완료](#스프링-빈-의존관계-설정---완료)
- [🫘 컨테이너에 등록된 모든 빈 조회](#-컨테이너에-등록된-모든-빈-조회)
- [🐾 스프링 빈 조회 - 기본](#스프링-빈-조회---기본)
- [👭 스프링 빈 조회 - 동일한 타입이 둘 이상](#스프링-빈-조회---동일한-타입이-둘-이상)
- [👨‍👧 스프링 빈 조회 - 상속관계](#스프링-빈-조회---상속관계)
- [🏭 BeanFactory와 ApplicationContext](#-beanfactory와-applicationcontext)
  - [BeanFactory](#beanfactory)
  - [ApplicationContext](#applicationcontext)
- [🗞 다양한 설정 형식 지원 - 자바 코드, XML](#다양한-설정-형식-지원---자바-코드-xml)
  - [애노테이션 기반 자바 코드 설정 사용](#애노테이션-기반-자바-코드-설정-사용)
  - [XML 설정 사용](#xml-설정-사용)
- [🦕 스프링 빈 설정 메타 정보 - BeanDefinition](#스프링-빈-설정-메타-정보---beandefinition)

<br>

## 🧳 스프링 컨테이너 생성

```java
ApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);
```

- `ApplicationContext` : 스프링 컨테이너, 인터페이스
- 스프링 컨테이너를 구성하는 방식에 따라 구현체 존재(XML, 애노테이션 , ..)
- 위의 코드는 자바 설정 클래스 기반으로 스프링 컨테이너 생성

> *스프링 컨테이너 부를 때 `BeanFactory` , `ApplicationContext` 구분해서 말함
일반적으로는 `ApplicationContext` 를 스프링 컨테이너라고 함*
> 

<br>

### 스프링 컨테이너 생성 과정

![Untitled](/img/spring_container_simple.png)

- `new AnnotationConfigApplicationContext(AppConfig.class)` 통해 스프링 컨테이너 생성
- 구성 정보 지정 필요 - `AppConfig` 로 구성 정보 지정

<br>

### 스프링 빈 등록

![Untitled](/img/reg_spring_bean.png)

- 설정 정보에서 `@Bean` 이 붙어 있는 메서드를 호출하여 **메서드 이름을 빈 이름**으로 하고 반환 객체를 빈 객체로 등록
- **빈 이름**을 직접 부여할 수도 있음 → 같은 이름을 부여하면 다른 빈 무시되거나 기존 빈을 덮어버리거나 설정에 따라 오류 발생할 수 있으므로 **항상 다른 이름을 부여해야 함**

<br>

### 스프링 빈 의존관계 설정 - 준비

![Untitled](/img/prepare_di.png)

<br>

### 스프링 빈 의존관계 설정 - 완료

![Untitled](/img/done_di.png)

- 설정 정보를 참고하여 의존관계 주입 (DI)
- 동적인 인스턴스 의존 관계 모두 연결
- 단순히 자바 코드를 호출하는 것 같지만 차이 존재

> *스프링은 빈 생성과 의존관계 주입하는 단계가 나눠져 있지만 자바 코드로 스프링 빈을 등록하면 생성자를 호출하면서 의존 관계 주입도 한 번에 처리됨*
> 

<br>

## 🫘 컨테이너에 등록된 모든 빈 조회

```java
public class ApplicationContextInfoTest {

    AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);

    @Test
    @DisplayName("모든 빈 출력하기")
    void findAllBean() {
        String[] beanDefinitionNames = ac.getBeanDefinitionNames();
        for (String beanDefinitionName : beanDefinitionNames) {
            Object bean = ac.getBean(beanDefinitionName);
            System.out.println("name = " + beanDefinitionName + " object = " + bean);
        }
    }
}
```

- `ac.getBeanDefinitionNames()` : 등록되어 있는 모든 빈의 이름 가져옴

**[출력]**

```bash
name = org.springframework.context.annotation.internalConfigurationAnnotationProcessor object = org.springframework.context.annotation.ConfigurationClassPostProcessor@708400f6
name = org.springframework.context.annotation.internalAutowiredAnnotationProcessor object = org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor@5829e4f4
name = org.springframework.context.annotation.internalCommonAnnotationProcessor object = org.springframework.context.annotation.CommonAnnotationBeanPostProcessor@4218500f
name = org.springframework.context.event.internalEventListenerProcessor object = org.springframework.context.event.EventListenerMethodProcessor@4bff64c2
name = org.springframework.context.event.internalEventListenerFactory object = org.springframework.context.event.DefaultEventListenerFactory@1b2c4efb
name = appConfig object = hello.core.AppConfig$$EnhancerBySpringCGLIB$$594ccebb@c35172e
name = memberService object = hello.core.member.MemberServiceImpl@c2db68f
name = memberRepository object = hello.core.member.MemoryMemberRepository@3cc41abc
name = orderService object = hello.core.order.OrderServiceImpl@4566d049
name = discountPolicy object = hello.core.discount.RateDiscountPolicy@61ce23
```

스프링에서 자동으로 등록하는 빈과 주입하고자 설정한 빈과 구분하고 싶음

- `ac.getBeanDefinition()` : 빈에 대한 정보들, 메타데이터 정보
- `beanDefinition.getRole() == BeanDefinition.ROLE_APPLICATION` : Application에서 등록한 빈인지 확인
    - **ROLE_APPLICATION** : 직접 등록한 애플리케이션 빈
    - **ROLE_INFRASTRUCTURE** : 스프링이 내부에서 사용하는 빈

**[출력]**

```bash
name = appConfig object = hello.core.AppConfig$$EnhancerBySpringCGLIB$$594ccebb@708400f6
name = memberService object = hello.core.member.MemberServiceImpl@5829e4f4
name = memberRepository object = hello.core.member.MemoryMemberRepository@4218500f
name = orderService object = hello.core.order.OrderServiceImpl@4bff64c2
name = discountPolicy object = hello.core.discount.RateDiscountPolicy@1b2c4efb
```

<br>

## 🐾 스프링 빈 조회 - 기본

- `ac.getBean(빈이름, 타입)`
- `ac.getBean(타입)`
- 조회 대상 스프링 빈이 없다면 예외 발생 - `NoSuchBeanDefinitionException`

```java
public class ApplicationContextBasicFindTest {

    AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);

    @Test
    @DisplayName("빈 이름으로 조회")
    void findBeanByName() {
        MemberService memberService = ac.getBean("memberService", MemberService.class);
        assertThat(memberService).isInstanceOf(MemberServiceImpl.class);
    }

    @Test
    @DisplayName("이름 없이 타입으로만 조회")
    void findBeanByType() {
        MemberService memberService = ac.getBean(MemberService.class);
        assertThat(memberService).isInstanceOf(MemberServiceImpl.class);
    }

    @Test
    @DisplayName("구체 타입으로 조회")
    void findBeanByName2() {
        MemberService memberService = ac.getBean("memberService", MemberServiceImpl.class);
        assertThat(memberService).isInstanceOf(MemberServiceImpl.class);
    }

    @Test
    @DisplayName("빈 이름으로 조회X")
    void findByBeanNameX() {
        assertThrows(NoSuchBeanDefinitionException.class,
            () -> ac.getBean("xxxxx", MemberService.class));
    }
}
```

<br>

## 👭 스프링 빈 조회 - 동일한 타입이 둘 이상

타입으로 조회했을 때 같은 타입의 스프링 빈이 둘 이상이면 오류 발생 `NoUniqueBeanDefinitionException` ⇒ **빈 이름 지정 필요**

`ac.getBeansOfType(type)` : 해당 타입의 모든 빈 조회

```java
public class ApplicationContextSameBeanFindTest {

    AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(SameBeanConfig.class);

    @Test
    @DisplayName("타입으로 조회시 같은 타입이 둘 이상 있으면, 중복 오류가 발생한다")
    void findBeanByTypeDuplicate() {
        Assertions.assertThrows(NoUniqueBeanDefinitionException.class,
            () -> ac.getBean(MemberRepository.class));
    }

    @Test
    @DisplayName("타입으로 조회시 같은 타입이 둘 이상 있으면, 빈 이름을 지정하면 된다")
    void findBeanByName() {
        MemberRepository memberRepository = ac.getBean("memberRepository1", MemberRepository.class);
        assertThat(memberRepository).isInstanceOf(MemberRepository.class);
    }

    @Test
    @DisplayName("특정 타입을 모두 조회하기")
    void findAllBeanByType() {
        Map<String, MemberRepository> beansOfType = ac.getBeansOfType(MemberRepository.class);
        for (String key : beansOfType.keySet()) {
            System.out.println("key = " + key + " value = " + beansOfType.get(key));
        }
        System.out.println("beansOfType = " + beansOfType);
        assertThat(beansOfType.size()).isEqualTo(2);
    }

    @Configuration
    static class SameBeanConfig {

        @Bean
        public MemberRepository memberRepository1() {
            return new MemoryMemberRepository();
        }

        @Bean
        public MemberRepository memberRepository2() {
            return new MemoryMemberRepository();
        }
    }
}
```

<br>

## 👨‍👧 스프링 빈 조회 - 상속관계

부모 타입으로 조회하면 자식 타입도 함께 조회함

여러 개인 경우 ⇒ **빈 이름 지정 or 하위 타입으로 조회(추천X)**

```java
public class ApplicationContextExtendsTest {

    AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(TestConfig.class);

    @Test
    @DisplayName("부모 타입으로 조회 시, 자식이 둘 이상 있으면 중복 오류가 발생한다")
    void findBeanByTypeDuplicate() {
        assertThrows(NoUniqueBeanDefinitionException.class,
            () -> ac.getBean(DiscountPolicy.class));
    }

    @Test
    @DisplayName("부모 타입으로 조회 시, 자식이 둘 이상 있으면 빈 이름을 지정하면 된다")
    void findBeanByParentTypeBeanName() {
        DiscountPolicy rateDiscountPolicy = ac.getBean("rateDiscountPolicy", DiscountPolicy.class);
        assertThat(rateDiscountPolicy).isInstanceOf(RateDiscountPolicy.class);
    }

    @Test
    @DisplayName("특정 하위 타입으로 조회 - 추천X")
    void findBeanBySubType() {
        RateDiscountPolicy bean = ac.getBean(RateDiscountPolicy.class);
        assertThat(bean).isInstanceOf(RateDiscountPolicy.class);
    }

    @Test
    @DisplayName("부모 타입으로 모두 조회하기")
    void findAllBeanByParentType() {
        Map<String, DiscountPolicy> beansOfType = ac.getBeansOfType(DiscountPolicy.class);
        assertThat(beansOfType.size()).isEqualTo(2);
        for (String key : beansOfType.keySet()) {
            System.out.println("key = " + key + " value = " + beansOfType.get(key));
        }
    }

    @Test
    @DisplayName("부모 타입으로 모두 조회하기 - Object")
    void findAllBeanByObjectType() {
        Map<String, Object> beansOfType = ac.getBeansOfType(Object.class);
        for (String key : beansOfType.keySet()) {
            System.out.println("key = " + key + " value = " + beansOfType.get(key));
        }
    }

    @Configuration
    static class TestConfig {

        @Bean
        public DiscountPolicy rateDiscountPolicy() {
            return new RateDiscountPolicy();
        }

        @Bean
        public DiscountPolicy fixDiscountPolicy() {
            return new FixDiscountPolicy();
        }
    }
}
```

<br>

## 🏭 BeanFactory와 ApplicationContext

![Untitled](/img/beanfactory_ac.png)

<br>

### BeanFactory

스프링 컨테이너 최상위 인터페이스

스프링 빈 관리,조회 역할 담당

`getBean()` 제공

<br>

### ApplicationContext

`BeanFactory` 기능 모두 상속받아 제공

일반적으로 `BeanFactory` 대신 `ApplicationContext` 사용

애플리케이션 개발할 때 빈 관리, 조회 기능뿐만 아니라 **수많은 부가 기능** 필요!

- **메시지소스를 활용한 국제화 기능** : 언어권에 따라 해당 언어 출력 지원
- **환경변수** : 로컬, 개발, 운영 등을 구분해서 처리 ex) 환경별로 DB 연결
- **애플리케이션 이벤트** : 이벤트를 발행하고 구독하는 모델 편리하게 지원
- **편리한 리소스 조회** : 파일, 클래스패스, 외부 등에서 리소스 편리하게 조회

<br>

## 🗞 다양한 설정 형식 지원 - 자바 코드, XML

스프링 컨테이너는 다양한 형식의 설정 정보를 사용할 수 있도록 유연하게 설계되어 있음

- 자바 코드, XML, Groovy, ..

![Untitled](/img/various_config.png)

<br>

### 애노테이션 기반 자바 코드 설정 사용

- `new AnnotationConfigApplicationContext(AppConfig.class)`
- 자바 코드로된 설정 정보를 넘기면 됨

<br>

### XML 설정 사용

- 최근에는 스프링 부트를 많이 사용하면서 XML 기반 설정은 잘 사용하지 않음
- 하지만 아직 많은 레거시 프로젝트들이 XML로 되어 있음
- 컴파일 없이 설정 정보를 변경할 수 있는 장점도 존재

```java
<bean id="memberService" class="hello.core.member.MemberServiceImpl">
    <constructor-arg name="memberRepository" ref="memberRepository"/>
</bean>
```

`<bean>` : bean 등록

`<constructor-arg>` : 의존관계 주입

<br>

## 🦕 스프링 빈 설정 메타 정보 - BeanDefinition

정보에 관한 것을 **추상화**시켰기 때문에 다양한 설정 형식 지원 가능! `BeanDefinition` 

즉 역할과 구현을 개념적으로 나눈 것

- XML → BeanDefinition
- 자바 코드 → BeanDefinition

`BeanDefinition` 은 빈 설정 메타정보라고 함

- @Bean , <bean> 당 각각 하나씩 메타 정보 생성

스프링 컨테이너는 이 메타정보를 기반으로 스프링 빈을 생성

![Untitled](/img/beandefinition.png)

추상화에만 의존하도록 설계

<br>

![Untitled](/img/beandefinition_detail.png)

`AnnotationBeanDefinitionReader` 를 사용해서 `AppConfig` 를 읽어 `BeanDefinition` 생성

```java
public class BeanDefinitionTest {

    AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);

    @Test
    @DisplayName("빈 설정 메타정보 확인")
    void findApplicationBean() {
        String[] beanDefinitionNames = ac.getBeanDefinitionNames();
        for (String beanDefinitionName : beanDefinitionNames) {
            BeanDefinition beanDefinition = ac.getBeanDefinition(beanDefinitionName);

            if (beanDefinition.getRole() == BeanDefinition.ROLE_APPLICATION) {
                System.out.println("beanDefinitionName = " + beanDefinitionName + " beanDefinition = " + beanDefinition);
            }
        }
    }
}
```

**[출력]**

```bash
beanDefinitionName = appConfig beanDefinition = Generic bean: class [hello.core.AppConfig$$EnhancerBySpringCGLIB$$594ccebb]; scope=singleton; abstract=false; lazyInit=null; autowireMode=0; dependencyCheck=0; autowireCandidate=true; primary=false; factoryBeanName=null; factoryMethodName=null; initMethodName=null; destroyMethodName=null
beanDefinitionName = memberService beanDefinition = Root bean: class [null]; scope=; abstract=false; lazyInit=null; autowireMode=3; dependencyCheck=0; autowireCandidate=true; primary=false; factoryBeanName=appConfig; factoryMethodName=memberService; initMethodName=null; destroyMethodName=(inferred); defined in hello.core.AppConfig
beanDefinitionName = memberRepository beanDefinition = Root bean: class [hello.core.AppConfig]; scope=; abstract=false; lazyInit=null; autowireMode=3; dependencyCheck=0; autowireCandidate=true; primary=false; factoryBeanName=null; factoryMethodName=memberRepository; initMethodName=null; destroyMethodName=(inferred); defined in hello.core.AppConfig
beanDefinitionName = orderService beanDefinition = Root bean: class [null]; scope=; abstract=false; lazyInit=null; autowireMode=3; dependencyCheck=0; autowireCandidate=true; primary=false; factoryBeanName=appConfig; factoryMethodName=orderService; initMethodName=null; destroyMethodName=(inferred); defined in hello.core.AppConfig
beanDefinitionName = discountPolicy beanDefinition = Root bean: class [null]; scope=; abstract=false; lazyInit=null; autowireMode=3; dependencyCheck=0; autowireCandidate=true; primary=false; factoryBeanName=appConfig; factoryMethodName=discountPolicy; initMethodName=null; destroyMethodName=(inferred); defined in hello.core.AppConfig
```

`BeanDefinition` 직접 생성해서 스프링 컨테이너에 등록할 수 있음 하지만 그럴 일은 거의 없음

스프링이 다양한 형태의 설정 정보를 `BeanDefinition` 으로 추상화해서 사용하는 것 정도만 이해하면 됨