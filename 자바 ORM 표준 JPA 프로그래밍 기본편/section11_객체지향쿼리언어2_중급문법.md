**목차**
- [🛣 경로 표현식](#경로-표현식)
  - [상태 필드](#상태-필드)
  - [연관 필드](#연관-필드)
  - [특징](#특징)
- [🍍 페치 조인 1 - 기본](#페치-조인-1---기본)
  - [단일 페치 조인](#단일-페치-조인)
  - [컬렉션 페치 조인](#컬렉션-페치-조인)
  - [중복 제거 DISTINCT](#중복-제거-distinct)
  - [페치 조인 vs 일반 조인](#페치-조인-vs-일반-조인)
- [⛵️ 페치 조인 2 - 한계](#️-페치-조인-2---한계)
  - [정리](#정리)
- [🚀 다형성 쿼리](#다형성-쿼리)
  - [TREAT](#treat)
- [✉️ 엔티티 직접 사용](#️엔티티-직접-사용)
  - [기본키 값](#기본키-값)
  - [외래키 값](#외래키-값)
- [🎤 Named 쿼리](#named-쿼리)
- [🌧 벌크 연산](#벌크-연산)
  - [주의](#주의)

<br>

## 🛣 경로 표현식

점( `.` )을 찍어 객체 그래프를 탐색하는 것

<br>

### 상태 필드

단순히 값 저장하기 위한 필드

`m.username`

<br>

### 연관 필드

- 단일 값 연관 필드
    - `@ManyToOne` , `@OneToOne` → 대상이 엔티티
    - `m.team`
- 컬렉션 값 연관 필드
    - `@OneToMany` , `@ManyToMany` → 대상이 컬렉션
    - `m.orders`

<br>

### 특징

- 상태 필드는 경로 탐색의 끝, 더이상 탐색 X
- 단일 값 연관 경로 : 묵시적 내부 조인 발생, 탐색 O
- 컬렉션 값 연관 경로 : 묵시적 내부 조인 발생, 탐색 X (size만 가능..!) ⇒ 명시적 조인 통해서 별칭으로 탐색 가능

> *묵시적 조인 사용하지 X ← 조인이 일어나는 상황을 파악하기 어려우므로 예상치 못한 문제 발생할 수 있음*
> 
> 
> *묵시적 조인 : 경로 표현식에 의해 SQL 조인발생 (내부 조인만 가능)*
> 

<br>

## 🍍 페치 조인 1 - 기본

- JPQL에서 성능 최적화 위해 제공하는 기능
- **연관된 엔티티나 컬렉션을 SQL 한 번에 함께 조회하는 기능**
- `join fetch`

<br>

### 단일 페치 조인

회원 조회할 때 연관된 팀도 함께 조회

JPQL : `select **m** from Member m **join fetch** m.team` 

⇒ SQL : `SELECT **m.*, t.*** FROM MEMBER m INNER JOIN TEAM t ON m.team_id = t.id` (즉시로딩)

**[fetch join 전]**

```java
String query = "select m from Member m";

List<Member> result = em.createQuery(query, Member.class).getResultList();
  for (Member m : result) {
      System.out.println("m = " + m.getUsername() + ", " + m.getTeam().getName());
  }
//회원1, 팀A(SQL)
//회원2, 팀A(1차캐시)
//회원3, 팀B(SQL)
```

team을 조회할 때 추가적인 select query 발생 → *N+1 문제*

<br>

**[fetch join]**

```java
String query = "select m from Member m join fetch m.team";

List<Member> result = em.createQuery(query, Member.class).getResultList();
for (Member m : result) {
    System.out.println("m = " + m.getUsername() + ", " + m.getTeam().getName());
}
```

join으로 데이터를 가져옴 → select query 1번만 발생

<br>

### 컬렉션 페치 조인

**일대다 관계**

JPQL : `select t from Team t join fetch t.members where t.name=’teamA’`

⇒ SQL : `SELECT t.*, m.* FROM TEAM t INNER JOIN MEMBER m ON t.id=m.team_id WHERE t.name=’teamA’` 

```java
String query = "select t from Team t join fetch t.members";

//teamA, 2
//teamA, 2  --> 중복?
//teamB, 1
```

![Untitled](/img/jpa_basic/section11/fetch_join_OTM.png)

join을 하면 위와 같이 teamA에 대해서 2개의 데이터로 나오기 때문에!

<br>

### 중복 제거 DISTINCT

**JPQL DISTINCT 기능**

- SQL의 DISTINCT 기능
- **애플리케이션에서 엔티티 중복 제거**

```java
String query = "select distinct t from Team t join fetch t.members";
```

→ SQL의 DISTINCT만으로 중복 제거가 안되는 것을 JPQL을 통해 중복 제거 가능

> 하이버네이트6부터는 DISTINCT 사용하지 않아도 애플리케이션에서 중복 제거 가능
> 

<br>

### 페치 조인 vs 일반 조인

- 일반 조인에서 쿼리는 join 으로 나감 but **select 절에서 실제 가져오는 것은 team에 대한 것만 가져옴**
- 페치 조인을 사용할 때만 연관된 엔티티도 함께 조회 ⇒ **즉시 로딩**
- 페치 조인은 객체 그래프 SQL 한 번에 조회하는 개념
- 엔티티에 직접 적용하는 글로벌 로딩 전략보다 우선 ( `FetchType.LAZY` )

<br>

## ⛵️ 페치 조인 2 - 한계

- 페치 조인 대상에 별칭 X
    - 하이버네이트는 가능하나 가급적 사용 X
    - 객체 그래프는 filter 거치지 않고 다 조회하는 것을 기본으로 함
- 둘 이상의 컬렉션은 페치 조인 X
- 컬렉션 페치 조인시 페이징 API 사용 X
    - 단일 값 필드(일대일, 다대일)는 페치 조인으로 페이징 가능
    - `@BatchSize` 로 해결 → select 할 때 in으로 한 번에 조회

<br>

### 정리

모든 것을 페치 조인으로 해결할 수는 없음

객체 그래프를 유지할 때 사용하면 효과적

여러 테이블을 조인해서 엔티티가 가진 모양이 아닌 전혀 다른 결과를 내야 한다면 일반 조인을 사용하고 필요한 데이터들만 조회해서 DTO로 반환하는 것이 효과적

<br>

## 🚀 다형성 쿼리

조회대상을 특정 자식으로 한정할 수 있음

`select i from Item i where type(i) IN (Book, Movie)` → `select i from i where i.DTYPE in (’B’, ‘M’)`

<br>

### TREAT

자바 타입 캐스팅

부모 타입을 특정 자식 타입으로 다룰 때 사용

`select i from Item i where treat(i as Book).author = ‘kim’` → `select i.* from Item i where i.DTYPE=’B’ and i.author = ‘kim’`

<br>

## ✉️ 엔티티 직접 사용

### 기본키 값

JPQL에서 엔티티 직접 사용하면 SQL에서 해당 엔티티의 **기본키 값** 사용

- `select count(m) from Member m` → `select count(m.id) as cnt from Member m`
- `select m from Memberm whwere m = :member` → `select m.* from Member m where m.id=?`

<br>

### 외래키 값

- `select m from Member m where m.team = :team` → `select m.* from Member m where m.team_id=?`

<br>

## 🎤 Named 쿼리

미리 정의해서 이름 부여해두고 사용하는 JPQL

정적 쿼리

어노테이션, XML에 정의 가능

✨ 애플리케이션 로딩 시점에 초기화 후 재사용 → sql로 미리 파싱 후 캐싱

✨ 애플리케이션 로딩 시점에 쿼리 검증 → 컴파일 오류!

```java
@Entity
@NamedQuery(
	name = "Member.findBuUsername",
	query = "select m from Member m where m.username = :username")
public class Member { ... }

List<Member> resultList = em.createNamedQuery("Member.findUsername", Member.class)
															.setParameter("username", "회원1").getResultList();
```

> *Spring Data JPA에서 인터페이스 메서드 위에다가 바로 작성할 수 있음 → 즉 네임드 쿼리! 따라서 위와 같이는 잘 사용 X*
> 

<br>

## 🌧 벌크 연산

- SQL의 update, delete 문!
- 재고가 10개 미만인 모든 상품 가격 10% 상승하려면?
    
    → JPA 변경 감지 기능으로 실행하려면 많은 SQL 실행
    
- 쿼리 한 번으로 여러 테이블의 row 변경 가능
    - `executeUpdate()`

<br>

### 주의

영속성 컨텍스트 무시하고 DB에 직접 쿼리 ⇒ 영속성 컨텍스트와 DB간 데이터 차이 생김

- 방법1. 벌크 연산 먼저 실행
- 방법2. 벌크 연산 수행 후 영속성 컨텍스트 초기화