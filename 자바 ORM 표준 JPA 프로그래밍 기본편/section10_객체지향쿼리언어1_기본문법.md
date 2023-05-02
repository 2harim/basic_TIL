**목차**
- [👋 소개](#소개)
  - [JPA가 지원하는 쿼리 방법](#jpa가-지원하는-쿼리-방법)
  - [JPQL](#jpql)
  - [Criteria](#criteria)
  - [QueryDSL](#querydsl)
  - [네이티브 쿼리](#네이티브-쿼리)
  - [JDBC 직접 사용, SpringJdbcTemplate 등](#jdbc-직접-사용-springjdbctemplate-등)
- [🍉 기본 문법과 쿼리 API](#기본-문법과-쿼리-api)
  - [JPQL - Java Persistence Query Language](#jpql---java-persistence-query-language)
  - [JPQL 문법](#jpql-문법)
  - [결과 조회](#결과-조회)
  - [파라미터 바인딩](#파라미터-바인딩)
- [🎣 프로젝션 (SELECT)](#프로젝션-select)
  - [여러 값 조회](#여러-값-조회)
- [📖 페이징](#페이징)
- [🤝 조인](#조인)
  - [ON 절](#on-절)
- [🥅 서브쿼리](#서브쿼리)
  - [지원 함수](#지원-함수)
  - [한계](#한계)
- [🗯 JPQL 타입 표현과 기타식](#jpql-타입-표현과-기타식)
- [🛠 조건식 (CASE 등등)](#조건식-case-등등)
- [🏭 JPQL 함수](#jpql-함수)
  - [JPQL 기본 함수](#jpql-기본-함수)
  - [사용자 정의 함수](#사용자-정의-함수)

<br>

## 👋 소개

### JPA가 지원하는 쿼리 방법

- **JPQL**
- JPA Criteria
- **QueryDSL**
- native SQL → 특정 벤더에 맞게 쿼리 작성해야 할 때
- JDBC API 직접 사용, MyBatis/SpringJdbcTemplate 함께 사용

> *실무에서 대부분 JPQL 로 해결, 표준 문법으로 안되는게 존재할 땐 다른 것도 함께 사용*
> 

<br>

### JPQL

가장 단순한 조회 방법 : `EntityManager.find()` , 객체 그래프 탐색

JPA 사용하면 엔티티 객체 중심으로 개발해야 함 이때 검색 할 때도 테이블이 아닌 엔티티 객체 대상으로 검색

SQL 추상화한 JPQL 라는 객체 지향 쿼리 언어 제공

SQL 문법 유사 → SELECT, FROM, WHER  E, GROUP BY, HAVING, JOIN 지원

엔티티 객체 대상으로 쿼리

```java
List<Member> result = em.createQuery("select m from Member m where m.username like '%kim%'", Member.class).getResultList();
```

```sql
Hibernate: 
    /* select
        m 
    from
        Member m 
    where
        m.username like '%kim%' */ select
            member0_.MEMBER_ID as member_i1_6_,
            member0_.city as city2_6_,
            member0_.street as street3_6_,
            member0_.zipcode as zipcode4_6_,
            member0_.USERNAME as username5_6_,
            member0_.endDate as enddate6_6_,
            member0_.startDate as startdat7_6_ 
        from
            Member member0_ 
        where
            member0_.USERNAME like '%kim%'
```

<br>

### Criteria

JPQL로 해결이 어려운 것 → 동적 쿼리 (문자열을 조건에 따라 더해야함)

```java
CriteriaBuilder cb = em.getCriteriaBuilder();
CriteriaQuery<Member> query = cb.createQuery(Member.class);

Root<Member> m = query.from(Member.class);
CriteriaQuery<Member> cq = query.select(m).where(cb.equal(m.get("username"), "kim"));
List<Member> resultList = em.createQuery(cq).getResultList();
```

```sql
Hibernate: 
    /* select
        generatedAlias0 
    from
        Member as generatedAlias0 
    where
        generatedAlias0.username=:param0 */ select
            member0_.MEMBER_ID as member_i1_6_,
            member0_.city as city2_6_,
            member0_.street as street3_6_,
            member0_.zipcode as zipcode4_6_,
            member0_.USERNAME as username5_6_,
            member0_.endDate as enddate6_6_,
            member0_.startDate as startdat7_6_ 
        from
            Member member0_ 
        where
            member0_.USERNAME=?
```

문자가 아닌 자바코드로 JPQL 작성 가능

동적쿼리 작성 가능

but sql스럽지 않음 → 실무에서 X, 유지보수 어려움

⇒ **QueryDSL 사용 권장**

<br>

### QueryDSL

자바코드로 JPQL 작성

JPQL 빌더 역할

컴파일 시점에 문법 오류 찾을 수 있음

동적쿼리 작성 편리

**실무 사용 권장**

<br>

### 네이티브 쿼리

JPA가 제공하는 SQL 직접 사용하는 기능

특정 DB에 의존적인 기능

`em.createNativeQuery()` 

<br>

### JDBC 직접 사용, SpringJdbcTemplate 등

영속성 컨텍스트를 적절한 시점에 강제로 플러시 필요!!

> *flush는 commit할 때와 query 날라갈 때 발생*
> 

<br>

## 🍉 기본 문법과 쿼리 API

### JPQL - Java Persistence Query Language

객체 지향 쿼리 언어, 엔티티 객체 대상으로 쿼리

SQL 추상화해서 특정 DB SQL에 의존하지 X

결국 SQL 로 변환됨

<br>

### JPQL 문법

- 엔티티와 속성 대소문자 구분 O
    
    JPQL 키워드는 대소문자 구분 X (select, from, where, .. )
    
- 엔티티 이름 사용 ← 테이블 이름 아님!!
- 별칭 필수 (as 생략 가능)
- 집합, 정렬 제공
    
    group by, having, order by
    
- `TypeQuery` : 반환타입 명확할 때 사용
    
    `em.createQuery(”select m from m”, Member.class);`
    
- `Query` : 반환타입 명확하지 않을 때 사용
    
    `em.createQuery(”select m.username, m.age from Member m”);`
    
<br>

### 결과 조회

- `query.getResultList()`
    - 결과 하나 이상일 때 리스트 반환
    - 결과 없으면 빈 리스트 반환
- `query.getSingleResult()`
    - 결과 정확히 하나
    - 결과 없으면 `NoResultException`
    - 둘 이상이면 `NonUniqueResultException`
    - Spring Data Jpa 사용하면 Optional 객체로 반환

<br>

### 파라미터 바인딩

- 이름 기준
    
    `select m from member m where m.username=:username` 
    
    `query.setParameter(”username”, parameter);` 
    
- 위치 기준 *⇒ 웬만하면 사용 X*
    
    `select m from member m where m.username=?1` 
    
    `query.setParameter(1, parameter);` 
    
<br>

## 🎣 프로젝션 (SELECT)

select 절에 조회할 대상 지정하는 것

대상 : 엔티티, 임베디드 타입, 스칼라 타입(숫자, 문자 등 기본 데이터 타입)

`Select **m** FROM Meber m` → 엔티티 프로젝션 Member

`Select **m.team** FROM Meber m` → 엔티티 프로젝션 Team

`Select **m.address** FROM Meber m` → 임베디드 타입 프로젝션 Address

`Select **m.username, m.age** FROM Meber m` → 스칼라 타입 프로젝션

> *DISTINCT로 중복 제거 가능*
> 

`List<Member> result = em.createQuery("select m from Member m", Member.class).getResultList();`

엔티티 프로젝션 → 영속성컨텍스트에 반영 ⭕️

<br>

### 여러 값 조회

`Select **m.username, m.age** FROM Meber m`

1. Query 타입으로 조회
    
    결과가 Object 배열로 나옴
    
2. Object[] 타입으로 조회 
3. **new 명령어로 조회 → 단순 값을 DTO로 바로 조회**
    
    ```java
    List<MemberDTO> result = em.createQuery("select new jpql.MemberDTO(m.username, m.age) from Member m", MemberDTO.class).getResultList();
    ```
    
    > *패키지 명 포함한 전체 클래스 명 입력, 생성자(순서, 타입 일치) 필요*
    > 

<br>

## 📖 페이징

페이징을 다음 API로 추상화

- `setFirstResult(int startPosition)` : 조회 시작 위치 (0부터)
- `setMaxResult(int maxResult)` : 조회할 데이터 수

```java
List<Member> result = em.createQuery("select m from Member m order by m.age desc", Member.class)
        .setFirstResult(0)
        .setMaxResults(10)
        .getResultList();
```

```java
select
    member0_.id as id1_0_,
    member0_.age as age2_0_,
    member0_.TEAM_ID as team_id4_0_,
    member0_.username as username3_0_ 
from
    Member member0_ 
order by
    member0_.age desc limit ? offset ?
```

<br>

## 🤝 조인

- **내부 조인**
    
    `SELECT m FROM Member m [INNER] JOIN m.team t` 
    
- **외부 조인**
    
    `SELECT m FROM Member m LEFT [OUTER] JOIN [m.team](http://m.team) t` 
    
- **세타 조인 (cross join)**
    
    `SELECT count(m) FROM Member m, Team t WHERE m.username = [t.name](http://t.name)` 
    
<br>

### ON 절

- 조인 대상 필터링
    
    `SELECT m, t FROM Member m LEFT JOIN m.team t on t.name = ‘A’`
    
- 연관관계 없는 엔티티 외부 조인
    
    `SELECT m, t FROM Member m LEFT JOIN Team t on m.username = [t.name](http://t.name)` 
    
    > *이전에는 내부 조인만 가능했음*
    > 

<br>

## 🥅 서브쿼리

### 지원 함수

- `EXISTS` : 서브쿼리에 결과 존재하면 참
    - `ALL` : 모두 만족하면 참
    - `ANY` , `SOME` : 조건 하나라도 만족하면 참
- `IN` : 서브쿼리 결과 중 하나라도 같은 것이 있으면 참

<br>

### 한계

- WHERE, HAVING 절에서만 서브쿼리 사용 가능
- 하이버네이트에서는 SELECT 절도 가능
    
    `SELECT (SELECT avg(m1.age) FROM Member m1) as avg FROM Member m WHERE m.age = 10` 
    
- **FROM 절의 서브쿼리는 JPQL에서 불가능** 
→ 조인으로 풀 수 있다면 해결, 쿼리 분해해서 날리기, 애플리케이션에서 해결, native

<br>

## 🗯 JPQL 타입 표현과 기타식

- 문자 : `‘` , `‘` 는 `‘’`
- 숫자 : L (Long), D (Double), F (Float)
- Boolean : `TRUE` , `FALSE`
- Enum : 패키지명 포함해서 사용
- 엔티티 타입 : `TYPE(m) = Member` → 상속관계에서 사용

<br>

## 🛠 조건식 (CASE 등등)

- 기본 CASE : 조건식
    
    ```java
    String query = "select " +
    	                  "case when m.age <= 10 then '학생요금'" +
    	                  "when m.age >= 60 then '경로요금'" +
    	                  "else '일반요금'" +
    	                  "end " +
    	              "from Member m";
    ```
    
- 단순 CASE : exact matching
- COALESCE : 하나씩 조회해서 null이 아니면 반환
- NULLIF : 두 값 같으면 null, 다르면 첫번째 값 반환

<br>

## 🏭 JPQL 함수

### JPQL 기본 함수

- `CONCAT` : 문자열 합치기, ||
- `SUBSTRING` : 문자열 자르기
- `TRIM` : 공백 제거
- `LOWER` , `UPPER` : 대소문자
- `LENGTH` : 문자열 길이
- `LOCATE` : 문자열의 위치 반환
- `ABS` , `SQRT` , `MOD`
- `SIZE` :  연관관계에서 컬렉션 크기
- `INDEX` : `@OrderColumn` 값 타입일 때 컬렉션 위치 값 구할 때

DB 종속적인 함수들이 방언을 통해 다 등록되어 있음 

<br>

### 사용자 정의 함수

사용 전 DB 방언 상속 받은 클래스 작성

사용자 정의 함수 등록