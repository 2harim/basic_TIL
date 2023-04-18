
## 목차
- [목차](#목차)
- [✂️ 프로젝트 생성](#️프로젝트-생성)
  - [준비물](#준비물)
  - [프로젝트 생성](#프로젝트-생성)
- [🪜 비즈니스 요구사항과 설계](#비즈니스-요구사항과-설계)
  - [회원](#회원)
  - [주문과 할인 정책](#주문과-할인-정책)
- [🙎 회원 도메인 설계](#-회원-도메인-설계)
  - [회원 도메인 협력 관계](#회원-도메인-협력-관계)
  - [회원 클래스 다이어그램](#회원-클래스-다이어그램)
  - [회원 객체 다이어그램](#회원-객체-다이어그램)
- [🛠 회원 도메인 개발](#회원-도메인-개발)
- [⚠️ 회원 도메인 실행과 테스트](#️회원-도메인-실행과-테스트)
  - [회원 도메인 설계의 문제점](#회원-도메인-설계의-문제점)
  - [Junit의 assertThat보다 assertJ의 assertThat 사용하는 이유](#junit의-assertthat보다-assertj의-assertthat-사용하는-이유)
- [🪄 주문과 할인 도메인 설계](#주문과-할인-도메인-설계)
  - [주문 도메인 협력, 역할, 책임](#주문-도메인-협력-역할-책임)
  - [주문 도메인 클래스 다이어그램](#주문-도메인-클래스-다이어그램)
  - [주문 도메인 객체 다이어그램](#주문-도메인-객체-다이어그램)
- [🛠 주문과 할인 도메인 개발](#주문과-할인-도메인-개발)
  - [discount](#discount)
  - [order](#order)
- [⚠️ 주문과 할인 도메인 실행과 테스트](#️주문과-할인-도메인-실행과-테스트)


---

## ✂️ 프로젝트 생성

### 준비물

- Java11
- IDE (IntelliJ, Eclipse)
  
<br>

### 프로젝트 생성

[https://start.spring.io](https://start.spring.io/)

- Gradle Project
- Java 11
- Spring Boot 2.7.10
- packaging : Jar

```groovy
plugins {
	id 'java'
	id 'org.springframework.boot' version '2.7.10'
	id 'io.spring.dependency-management' version '1.0.15.RELEASE'
}

group = 'hello'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '11'

repositories {
	mavenCentral()
}

dependencies {
	implementation 'org.springframework.boot:spring-boot-starter'
	testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

tasks.named('test') {
	useJUnitPlatform()
}
```

<br>
<h3>
<details>
<summary> Jar vs War </summary>

```
Jar (Java Archive)
- class(Java 리소스, 속성 파일), 라이브러리 및 액세서리 파일 포함 ⇒ 자바 프로젝트를 압축한 파일
- JDK에 포함하고 있는 JRE만 가지고도 실행 가능
```
```
War (Web Application Archive)
- servlet, jsp 컨테이너에 배치할 수 있는 웹 어플리케이션 압축 파일 포맷
- 웹 관련 자원 포함(jsp, Servlet, class, XML, HTML, JS, .. )
- war 파일 실행하려면 웹서버 또는 웹컨테이너 필요
⇒ 웹 애플리케이션 전체를 패키징 하기 위한 Jar 파일
```

스프링 부트에서 가이드하는 표준은 JAR ! ← JSP, 외장 톰캣 사용 X

</details>
</h3>

<br>

---

## 🪜 비즈니스 요구사항과 설계

### 회원

- 회원 가입, 조회
- 일반, VIP 두 가지 등급 존재
- 자체 DB 구축 or 외부 시스템과 연동 (미확정)

<br>

### 주문과 할인 정책

- 회원은 상품 주문 가능
- 회원 등급에 따라 할인 정책 적용
- 모든 VIP 1000원 할인 → 고정 금액 할인
- 할인 정책 변경 가능성 높음 (미확정)

<br>

결정하기 어려운 부분때문에 개발을 미룰 수 없으므로 객체 지향 설계에 따라 인터페이스 만들고 구현체를 언제든지 갈아끼울 수 있도록 설계한다

<br>

---

## 🙎 회원 도메인 설계

### 회원 도메인 협력 관계

![Untitled](/img/member_domain.png)

회원 데이터에 접근하는 계층 분리 → 인터페이스를 통해 자체 DB 구축과 외부 시스템 연동 미확정 상황에 대응

<br>

### 회원 클래스 다이어그램

![Untitled](/img/member_cld.png)

정적

<br>

### 회원 객체 다이어그램

![회원 서비스 : MemoryServiceImpl](/img/member_od.png)

회원 서비스 : MemoryServiceImpl

동적, Runtime 때 진짜 참조하고 있는 객체 표시

<br>

---

## 🛠 회원 도메인 개발

Member Entity

```java
public class Member {

    private Long id;
    private String name;
    private Grade grade;

    public Member(Long id, String name, Grade grade) {
        this.id = id;
        this.name = name;
        this.grade = grade;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Grade getGrade() {
        return grade;
    }

    public void setGrade(Grade grade) {
        this.grade = grade;
    }
}
```

MemberRepository

```java
public interface MemberRepository {

    void save(Member member);

    Member findById(Long memberId);
}
```

MemoryMemberRepository

```java
public class MemoryMemberRepository implements MemberRepository {

    private static Map<Long, Member> store = new HashMap<>();

    @Override
    public void save(Member member) {
        store.put(member.getId(), member);
    }

    @Override
    public Member findById(Long memberId) {
        return store.get(memberId);
    }
}
```

DB 확정이 안되었음, 일단 메모리 회원 저장소를 구현해서 개발 진행

> 💡 실무에서는 `HashMap`은 동시성 이슈가 있을 수 있기 때문에 `ConcurrentHashMap` 사용
> 

<br>

<h3>
<details>
<summary>ConcurrentHashMap</summary>
<br>
해당 Collection 이 나오기 전까지는 map을 thread-safe하게 사용하기 위해서는 lock을 걸어 사용
→ 성능 오버헤드 이슈, 하나의 thread가 lock 유지하는 동안 다른 모든 thread가 사용할 수 없기 때문

<br>

ConcurrentHashMap은 map의 일부에만 lock 사용

각각의 Bucket 별로 동기화 진행하기 때문에 다른 Bucket에 속한 경우 별도 lock 없이 운용

![Untitled](/img/concurrentHashMap.png)

- `put` Bucket에 Node가 존재하는 경우만 synchronized 이용해 thread 제어
- `get` synchronized 이용 X, 가장 최신 value return
- `entrySet(), keySet(), values()` 자체적으로 갖고 있어 동기화 문제 발생 X

[참고] [https://velog.io/@alsgus92/ConcurrentHashMap의-Thread-safe-원리](https://velog.io/@alsgus92/ConcurrentHashMap%EC%9D%98-Thread-safe-%EC%9B%90%EB%A6%AC)

</details>
</h3>
<br>

---

## ⚠️ 회원 도메인 실행과 테스트

MemberApp

```java
public class MemberApp {

    public static void main(String[] args) {
        MemberService memberService = new MemberServiceImpl();

        Member member = new Member(1L, "memberA", Grade.VIP);
        memberService.join(member);

        Member findMember = memberService.findMember(1L);
        System.out.println("member = " + member.getName());
        System.out.println("findMember = " + findMember.getName());
    }
}
```

<br>

MemberServiceTest

```java
public class MemberServiceTest {

    MemberService memberService = new MemberServiceImpl();
    @Test
    void join() {
        //given
        Member member = new Member(1L, "memberA", Grade.VIP);

        //when
        memberService.join(member);
        Member findMember = memberService.findMember(1L);

        //then
        Assertions.assertThat(member).isEqualTo(findMember);
    }
}
```

MemberApp을 통해 검증할 때는 눈으로만 검증했음

Test를 통해 오류를 빠르게 확인할 수 있음

<br>

### 회원 도메인 설계의 문제점

- 다른 저장소로 변경할 때 OCP 원칙 준수할까?

- DIP 잘 지키고 있을까?

- 인터페이스 뿐만 아니라 구현까지 모두 의존하는 문제 존재

<br>

### Junit의 assertThat보다 assertJ의 assertThat 사용하는 이유

```java
public static <T> void assertThat(T actual, Matcher<? super T> matcher)
```

actual에 검증 대상을 넣고 비교하는 로직을 matcher에 넣어 검증 단계 수행

이때 개발자가 matcher를 직접 구현하는 것은 비효율적이고 구현한 matcher에서 오류 발생할 수 있음
→ hamcrest에 구현된 matcher 사용하도록 강제함

<br>

**단점**

1. 필요 메소드를 공식문서에서 직접 가져와야 함
2. Matchers 클래스에서 여러 타입을 한 번에 가져와서 원하는 타입에 대한 matcher 찾기 불편


<br>

⇒ `org.assertj.core.api.Assertions.assertThat` : **메서드 체이닝 패턴 형식**을 통해 가독성 향상시킴

```java
public static AbstractAssert<SELF, T> assertThat(T actual)
```

`parameter` : actual

`return` : Assert 인스턴스

<br>

---

## 🪄 주문과 할인 도메인 설계

### 주문 도메인 협력, 역할, 책임

![Untitled](/img/order_domain.png)

- 클라이언트는 주문 생성 요청
- 주문 서비스는 할인을 위해 회원 저장소에서 회원 조회
- 주문 서비스는 회원 등급에 따라 할인 여부를 할인 정책에 위임
- 주문 서비스는 할인 결과를 포함한 주문 결과 반환

<br>

역할과 구현 분리 → 회원 저장소, 할인 정책 유연하게 변경 가능

![Untitled](/img/order_role.png)

### 주문 도메인 클래스 다이어그램

![Untitled](/img/order_cld.png)

OrderServiceImpl이 MemberRepository, DiscountPolicy 사용

<br>

### 주문 도메인 객체 다이어그램

![Untitled](/img/order_od1.png)

메모리에서 회원 조회, 정액 할인 정책 적용

<br>

![Untitled](/img/order_od2.png)

회원을 실제 DB에서 조회, 정률 할인 정책 적용으로 변경 → 주문 서비스는 변경 필요 X

**역할들의 협력 관계 재사용 가능!**

<br>

---

## 🛠 주문과 할인 도메인 개발

### discount

<br>

DiscountPolicy

```java
public interface DiscountPolicy {

    /**
     * @return 할인 대상 금액
     */
    int discount(Member member, int price);
}
```

<br>

FixDiscountPolicy ← DiscountPolicy 구현체

```java
public class FixDiscountPolicy implements DiscountPolicy {

    private int discountFixAmount = 1000; //VIP 대상으로 1000원 고정 할인

    @Override
    public int discount(Member member, int price) {
        if (member.getGrade() == Grade.VIP) {
            return discountFixAmount;
        }else {
            return 0;
        }
    }
}
```

<br>

### order

<br>

Order

```java
public class Order {

    private Long memberId;
    private String itemName;
    private int itemPrice;
    private int discountPrice;

    public Order(Long memberId, String itemName, int itemPrice, int discountPrice) {
        this.memberId = memberId;
        this.itemName = itemName;
        this.itemPrice = itemPrice;
        this.discountPrice = discountPrice;
    }

    public int calculatePrice() {
        return itemPrice - discountPrice;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(int itemPrice) {
        this.itemPrice = itemPrice;
    }

    public int getDiscountPrice() {
        return discountPrice;
    }

    public void setDiscountPrice(int discountPrice) {
        this.discountPrice = discountPrice;
    }

    @Override
    public String toString() {
        return "Order{" +
            "memberId=" + memberId +
            ", itemName='" + itemName + '\'' +
            ", itemPrice=" + itemPrice +
            ", discountPrice=" + discountPrice +
            '}';
    }
}
```

<br>

OrderService

```java
public interface OrderService {

    Order createOrder(Long memberId, String itemName, int itemPrice);
}
```

OrderServiceImpl ← OrderService 구현체

```java
public class OrderServiceImpl implements OrderService {

    private final MemberService memberService = new MemberServiceImpl();
    private final DiscountPolicy discountPolicy = new FixDiscountPolicy();

    @Override
    public Order createOrder(Long memberId, String itemName, int itemPrice) {
        Member member = memberService.findMember(memberId);
        int discountPrice = discountPolicy.discount(member, itemPrice); //할인 금액 확인

        return new Order(memberId, itemName, itemPrice, discountPrice); //주문 생성
    }
}
```

<br>

---

## ⚠️ 주문과 할인 도메인 실행과 테스트

<br>


```java
public class OrderServiceTest {

    MemberService memberService = new MemberServiceImpl();
    OrderService orderService = new OrderServiceImpl();

    @Test
    void createOrder() {
        Long memberId = 1L; //null이 들어갈 수도 있으므로 wrapper type 사용
        Member member = new Member(memberId, "memberA", Grade.VIP);
        memberService.join(member);

        Order order = orderService.createOrder(memberId, "itemA", 10000);
        Assertions.assertThat(order.getDiscountPrice()).isEqualTo(1000);
    }
}
```

정률할인 정책으로 깔끔하게 바꿀 수 있을까?!