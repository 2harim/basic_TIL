**목차**
- [📞 빈 생명주기 콜백 시작](#빈-생명주기-콜백-시작)
  - [스프링 빈 이벤트 라이프사이클](#스프링-빈-이벤트-라이프사이클)
- [⛳️ 인터페이스 InitializingBean, DisposableBean](#️-인터페이스-initializingbean-disposablebean)
  - [단점](#단점)
- [🏂 빈 등록 초기화, 소멸 메서드](#빈-등록-초기화-소멸-메서드)
  - [특징](#특징)
  - [종료 메서드 추론](#종료-메서드-추론)
- [📮 애노테이션 @PostConstruct, @PreDestroy](#애노테이션-postconstruct-predestroy)
  - [특징](#특징-1)
  - [결론](#결론)

<br>

## 📞 빈 생명주기 콜백 시작

*DB 커넥션 풀, 네트워크 소켓* 처럼 애플리케이션 시작 시점에 필요한 연결 미리 하고 종료 시점에 연결 모두 종료하는 작업 진행 → **객체 초기화, 종료 작업 필요**

```java
package hello.core.lifecycle;

public class NetworkClient {

    private String url;

    public NetworkClient() {
        System.out.println("생성자 호출, url = " + url);
        connect();
        call("초기화 연결 메시지");
    }

    public void setUrl(String url) {
        this.url = url;
    }

    //서비스 시작시 호출
    public void connect() {
        System.out.println("connect " + url);
    }

    public void call(String message) {
        System.out.println("call " + url + " message = " + message);
    }

    //서비스 종료시 호출
    public void disconnect() {
        System.out.println("close " + url);
    }
}
```

**[출력]**
```bash
생성자 호출, url = null
connect null
call null message = 초기화 연결 메시지
```

기본 생성자로 파라미터 없이 **객체 생성 후 설정**을 하므로 *url 정보 없이(null) 호출됨*

<br>

✔️ **스프링 빈의 간단한 라이프 사이클**

> 객체 생성 → 의존관계 주입 (생성자 주입 예외)
> 

따라서 초기화 작업은 의존관계 주입 모두 완료된 후 난 다음 호출해야 함

스프링은 **의존관계 주입 완료되면 스프링 빈에게 콜백 메서드** 통해 초기화 시점 알려주는 다양한 기능 제공

또한 **스프링 컨테이너 종료 직전 소멸 콜백**을 줌 ⇒ *안전하게 종료 작업 진행 가능*

<br>

### 스프링 빈 이벤트 라이프사이클

`스프링 컨테이너 생성 → 스프링 빈 생성 → 의존관계 주입 → 초기화 콜백 → 사용 → 소멸전 콜백 → 스프링 종료` 

- 초기화 콜백 : 빈 생성, 의존관계 주입 후 호출
- 소멸전 콜백 : 빈 소멸 직전 호출

> *객체 생성과 초기화 분리하자*
SRP 원칙! 객체 생성은 최대한 생성에만 집중하는 책임만 맡자
초기화는 생성된 값 활용해서 외부 커넥션 연결하는 것과 같이 무거운 동작을 수행함 😠
초기화가 내부 값 변경하는 정도의 단순한 경우 생성자에서 하는 것이 나을수도 있음
> 

<br>

## ⛳️ 인터페이스 InitializingBean, DisposableBean

```java
public class NetworkClient implements InitializingBean, DisposableBean {

	...
	@Override
  public void afterPropertiesSet() throws Exception {
      connect();
      call("초기화 연결 메시지");
  }
	@Override
	public void destroy() throws Exception {
	    disconnect();
	}
	
}
```

InitializingBean 

- `afterPropertiesSet()` : 의존관계 주입 끝나면 호출

DisposableBean 

- `destroy()` : 빈 종료 시 호출

<br>

**[출력]**
```bash
생성자 호출, url = null
connect http://hello-spring.dev
call http://hello-spring.dev message = 초기화 연결 메시지
15:28:44.683 [main] DEBUG org.springframework.context.annotation.AnnotationConfigApplicationContext - Closing org.springframework.context.annotation.AnnotationConfigApplicationContext@2ea41516, started on Fri Apr 21 15:28:44 KST 2023
close http://hello-spring.dev
```

<br>

### 단점

스프링 전용 인터페이스 → **스프링에 너무 의존적**이게 됨

초기화, 소멸 메서드 이름 변경 X

코드를 고칠 수 없는 라이브러리 적용 X

⇒ 초창기 나온 방법이어서 최근에는 잘 사용하지 않는다

<br>

## 🏂 빈 등록 초기화, 소멸 메서드

설정 정보에 `@Bean(initMethod = “init”, destroyMethod = “close”)` 와 같이 초기화/소멸 메서드 지정 가능

```java
public class NetworkClient {

	...

	public void init() {
	        connect();
	        call("초기화 연결 메시지");
	  }
	
	public void close() {
	    disconnect();
	}
}
```

```java
@Configuration
static class LifeCycleConfig {

  @Bean(initMethod = "init", destroyMethod = "close")
  public NetworkClient networkClient() { .. }
}
```

<br>

### 특징

- 메서드 이름 자유롭게 사용 가능
- 스프링 빈이 스프링 코드에 의존적 X
- **코드가 아니라 설정 정보 사용하기 때문에** 코드 고칠 수 없는 *외부 라이브러리* 에도 초기화, 종료 메서드 적용 가능

<br>

### 종료 메서드 추론

`@Bean` 으로 호출할 때 `destroyMethod` 속성의 특별한 기능 존재

기본 값이 `(inferred)` (추론) 으로 등록되어 있음

라이브러리 대부분 종료 메서드 이름이 `close` , `shutdown` 

`close` , `shutdown` 이라는 이름의 메서드를 자동으로 호출해줌! → 종료 메서드를 추론해서 호출

빈 공백 지정시 추론 기능 꺼줌

<br>

## 📮 애노테이션 @PostConstruct, @PreDestroy

```java
public class NetworkClient {
	...	
	@PostConstruct
  public void init() {
      connect();
      call("초기화 연결 메시지");
  }

  @PreDestroy
  public void close() {
      disconnect();
  }
}
```

<br>

### 특징

- 최신 스프링에서 권고하는 방법
- 애노테이션 하나만 붙이면 되므로 편리
- 패키지 `javax.annotaton.PostConstruct` 스프링에 종속적이지 않고 자바 표준임
    
    ⇒ 스프링이 아닌 다른 컨테이너에서도 잘 동작
    
- 컴포넌트 스캔과 잘 어울림 ← 빈 등록을 따로 하지 않으므로! (@Bean)
- 단점은 외부 라이브러리에 적용하지 못한다는 것 ← 코드 수정 못하기 때문에

<br>

### 결론

`@PostConstruct` , `@PreDestroy` 애노테이션 사용

코드 고칠 수 없는 외부 라이브러리 초기화/종료시 `@Bean` 의 `initMethod` , `destroyMethod` 사용