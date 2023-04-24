**목차**
- [❓ 빈 스코프란](#빈-스코프란)
  - [스프링이 지원하는 스코프 종류](#스프링이-지원하는-스코프-종류)
  - [지정 방식](#지정-방식)
- [📱 프로토타입 스코프](#프로토타입-스코프)
  - [singleton](#singleton)
  - [prototype](#prototype)
- [🌪 프로토타입 스코프 - 싱글톤 빈과 함께 사용시 문제점](#프로토타입-스코프---싱글톤-빈과-함께-사용시-문제점)
  - [프로토타입 빈 직접 요청](#프로토타입-빈-직접-요청)
- [☀️ 프로토타입 스코프 - 싱글톤 빈과 함께 사용시 Provider로 문제 해결](#️프로토타입-스코프---싱글톤-빈과-함께-사용시-provider로-문제-해결)
  - [스프링 컨테이너에 요청](#스프링-컨테이너에-요청)
  - [ObjectFactory, ObjectProvider](#objectfactory-objectprovider)
  - [JSR-330 Provider](#jsr-330-provider)
  - [프로토타입 빈 언제 사용?](#프로토타입-빈-언제-사용)
- [🖥 웹 스코프](#웹-스코프)
  - [특징](#특징)
  - [종류](#종류)
- [🛝 request 스코프 예제 만들기](#-request-스코프-예제-만들기)
- [🥨 스코프와 Provider](#스코프와-provider)
- [🎩 스코프와 프록시](#-스코프와-프록시)
  - [정리](#정리)

<br>

## ❓ 빈 스코프란

스프링 빈은 스프링 컨테이너의 시작과 함께 생성돼서 컨테이너 종료될 때 유지됨

⇒ **싱글톤 스코프**(기본)로 생성되기 때문

> 스코프 : 빈이 존재할 수 있는 범위
> 

<br>

### 스프링이 지원하는 스코프 종류

- `싱글톤` : 기본, 스프링 컨테이너 시작과 종료까지 유지되는 가장 넓은 범위의 스코프
- `프로토타입` : 스프링 컨테이너가 프로토타입 *빈 생성과 의존관계 주입까지만* 관여, 매우 짧음
- 웹 관련
    - `request` : 웹 요청 들어오고 나갈 때까지 유지
    - `session` : 웹 세션 생성되고 종료될 때까지 유지
    - `application` : 웹의 서블릿 컨텍스트와 같은 범위로 유지

<br>

### 지정 방식

- 컴포넌트 스캔 자동 등록
    
    ```java
    @Scope("prototype")
    @Component
    pbulic class HelloBean { }
    ```
    
- 수동 등록
    
    ```java
    @Scope("prototype")
    @Bean
    PrototypeBean HelloBean() { return new HelloBean(); }
    ```

<br> 

## 📱 프로토타입 스코프

프로토타입 스코프를 조회하면 스프링 컨테이너는 **항상 새로운 인스턴스 생성**해서 반환

![Untitled](/img/basic/section9/prototypebean_req1.png)

프로토타입 빈 요청하면 스프링 컨테이너는 그때 프로토타입 빈을 생성하고 필요한 의존관계 주입

<br>

![Untitled](/img/basic/section9/prototypebean_req2.png)

스프링 컨테이너가 생성한 빈을 클라인언트에 반환 후 더이상 관여 X

이후 같은 요청이 오면 새로운 빈 생성해서 반환

<br>

**정리**

**스프링 컨테이너는 프로토타입 빈 생성하고 의존관계 주입, 초기화까지만 처리함**

그 이후 스프링 컨테이너는 생성된 프로토타입 빈 관리 X

⇒ 관리 책임은 ***클라이언트*** 에 있음 따라서  `@PreDestroy` 호출 X

<br>

### singleton

```java
@Test
void singletonBeanFind() {
    AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(SingletonBean.class);

    SingletonBean singletonBean1 = ac.getBean(SingletonBean.class);
    SingletonBean singletonBean2 = ac.getBean(SingletonBean.class);
    System.out.println("singletonBean1 = " + singletonBean1);
    System.out.println("singletonBean2 = " + singletonBean2);
    Assertions.assertThat(singletonBean1).isSameAs(singletonBean2);

    ac.close();
}

@Scope("singleton")
static class SingletonBean {
    @PostConstruct
    public void init() {
        System.out.println("SingletonBean.init");
    }

    @PreDestroy
    public void destoy() {
        System.out.println("SingletonBean.destroy");
    }
}
```

**[출력]**

```bash
SingletonBean.init
singletonBean1 = hello.core.scope.SingletonTest$SingletonBean@5505ae1a
singletonBean2 = hello.core.scope.SingletonTest$SingletonBean@5505ae1a
16:22:36.175 [main] DEBUG org.springframework.context.annotation.AnnotationConfigApplicationContext - Closing org.springframework.context.annotation.AnnotationConfigApplicationContext@3ee37e5a, started on Fri Apr 21 16:22:36 KST 2023
SingletonBean.destroy
```

<br>

### prototype

```java
@Test
void singletonBeanFind() {
    AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(SingletonBean.class);

    SingletonBean singletonBean1 = ac.getBean(SingletonBean.class);
    SingletonBean singletonBean2 = ac.getBean(SingletonBean.class);
    System.out.println("singletonBean1 = " + singletonBean1);
    System.out.println("singletonBean2 = " + singletonBean2);
    assertThat(singletonBean1).isSameAs(singletonBean2);

    ac.close();
}

@Scope("singleton")
static class SingletonBean {
    @PostConstruct
    public void init() {
        System.out.println("SingletonBean.init");
    }

    @PreDestroy
    public void destroy() {
        System.out.println("SingletonBean.destroy");
    }
}
```

**[출력]**

```bash
find prototypeBean1
PrototypeBean.init
find prototypeBean2
PrototypeBean.init
prototypeBean1 = hello.core.scope.PrototypeTest$PrototypeBean@5505ae1a
prototypeBean2 = hello.core.scope.PrototypeTest$PrototypeBean@73cd37c0
16:27:36.624 [main] DEBUG org.springframework.context.annotation.AnnotationConfigApplicationContext - Closing org.springframework.context.annotation.AnnotationConfigApplicationContext@3ee37e5a, started on Fri Apr 21 16:27:36 KST 2023
```

- 싱글톤 빈은 스프링 컨테이너 생성 시점에 초기화 메서드 실행, 프로토타입 빈은 스프링 컨테이너에서 빈 조회할 때 생성되고 초기화 메서드도 실행
- 프로토타입 빈은 2번 조회했을 때 다른 스프링 빈이 생성되고 초기화 2번 실행
- 프로토타입 빈은 스프링 컨테이너가 더이상 관리하지 않기 때문에 컨테이너 종료될 때 `destory()` 호출 X → 종료 필요하면 클라이언트가 직접 호출

<br>

## 🌪 프로토타입 스코프 - 싱글톤 빈과 함께 사용시 문제점

싱글톤 빈과 함께 사용할 때 의도한대로 잘 동작하지 않음

<br>

### 프로토타입 빈 직접 요청

**클라이언트 A**

![Untitled](/img/basic/section9/prototypeA.png)

1. 스프링 컨테이너에 **프로토타입 빈** 요청
2. 스프링 컨테이너는 프로토타입 빈 생성 후 반환, 이때 count는 0
3. 클라이언트가 `addCount()` 호출하여 count 값을 1로 변경

<br>

**클라이언트 B**

![Untitled](/img/basic/section9/prototypeB.png)

1. 새로운 클라이언트가 스프링 컨테이너에 프로토타입 빈 요청
2. 스프링 컨테이너는 새로운 프로토타입 빈 생성 후 반환, count는 0
3. 클라이언트가 `addCount()` 호출하여 count 1로 변경

<br>

**싱글톤 빈에서 프로토타입 빈 사용**

![Untitled](/img/basic/section9/singleton_and_prototype.png)

`clientBean` 은 싱글톤 → 스프링 컨테이너 생성 시점에 생성, 의존관계 주입 발생 (자동)

주입 시점에 프로토타입 빈 생성 후 `clientBean` 에 반환 이때 count 값 0

`clientBean` 은 ***프로토타입 빈을 내부 필드에 보관***

![Untitled](/img/basic/section9/singleton_and_prototype_A.png)

클라이언트A가 `clientBean` 요청해서 받음

이때 `clientBean.logic()` 호출 → `addCount()` 호출해서 프로토타입 빈의 count 1 증가하여 값은 1이 됨

![Untitled](/img/basic/section9/singleton_and_prototype_B.png)

클라이언트B가 `clientBean` 요청해서 받음 

싱글톤이므로 클라이언트A와 같은 빈을 받음

이때 `clientBean` 이 **내부에 갖고 있는 프로토타입 빈은 이미 과거에 주입이 끝났으므로 새로 생성되지 x**

따라서 클라이언트B가 `logic()` 호출 → `addCount()` → count 1 증가하여 값이 2가 됨

프로토타입 빈을 이렇게 사용하는게 아니라 사용할 때마다 새로 생성해서 사용 원함p

> *여러 빈에서 같은 프로토타입 빈 주입받으면, 주입 받는 시점에 새로 생성됨*
> 

<br>

## ☀️ 프로토타입 스코프 - 싱글톤 빈과 함께 사용시 Provider로 문제 해결

### 스프링 컨테이너에 요청

싱글톤 빈이 프로토타입 사용할 때마다 스프링 컨테이너에 새로 요청

의존관계를 주입받는게 아니라 **직접 필요한 의존관계 찾는 것 =** **Depedency Lookup(DL),** 의존관계 조회

스프링 컨테이너에 종속적인 코드가 되고 단위 테스트도 어려움 😠

<br>

### ObjectFactory, ObjectProvider

과거 `ObjectFactory` + 편의기능 ⇒ `ObjectProvider` 

```java
@Scope("singleton")
static class ClientBean {

    @Autowired
    private ObjectProvider<PrototypeBean> prototypeBeanProvider;

    public int logic() {
        PrototypeBean prototypeBean = prototypeBeanProvider.getObject();
        prototypeBean.addCount();
        return prototypeBean.getCount();
    }
}
```

- `getObject()` : 호출할 때 prototype 빈 찾아서 반환 **(DL)**

스프링 컨테이너에서 빈 찾아주는 대리자 정도로 인지

스프링이 제공하는 기능 사용하지만 기능 단순해서 단위테스트, mock 코드 짜기 쉬움

`ObjectFactory` : 기능이 단순, 별도의 라이브러리 필요 없음, 스프링에 의존

`ObjectProvider` : `ObjectFactory` 상속, 옵션, 스트림 처리 등 편의 기능이 많음, 별도의 라이브러리 필요
없음, 스프링에 의존

<br>

### JSR-330 Provider

*스프링에 의존하지 않는* 자바 표준 사용하는 방법

`javax.inject:javax.inject:1` 라이브러리를 gradle에 추가해야함

`provider.get()` : 항상 새로운 프로토타입 빈 생성됨

내부에서는 **스프링 컨테이너 통해 해당 빈 찾아서 반환(DL)**

자바 표준, 기능 단순(메서드 1개)

별도 라이브러리 필요함

스프링 말고 다른 컨테이너에서 사용 가능

<br>

### 프로토타입 빈 언제 사용?

대부분 싱글톤 빈으로 문제 해결할 수 있으므로 직접적인 사용 거의 X

`ObjectProvider` , `JSR330 Provider` 는 프로토타입 뿐만 아니라 DL 필요한 경우 언제든 사용

- 지연해서 가져올 때, optional 하게 가져올 때, 순환 의존관계일 때

<br>

그러면 둘 중 무엇을 많이 사용할까?

기본적으로 `ObjectProvider` 이 DL 위한 편의 기능 많이 제공함

코드를 스프링이 아닌 다른 컨테이너에서 사용해야 한다면 `JSR-330 Provider` 사용해야 함

> *자바표준과 스프링이 제공하는 기능이 겹칠 때가 많음
대부분 스프링이 더 다양하고 편리한 기능 제공해주기 때문에 특별히 다른 컨테이너 쓸 일 없다면 스프링이 제공하는 기능 사용*
> 

<br>

## 🖥 웹 스코프

### 특징

- 웹 한경에서만 동작
- 프로토타입과 다르게 **스프링이 해당 스코프의 종료시점까지 관리함** ⇒ 종료 메서드 호출

<br>

### 종류

- `request` : HTTP 요청 하나 들어오고 나갈 때까지 유지, 각각의 HTTP 요청마다 별도의 빈 인스턴스 생성되고 관리됨
- `session` : HTTP Session과 동일한 생명주기
- `application` : 서빌릿 컨텍스트와 동일한 생명주기
- `websocket` : 웹 소켓과 동일한 생명주기

![Untitled](/img/basic/section9/request.png)

클라이언트 A 가 HTTP request 요청 → Controller가 request scope 관련 객체 조회 , Service에서도 해당 객체를 바라보게 되면 클라이언트 A 전용 객체를 바라보게 됨

<br>

## 🛝 request 스코프 예제 만들기

1. `build.gradle` 에 의존성 추가
    
    web 환경 동작하도록 라이브러리( `spring-boot-starter-web` ) 추가
    
    > `spring-boot-starter-web` 라이브러리 추가하면 스프링부트는 내장 톰캣 서버를 활용하여 웹 서버와 스프링 함께 실행
    > 
    
    > 스프링 부트는 웹 라이브러리가 없으면 `AnnotationConfigApplicationContext` 기반으로 애플리케이션 구동함
    웹 라이브러리가 추가되면 추가 설정과 환경들이 필요해서 `AnnotationConfigServletWebServerApplicationContext` 기반으로 애플리케이션 구동
    > 

<br>

동시에 여러 HTTP 요청 오면 정확히 어떤 요청이 남긴 로그인지 구분 어려움 → 이때 `request` 스코프 사용

<br>

2. request 스코프 예제 개발
    
    공동 포맷 : `[UUID] [requestURL] {message}` 
    
    > *UUID* : 네트워크 상에서 고유성이 보장되는 id 만들기 위한 표쥰 규약, 128비트  → HTTP 요청 구분!
    > 
    
    ```java
    @Component
    @Scope(value = "request")
    
    public class MyLogger {
    
        private String uuid;
        private String requestURL;
    
        public void setRequestURL(String requestURL) {
            this.requestURL = requestURL;
        }
    
        public void log(String message) {
            System.out.println("[" + uuid + "]" + "[" + requestURL + "] " + message);
        }
    
        @PostConstruct
        public void init() {
            uuid = UUID.randomUUID().toString();
            System.out.println("[" + uuid + "] request scope bean create:" + this);
        }
    
        @PreDestroy
        public void close() {
            System.out.println("[" + uuid + "] request scope bean close:" + this);
        }
    }
    ```
    
    - `@Scope(value = “request”)`  : request scope 생성, HTTP 요청 당 하나씩 생성, 스프링 컨테이너 요청 시점
    - `uuid` 를 초기화 메서드를 사용해서 저장 → 다른 HTTP 요청과 구분 가능
    - `requestURL` 은 이 빈이 생성되는 시점에 알 수 없으므로 외부에서 입력받도록 함
    
    <br>

    ```java
    @Controller
    @RequiredArgsConstructor
    public class LogDemoController {
    
        private final LogDemoService logDemoService;
        private final MyLogger myLogger;
    
        @RequestMapping("log-demo")
        @ResponseBody
        public String logDemo(HttpServletRequest request) {
            String requestURL = request.getRequestURL().toString();
            myLogger.setRequestURL(requestURL);
    
            myLogger.log("controller test");
            logDemoService.logic("testId");
            return "OK";
        }
    }
    ```
    
    ```java
    @Service
    @RequiredArgsConstructor
    public class LogDemoService {
    
        private final MyLogger myLogger;
    
        public void logic(String id) {
            myLogger.log("service id = " + id);
        }
    }
    ```
    
    - request 스코프 사용하지 않고 파라미터로 모든 정보 서비스 계층으로 넘긴다면 파라미터가 많아서 지저분함
    - 웹과 관련된 정보가 웹과 관련없는 서비스 계층까지 넘어가게 됨 ⇒ 서비스 계층 웹 기술 종속 X
    
    *HTTP Request 들어와서 나갈 때까지 사용할 수 있는데 아직 HTTP request 요청이 없기 때문에 오류 발생*
    
    > requestURL을 `MyLogger` 저장하는 부분은 공통 처리가 가능한 스프링 인터셉터나 서블릿필터 등을 활용하는 것이 좋음
    > 
    
<br>

## 🥨 스코프와 Provider

```java
private final ObjectProvider<MyLogger> myLoggerProvider;

@RequestMapping("log-demo")
@ResponseBody
public String logDemo(HttpServletRequest request) {
    String requestURL = request.getRequestURL().toString();
    MyLogger myLogger = myLoggerProvider.getObject();
		....
}
```

`ObjectProvider<MyLogger>` → `MyLogger` 주입받는게 아니라 `MyLogger` 를 찾을 수 있는 dependency 있는 애가 주입이 됨

```bash
[7be5a2b1-bf40-41d4-88e3-f082c2957775] request scope bean create:hello.core.common.MyLogger@9989e81
[7be5a2b1-bf40-41d4-88e3-f082c2957775][http://localhost:8080/log-demo] controller test
[7be5a2b1-bf40-41d4-88e3-f082c2957775][http://localhost:8080/log-demo] service id = testId
[7be5a2b1-bf40-41d4-88e3-f082c2957775] request scope bean close:hello.core.common.MyLogger@9989e81
```

`myLoggerProvider.getObject()` 하는 시점에 request 스코프의 빈 만들어짐

`ObjectProvider` 덕분에 `myLoggerProvider.getObject()` 호출 시점까지 reuqest 스코프 빈의 생성 지연할 수 있음 → 빈생성 지연보다는 **스프링 컨테이너 요청 지연**이 좀 더 정확한 표현

`LogDemoController` , `LogDemoService` 에서 각각 따로 호출해도 같은 HTTP 요청이면 같은 스프링 빈 반환

<br>

## 🎩 스코프와 프록시

```bash
@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class MyLogger { ... }
```

`@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)` 추가

- 적용 대상 클래스면 `TARGET_CLASS`
- 적용 대상 인터페이스면 `INTERFACES`
- `MyLogger` 가짜 프록시 클래스 만들고 주입시킴

```bash
myLogger = class hello.core.common.MyLogger$$EnhancerBySpringCGLIB$$e7a91199
```

가짜 프록시 객체 만든거 확인

- `CGLIB` 라이브러리로 클래스 내 상속받은 가짜 프록시 객체 만들어 주입
- 스프링 컨테이너에 `myLogger` 이름으로 **가짜 프록시 객체 등록** → 의존관계 주입도 프록시 객체

<br>

프록시 객체는 요청 오면 내부에서 진짜 빈 요청하는 위임 로직 존재

- 프록시 객체가 진짜 `myLogger.logic()` 호출함

**원본 클래스 상속받아 만들어졌기 때문에 클라이언트 입장에서는 원본인지 아닌지 모르게 동일하게 사용 가능(다형성)**

<br>

### 정리

Provider, 프록시 핵심 아이디어 : 진짜 객체 조회를 꼭 필요한 시점까지 지연처리함

단지 애노테이션 설정 변경만으로 원본 → 프록시 대체 가능 (다형성, DI 컨테이너 장점)