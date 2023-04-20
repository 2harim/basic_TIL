**목차**
- [🎬 컴포넌트 스캔과 의존관계 자동 주입 시작하기](#컴포넌트-스캔과-의존관계-자동-주입-시작하기)
  - [컴포넌트 스캔과 의존관계 주입 동작 방식](#컴포넌트-스캔과-의존관계-주입-동작-방식)
- [⚾️ 탐색 위치와 기본 스캔 대상](#️-탐색-위치와-기본-스캔-대상)
  - [탐색할 패키지 시작 위치 지정](#탐색할-패키지-시작-위치-지정)
  - [컴포넌트 스캔 기본 대상](#컴포넌트-스캔-기본-대상)
- [🥅 필터](#필터)
  - [FilterType 옵션](#filtertype-옵션)
- [💥 중복 등록과 충돌](#중복-등록과-충돌)
  - [자동 빈 등록 vs 자동 빈 등록](#자동-빈-등록-vs-자동-빈-등록)
  - [수동 빈 등록 vs 자동 빈 등록](#수동-빈-등록-vs-자동-빈-등록)

<br>

## 🎬 컴포넌트 스캔과 의존관계 자동 주입 시작하기

지금까지는 설정 정보에 직접 등록할 스프링 빈 나열했음

이렇데 등록하면 스프링 빈이 많아지게 되면 일일이 등록하기도 귀찮고 설정 정보 커지고 누락하는 문제 발생

스프링은 설정 정보 없어도 자동으로 스프링 빈 등록하는 **컴포넌트 스캔 기능 + 의존관계 자동 주입 기능 제공**

```java
@Configuration
@ComponentScan(
    excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Configuration.class)
)
public class AutoAppConfig {
}
```

- `@Configuration` : @Component 붙어 있음, 컴포넌트 스캔 대상
- `@ComponentScan` : 컴포넌트 스캔
- `excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Configuration.class)` : ANNOTATION 타입의 Configuration이 있는 설정 정보는 컴포넌트 스캔 대상에서 제외시킴 → 앞서 작성한 AppConfig, TestConfig와 같은 것을 제외시키기 위함

<br>

컴포넌트 스캔은 `@Component` 애노테이션이 붙은 클래스를 스캔해서 스프링 빈으로 등록함

컴포넌트 스캔 대상이 되고 싶은 클래스에 `@Component` 붙여야 함

```java
@Component
public class MemoryMemberRepository implements MemberRepository {}
```

<br>

AppConfig에 작성했던 의존관계 주입을 할 수 없으므로 자동 의존관계 주입이 필요 ⇒ `@Autowired`

- `MemberServiceImpl` , `OrderServiceImpl`

```java
@Autowired
public MemberServiceImpl(MemberRepository memberRepository) {
    this.memberRepository = memberRepository;
}
```

<br>

**[테스트]**

```java
@Test
void basicScan() {
  ApplicationContext ac = new AnnotationConfigApplicationContext(AutoAppConfig.class);

  MemberService memberService = ac.getBean(MemberService.class);
  assertThat(memberService).isInstanceOf(MemberService.class);
}
```
<br>

### 컴포넌트 스캔과 의존관계 주입 동작 방식

1. **@ComponentScan**
    
    ![Untitled](/img/basic/section6/componentscan.png)
    
    스프링 컨테이너가 `@Component` 붙은 클래스를  조회 후 스프링 빈 등록
    
    이때 스프링 빈 기본 이름은 클래스명 사용하되 맨 앞글자만 소문자
    
    - ex ) MemberSerivce → memberService
    - 빈 이름 직접 지정 : `@Component(”newMemberService”)` 와 같이 부여

<br>

1. **@Autowired 의존관계 자동 주입**
    
    ![Untitled](/img/basic/section6/autowired.png)
    
    생성자에 `@Autowired` 붙이면 스프링 컨테이너가 스프링 빈 찾아서 주입
    
    이때 **타입을** 기준으로 빈을 찾음 → `getBean(MemberRepository.class)` 와 동일
    
<br>

## ⚾️ 탐색 위치와 기본 스캔 대상

### 탐색할 패키지 시작 위치 지정

```java
@ComponentScan(
	  basePackages = "hello.core",
}
```

- `basePackages`  : 지정해둔 패키지부터 하위 패키지까지 탐색함
- 여러 개 지정 가능
- 지정하지 않으면 `@ComponentScan` 붙은 설정 정보 클래스의 패키지가 시작 위치가 됨

> 👍 ***권장 방법 : 설정 정보 클래스의 위치를 프로젝트 최상단에 두기, 패키지 위치 지정 X**
<br>
프로젝트 메인 설정 정보는 프로젝트를 대표하는 정보이기 때문에 프로젝트 시작 루트에 두는 것이 좋음
<br>
스프링 부트는 기본적으로 mainApplication이 프로젝트 시작 루트에 있고 위에 @SpringBootApplication 이 있다 (여기에는 @ComponentScan이 있음)*
> 

<br>

### 컴포넌트 스캔 기본 대상

- `@Component` : 컴포넌트 스캔에 사용
- `@Controller` : 스프링 MVC 컨트롤러에서 사용
- `@Service` : 스프링 비즈니스 로직에서 사용
- `@Repository` : 스프링 데이터 접근 계층에서 사용
- `@Configuration` : 스프링 설정 정보에서 사용

⇒ 모두 @Component 를 포함함

> 애노테이션은 상속 관계 따로 없음, 애노테이션이 특정 애토네이션을 들고 있을 수 있는 것은 자바가 아니라 스프링이 지원하는 기능!
> 

<br>

컴포넌트 스캔 뿐만 아니라 위의 애노테이션이 있는 경우 **부가 기능 수행**

- `@Controller` : 스프링 MVC 컨트롤러로 인식
- `@Service` : 특별한 처리 X .. 대신 개발자들이 핵심 비즈니스 로직이 있는 계층으로 인식하는데 도움
- `@Repository` : 스프링 데이터 접근 계층으로 인식, 데이터 계층의 예외를 스프링 예외로 변환
- `@Configuration` : 스프링 설정 정보로 인식, 스프링 빈이 싱글톤 유지하도록 추가 처리

<br>

## 🥅 필터

`includeFilters` : 컴포넌트 스캔 대상 추가 지정

`excludeFilters` : 컴포넌트 스캔 제외 대상 지정

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyIncludeComponent { }
```

애노테이션 생성 `MyIncludeComponent` , `MyExcludeComponent` 

- `@Target` : 선언한 애노테이션 적용될 위치 결정
    - TYPE : class, interface, enum
    - FILED : 클래스 필드
    - METHOD : aptjem
- `@Retention` : 애노테이션 어느 레벨까지 유지되는지 결정
    - SOURCE : 자바 컴파일에 의해 애노테이션 삭제
    - CLASS : 애노테이션은 .class 파일에 남아 있지만 runtime에는 제공되지 않는 애노테이션, 기본값
    - RUNTIME : runtime에도 제공되어 자바 reflection으로 선언한 애노테이션에 접근 가능
- `@Documented` : 새로 생성한 애노테이션이 자바 문서 생성시 자바 문서에도 포함시키는 애노테이션

<br>

```java
@Configuration
@ComponentScan(
    includeFilters = @Filter(type = FilterType.ANNOTATION, classes = MyIncludeComponent.class),
    excludeFilters = @Filter(type = FilterType.ANNOTATION, classes = MyExcludeComponent.class)
)
static class ComponentFilterAppConfig {
}
```

위와 같은 설정 정보를 가지고 

`MyIncludeComponent` 가 붙어있는 `BeanA` 클래스의 객체와 `MyExcludeComponent` 가 붙어있는 `BeanB`  클래스의 객체를 스프링 컨테이너에서 가져오면 beanA는 잘 가져오지만 beanB는 가져오지 못한다 (등록 X)

<br>

### FilterType 옵션

- ANNOTATION : 기본값, 애노테이션 인식해서 동작
- ASSIGNABLE_TYPE : 지정한 타입과 자식 타입 인식해서 동작, 정확한 타입 지정
- ASPECTJ : AspectJ 패턴 사용 ex) org.example..*Service*
- REGIX : 정규표현식
- CUSTOM : TypeFilter 라는 인터페이스 구현해서 처리 (조건 직접 생성해서 만들 수 있음)

```markdown
참고
@Component면 충분하기 때문에 includeFilters 사용할 일 거의 X
excludeFilter는 여러가지 이유로 간혹 사용하긴 하지만 많지는 않음
특히 스프링 부트는 컴포넌트 스캔을 기본적으로 제공하기 때문에 기본 설정에 최대한 맞추어 사용하는 것을 권장
```

<br>

## 💥 중복 등록과 충돌

컴포넌트 스캔에서 같은 빈 이름을 등록하면 ?

<br>

### 자동 빈 등록 vs 자동 빈 등록

컴포넌트 스캔에 의해 자동으로 스프링 빈이 등록되는데 이름이 같은 경우 오류 발생 시킴

`ConflictingBeanDefinitionException` 

<br>

### 수동 빈 등록 vs 자동 빈 등록

빈 등록에는 성공함

이 경우 **수동 빈 등록이 우선권 가짐** → 자동 빈 오버라이딩

하지만 현실에서는 개발자가 의도적으로 설정하기 보다는 여러 설정이 꼬여서 이런 결과가 나오는게 대부분

⇒ 최근 스프링 부트에서 수동 빈 등록과 자동 빈 등록이 충돌나면 오류 발생하도록 기본 값 바꿈

`Consider renaming one of the beans or enabling overriding by setting spring.main.allow-bean-definition-overriding=true`