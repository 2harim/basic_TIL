**목차**
- [⚖️ JPQL vs Querydsl](#️jpql-vs-querydsl)
  - [Querydsl 장점](#querydsl-장점)
- [⏳ 기본 Q-Type 활용](#기본-q-type-활용)
- [🥁 검색 조건 쿼리](#검색-조건-쿼리)
- [🎬 결과 조회](#결과-조회)
- [🧤 정렬](#정렬)
- [📑 페이징](#페이징)
- [👥 집합](#집합)
- [🫂 조인](#조인)
  - [기본 조인](#기본-조인)
  - [ON 절](#on-절)
  - [페치 조인](#페치-조인)
- [🍄 서브 쿼리](#서브-쿼리)
  - [from 절의 서브쿼리 한계](#from-절의-서브쿼리-한계)
  - [**해결방안**](#해결방안)
- [👑 Case 문](#case-문)
- [🐳 상수, 문자 더하기](#상수-문자-더하기)

<br>

## ⚖️ JPQL vs Querydsl

[JPQL]

```java
String qlString = "select m from Member m "
                         + "where m.username = :username";
Member findMember = em.createQuery(qlString, Member.class)
    .setParameter("username", "member1")
    .getSingleResult();
```

[Querydsl]

```java
QMember m = new QMember("m");

Member findMember = queryFactory
    .select(m)
    .from(m)
    .where(m.username.eq("member1")) //파라미터 바인딩 처리
    .fetchOne();
```

<br>

### Querydsl 장점

- 컴파일 오류
- 파라미터 바인딩 자동 처리
- 자동완성

> **JPAQueryFactory 필드로 제공했을 때 동시성 문제**
> 
> 
> 동시성 문제는 JPAQueryFactory 생성할 때 제공하는 EntityManager에 달려있는데 여러 쓰레드에서 동시에 같은 EntityManager에 접근해도 트랜잭션마다 별도의 영속성 컨텍스트를 제공하기 때문에 문제 X
> 

<br>

## ⏳ 기본 Q-Type 활용

1. 별칭 직접 지정
    
    `QMember qMember = new QMember("m");` 
    
2. 기본 인스턴스 사용
    
    `QMember qMember = QMember.member;` 
    
<br>

## 🥁 검색 조건 쿼리

- `and()` , `or()` : 메서드 체인으로 연결
    - `and()` 는 `,(comma)` 로 대체 가능
    - `,`  null 을 무시하고 코드가 간단해지기 때문에 권장

<br>

## 🎬 결과 조회

`fetch` : 리스트 조회, 데이터 없으면 빈 리스트

`fetchOne` : 딱 하나 조회, 없으면 Null, 둘 이상이면 NonUniqueResultException

`fetchFirst` : limit(1).fetchOne()

`fetchResults` : 페이징 정보 포함, total count 쿼리 추가 실행

`fetchCount` : count 쿼리로 변경해서 count 수 조회

<br>

## 🧤 정렬

```java
List<Member> result = queryFactory
            .selectFrom(member)
            .where(member.age.eq(100))
            .orderBy(member.age.desc(), member.username.asc().nullsLast())
            .fetch();
```

```sql
select
            member0_.member_id as member_i1_1_,
            member0_.age as age2_1_,
            member0_.team_id as team_id4_1_,
            member0_.username as username3_1_ 
        from
            member member0_ 
        where
            member0_.age=? 
        order by
            member0_.age desc,
            member0_.username asc nulls last
```

`desc()` , `asc()` 

`nullsLast()` , `nullsFirst()` : null에 순서 부여

<br>

## 📑 페이징

```java
List<Member> result = queryFactory
            .selectFrom(member)
            .orderBy(member.username.desc())
            .offset(1)
            .limit(2)
            .fetch();

QueryResults<Member> queryResults = queryFactory
             .selectFrom(member)
             .orderBy(member.username.desc())
             .offset(1)
             .limit(2)
				     .fetchResults();

assertThat(queryResults.getTotal()).isEqualTo(4);
assertThat(queryResults.getLimit()).isEqualTo(2);
assertThat(queryResults.getOffset()).isEqualTo(1);
assertThat(queryResults.getResults().size()).isEqualTo(2);
```

`offset()` : 0부터 시작

`limit()` : 조회할 데이터 개수

전체 조회 수가 필요하다면 `fetchResults()` 사용 

> count 쿼리 실행되므로 성능상 주의
> 

<br>

## 👥 집합

- sum(), count(), max(), min(), avg()
- group by, having

> `com.querydsl.core.Tuple` - projection, 추후 DTO 사용
> 

<br>

## 🫂 조인

### 기본 조인

첫 번째 파라미터에 조인 대상 지정하고 두 번째 파라미터에 별칭으로 사용할 Q타입 지정

```java
join(조인 대상, 별칭으로 사용할 Q타입)
```

- join / leftJoin
- theta join
    - 연관관계 없는 필드로 조인
    - from절에 여러 엔티티 선택
    - 외부 조인 불가능 → on을 사용해야 함

<br>

### ON 절

1. 조인 대상 필터링
    
    > on절을 내부 조인에서 사용하면 where 절과 같은 역할을 함
    > 
2. 연관관계 없는 엔티티 외부 조인

`leftJoin(member.team, team)` → id로 매칭, 일반 조인

`leftJoin(team).on(xxx)` → 연관관계 없는 조인, 세타 조인에 사용

<br>

### 페치 조인

SQL이 제공하는 기능 X 

SQL 조인 활용하여 연관된 엔티티를 SQL 한번에 조회하는 기능 ← 성능 최적화

`join` 이나 `leftJoin` 등 뒤에 `fetchJoin()` 추가! ****

`.join()**.fetchJoin()`** 

<br>

## 🍄 서브 쿼리

`com.querydsl.jpa.JPAExpressions` 사용

```java
List<Member> result = queryFactory
            .selectFrom(member)
            .where(member.age.eq(
                JPAExpressions
										.select(memberSub.age.max())
                    .from(memberSub)
            ))
            .fetch();
```

`JPAExpressions` 는 static import 통해서 생략 가능

<br>

### from 절의 서브쿼리 한계

JPA JPQL 서브쿼리 한계점으로 from 절의 서브쿼리 지원 X, Querydsl도 지원 X

<br>

### **해결방안**

1. 서브 쿼리를 join으로 변경(가능한 상황과 그렇지 않은 상황 모두 존재)
2. 애플리케이션에서 쿼리 2번 분리하여 실행
3. nativeSQL 실행

<br>

## 👑 Case 문

select, where, order by에서 사용 가능

**[단순한 조건]**

```java
List<String> result = queryFactory
            .select(member.age
                .when(10).then("열살")
                .when(20).then("스무살")
                .otherwise("기타"))
            .from(member)
            .fetch();
```

**[복잡한 조건]**

```java
List<String> list = queryFactory
            .select(new CaseBuilder()
                .when(member.age.between(0, 20)).then("0~20살")
                .when(member.age.between(21, 30)).then("21~30살")
                .otherwise("기타"))
            .from(member)
            .fetch();
```

<br>

## 🐳 상수, 문자 더하기

`Expressions.constant()` 

```java
List<Tuple> result = queryFactory
        .select(member.username, Expressions.constant("A"))
        .from(member)
        .fetch();
```

<br>

`concat()` 

```java
List<String> result = queryFactory
        .select(member.username.concat("_").concat(member.age.stringValue()))
        .from(member)
        .fetch();
```

username은 문자고 age는 숫자이기 때문에 타입 통일 필요!