**목차**
- [🎭 프록시](#프록시)
  - [프록시 기초](#프록시-기초)
  - [프록시 특징](#프록시-특징)
  - [프록시 객체의 초기화](#프록시-객체의-초기화)
  - [프록시 확인](#프록시-확인)
- [🐢 즉시로딩과 지연로딩](#즉시로딩과-지연로딩)
  - [프록시와 즉시로딩 주의](#프록시와-즉시로딩-주의)
  - [지연 로딩 활용](#지연-로딩-활용)
- [🍡 영속성 전이(CASCADE)와 고아 객체](#영속성-전이cascade와-고아-객체)
  - [영속성 전이 : CASCADE](#영속성-전이--cascade)
  - [주의](#주의)
  - [CASCADE 종류](#cascade-종류)
  - [고아 객체](#고아-객체)
  - [영속성 전이 + 고아 객체](#영속성-전이--고아-객체)
- [🌿 실전 예제 5 - 연관관계 관리](#실전-예제-5---연관관계-관리)

<br>

## 🎭 프록시

Member를 조회할 때 Team도 함께 조회 필요?

항상 함께 조회하고 싶지는 않음

<br>

### 프록시 기초

`em.find()` : DB 통해 실제 엔티티 객체 조회

`em.getReference()` : DB 조회를 미루는 가짜(프록시) 엔티티 객체 조회 → 쿼리 발생 X

```java
findMember.class = class hellojpa.Member$HibernateProxy$uEZYmfDV
```

<br>

### 프록시 특징

- 실제 클래스를 상속받아 만들어짐 → 겉 모양 같음
    - 사용하는 입장에서는 진짜 객체인지 프록시 객체인지 구분하지 않고 사용하면 됨
    
    ![Untitled](/img/jpa_basic/section8/proxy_inheritance.png){: width="30" height="60"}

    
- 실제 객체의 `참조(target)` 보관
    - 프록시 객체를 호출하면 프록시 객체는 실제 객체의 메서드 호출
    
    ![Untitled](/img/jpa_basic/section8/proxy_target.png)
    

<br>

### 프록시 객체의 초기화

```java
Member member = em.getReference(Member.class, "id1");
member.getName();
```

![Untitled](/img/jpa_basic/section8/proxy_initialization.png)

`getName()` 요청 → 값이 없으므로 영속성 컨텍스트에 초기화 요청 → 실제 entity 가져와서 target에 연결시킴

- 프록시 객체는 처음 사용할 때 한 번만 초기화
    - 초기화할 때 프록시 객체가 실제 엔티티로 바뀌는 것이 아님, 실체 엔티티에 접근 가능한 것임
- 상속 관계 → 타입 체크 시 주의 (== 비교하면 X, `instanceOf` 사용)
- 영속성 컨텍스트에 엔티티 이미 있다면 em.getReference() 호출해도 실제 엔티티 반환
    - 프록시를 굳이 만드는 것은 별로! 성능 최적화 입장에서 더 나음
    - JPA 가 제공하는 매커니즘때문에 == 비교시 true가 나오도록 해줘야 함
        
        하나의 트랜잭션 안에서 같은 객체임을 보장
        
- em.getReference() 호출 후 em.find() 하면 proxy 객체 반환
    - find 했을 때 select 쿼리가 발생하기는 하나 == 비교를 위해 proxy 반환
- 영속성 컨텍스트 도움 받을 수 없는 준영속 상태일 때 프록시 초기화하면 문제 발생
    - LazyInitializationException 예외 발생 시킴
    - 영속성 컨텍스트가 더이상 관리하지 않기 때문에 도움 받을 수 없음, could not initialize proxy !

**프록시든 아니든 문제 없게 개발하는 것이 중요**

<br>

### 프록시 확인

- 프록시 인스턴스 초기화 여부 확인
    
    `PersistenceUnitUtil.isLoaded(Object entity)` 
    
- 프록시 클래스 확인
    
    `entity.getClass().getName()` 
    
- 프록시 강제 초기화
    
    `org.hibernate.Hibernate.initialize(entity)` 
    
    JPA 표준은 없음
    
<br>

## 🐢 즉시로딩과 지연로딩

지연로딩 LAZY 사용해서 프록시로 조회

![Untitled](/img/jpa_basic/section8/lazy.png)

`@ManyToOne(fetch = FetchType.LAZY)` 

```java
public class Member {
	...
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TEAM_ID")
    private Team team;
	...
}
```

```java
Member member = em.find(Member.class, 1L);
Team team = member.getTeam();
```

`m.getTeam().getClass()` → `class hellojpa.Team$HibernateProxy$NVI3TAui`  프록시 객체

```sql
Hibernate: 
    select
        member0_.MEMBER_ID as member_i1_3_0_,
        member0_.createdBy as createdb2_3_0_,
        member0_.createdDate as createdd3_3_0_,
        member0_.lastModifiedBy as lastmodi4_3_0_,
        member0_.lastModifiedDate as lastmodi5_3_0_,
        member0_.TEAM_ID as team_id7_3_0_,
        member0_.USERNAME as username6_3_0_ 
    from
        Member member0_ 
    where
        member0_.MEMBER_ID=?
```

이후 `team.getName()` 호출하면 그제서야 team 조회하는 select문 발생

실제 사용 시점에 초기화됨(DB조회)

<br>

Member와 Team을 자주 함께 사용한다면 즉시 로딩 `EAGER` 사용해서 함께 조회

![Untitled](/img/jpa_basic/section8/eager.png)

→ 조회할 때 member, team 조인해서 가져옴 (JPA 구현체 대부분) → 프록시 필요 X

<br>

### 프록시와 즉시로딩 주의

- ***실무에서 가급적 지연 로딩만 사용***
    - 즉시로딩 적용하면 예상치 못한 SQL 발생
        
        join 하는 테이블이 많아지게 되면 find할 때 모든 값 다 조회하므로 성능 문제 
        
    - JPQL에서 N+1 문제 발생
        
        SQL로 먼저 번역됨 → member만 select하게 됨 → 즉시로딩이므로 team 필요함을 느끼고 별도의 select 쿼리 발생 
        
        > N+1 문제 : 쿼리를 하나 보냈는데 N개가 추가로 발생함
        > 
        
        `fetchJoin` : 동적으로 원하는 애를 선택해서 가져옴 / `엔티티 그래프` 등의 기능 사용!
        
    - `@ManyToOne` , `@OneToOne` 은 기본이 즉시로딩 → LAZY로 설정 필요

<br>

### 지연 로딩 활용

`Member - Team` 자주 함께 사용 → 즉시 로딩

`Member - Order` 가끔 사용 → 지연 로딩

`Order - Product` 자주 함께 사용 → 즉시 로딩

![Untitled](/img/jpa_basic/section8/lazy_example1.png)

주문내역리스트는 lazy 설정이므로 proxy

![Untitled](/img/jpa_basic/section8/lazy_example2.png)

이때 만약 주문내역리스트를 사용하게 되면 상품과 즉시로딩이 걸려있기 때문에 객체를 가져옴

<br>

## 🍡 영속성 전이(CASCADE)와 고아 객체

### 영속성 전이 : CASCADE

특정 엔티티를 영속 상태로 만들 때 연관된 엔티티도 함께 영속 상태로 만들고 싶을 때 사용

ex) 부모 엔티티를 저장할 때 자식 엔티티를 함께 저장하고 싶을 때 

```java
Child child1 = new Child();
Child child2 = new Child();

Parent parent = new Parent();
parent.addChild(child1);
parent.addChild(child2);

em.persist(parent);
em.persist(child1);
em.persist(child2);
```

parent, child1, child2 각자 저장하는 것 귀찮음!

<br>

```java
public class Parent { 
		...	
		@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Child> childList = new ArrayList<>();
		...
}
```

```java
Child child1 = new Child();
Child child2 = new Child();

Parent parent = new Parent();
parent.addChild(child1);
parent.addChild(child2);

em.persist(parent);
```

parent만 persist했지만 실제로 parent, child1, child2 모두 insert 쿼리 발생

<br>

### 주의

✲ *연관관계 설정이나 즉시로딩/지연로딩과 전혀 관계 없음!!* ✲ 

엔티티 영속화할 때 연관된 엔티티 함께 영속화하는 단지 편리함 제공

<br>

### CASCADE 종류

- **`ALL` : 모두 적용 (라이프사이클 맞춰야 할 때)**
- **`PERSIST` : 영속 (저장)**
- **`REMOVE` : 삭제**
- `MERGE` : 병합
- `REFRESH`
- `DETACH`

<br>

**언제 사용해야하 하는가**

- 하나의 부모가 여러 자식을 관리할 때는 의미 있음 ⇒  ****단일 엔티티에 종속적일 때  ex) 게시판과 첨부파일 목록들
- 라이프사이클이 거의 유사할 때

<br>

### 고아 객체

부모 엔티티와 연관관계 끊어진 자식 엔티티 → 자동으로 삭제

- `orphanRemoval = true`

```java
Parent findParent = em.find(Parent.class, parent.getId());
  findParent.getChildList().remove(0);
```

자동으로 delete query 발생 

<br>

**주의**

참조하는 곳이 하나일 때 사용해야 함

특정 엔티티가 개인 소유할 때 사용

`@OneToOne` , `@OneToMany` 만 가능

> *부모를 제거하면 자식은 고아가 됨 → 객체 제거 기능 활성화하면 부모 제거할 때 자식도 함께 제거됨
CascadeType.REMOVE 처럼 동작*
> 

<br>

### 영속성 전이 + 고아 객체

`CascadeType.ALL + orphanRemoval = true` 

스스로 생명주기 관리하는 엔티티는 `em.persist()` 로 영속화, `em.remove()` 로 제거

두 옵션 모두 활성화하면 부모 엔티티 통해서 자식의 생명 주기 관리 가능 

도메인주도설계(DDD) 의 Aggregate Root 개념 구현할 때 유용

- DAO 따로 만들 필요 없음

<br>

## 🌿 실전 예제 5 - 연관관계 관리

- 모든 연관관계 지연 로딩으로 ( `@ManyToOne` , `@OneToOne` )
- 영속성 전이 설정

![Untitled](/img/jpa_basic/section8/example5.png)