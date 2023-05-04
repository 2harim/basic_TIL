**목차**
- [🧚 프로젝션과 결과 반환](#프로젝션과-결과-반환)
  - [기본](#기본)
  - [DTO 조회](#dto-조회)
  - [프로젝션 결과 반환 @QueryProjection](#프로젝션-결과-반환-queryprojection)
- [🎢 동적 쿼리](#동적-쿼리)
  - [BooleanBuilder 사용](#booleanbuilder-사용)
  - [Where 다중 파라미터 사용](#where-다중-파라미터-사용)
- [💨 수정, 삭제 벌크 연산](#수정-삭제-벌크-연산)
- [🍊 SQL function 호출하기](#sql-function-호출하기)

<br>

## 🧚 프로젝션과 결과 반환

*프로젝션 : select 대상 지정*

<br>

### 기본

- 대상이 하나인 경우
    - 타입 명확하게 지정 가능
- 둘 이상인 경우
    - 튜플
        
        `com.querydsl.core.Tuple`  
        
        querydsl에 의존적, repository 계층을 넘어서는 것은 좋지 않음
        
    - DTO
    
<br>

### DTO 조회

- 순수 JPA에서 DTO 조회
    - new 명령어 사용
    - DTO 패키지명까지 작성 필요
    - 생성자 방식만 지원
    
    ```java
    List<MemberDto> resultList = 
    		em.createQuery("select new study.querydsl.dto.MemberDto(m.username, m.age) from Member m", MemberDto.class)
        .getResultList();
    ```
    
- Querydsl 빈 생성
    - 프로퍼티 접근 `Projections.bean()`
    - 필드 접근 `Projections.fields()`
    - 생성자 사용 `Projections.constructor()`
    
    ```java
    List<MemberDto> result = queryFactory
            .select(Projections.bean(MemberDto.class,
                member.username, 
    						member.age))
            .from(member)
            .fetch();
    ```
    

> 별칭 다를 때
> 
> 1. *ExpressionUtils.as(source, alias)*
> 2. *username.as(”alias”)*

<br>

### 프로젝션 결과 반환 @QueryProjection

DTO 생성자에 `@QueryProjection` 달아주고 `gradle - compileQuerydsl` 하면 DTO도 Q 타입 생성됨

constructor의 경우 컴파일 오류를 못잡는 문제를 보완해줌

```java
@QueryProjection
public MemberDto(String username, int age) {
    this.username = username;
    this.age = age;
}

...

List<MemberDto> result = queryFactory
	      .select(new QMemberDto(member.username, member.age))
	      .from(member)
	      .fetch();
```

**[단점]**

새로운 파일을 생성함( `QMemberDto` )

Dto를 여러 layer에서 사용하는데 Querydsl에 의존성이 생김

<br>

## 🎢 동적 쿼리

### BooleanBuilder 사용

```java
private List<Member> searchMember1(String usernameCond, Integer ageCond) {

    BooleanBuilder builder = new BooleanBuilder();
    if (usernameCond != null) {
        builder.and(member.username.eq(usernameCond));
    }

    if (ageCond != null) {
        builder.and(member.age.eq(ageCond));
    }

    return queryFactory
        .selectFrom(member)
        .where(builder)
        .fetch();
}
```

<br>

### Where 다중 파라미터 사용

```java
return queryFactory
        .selectFrom(member)
        .where(usernameEq(usernameCond), ageEq(ageCond))
        .fetch();

private Predicate usernameEq(String usernameCond) {
    return usernameCond != null ? member.username.eq(usernameCond) : null;
}

private Predicate ageEq(Integer ageCond) {
    return ageCond != null ? member.age.eq(ageCond) : null;
}
```

- 메서드 명을 보고 어떤 역할을 하는 것인지 바로 파악 가능 → 가독성 👍
- where조건에서 null일 때 무시 가능
- 메서드 재활용 가능
- 조합 가능
    
    ```java
    private BooleanExpression allEq(String usernameCond, Integer ageCond) {
    	return usernameEq(usernameCond).and(ageEq(ageCond));
    }
    ```
    
    ⚠️ null 체크 주의 필요
    
<br>

## 💨 수정, 삭제 벌크 연산

JPA는 엔티티 가지고 와서 엔티티 값만 바꾸면 transaction commit 시점에 flush 발생해서 변경감지가 일어나서 update 쿼리가 발생하게 됨 → 개별적으로 발생!

한 번에 바꾸고 싶다면 쿼리 한 번으로 수정하고 싶음 

⇒ 벌크 연산 : 쿼리 한 번으로 대량 데이터 수정 

```java
long count = queryFactory
    .update(member)
    .set(member.username, "비회원")
    .where(member.age.lt(28))
    .execute();

//member1 = 10 -> 비회원
//member2 = 20 -> 비회원
```

DB에서 select 해와서 값이 다른 경우 영속성컨텍스트에 있는 값을 우선으로 결정

따라서 바로 조회 쿼리를 실행하면 원래대로 member1, member2로 출력됨 

⇒  `em.flush(); em.clear();`  필수로 하는게 좋음!

<br>


```java
//add
queryFactory
    .update(member)
    .set(member.age, member.age.add(1))
    .execute();

//multiply
queryFactory
    .update(member)
    .set(member.age, member.age.multiply(1))
    .execute();

//delete
queryFactory
    .delete(member)
    .where(member.age.gt(18))
    .execute();
```

<br>

## 🍊 SQL function 호출하기

JPA는 Dialect에 등록된 내용만 호출 가능

- replace 함수
    
    ```java
    List<String> result = queryFactory
            .select(Expressions.stringTemplate(
    		            "function('replace', {0}, {1}, {2})",
    		            member.username, "member", "M"))
            .from(member)
            .fetch();
    ```
    

- lower 함수
    
    ```java
    List<String> result = queryFactory
                .select(member.username)
                .from(member)
    	          .where(member.username.eq(
                    Expressions.stringTemplate("function('lower', {0})", member.username)))
                .fetch();
    ```
    
    > lower같은 ansi 표준은 querydsl에서 내장하고 있는 경우가 많음! 확인해보기
    `.where(member.username.eq(member.username.lower()))`
    >