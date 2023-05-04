**목차**
- [🍋 스프링 데이터 JPA 리포지토리로 변경](#스프링-데이터-jpa-리포지토리로-변경)
- [⛑ 사용자 정의 리포지토리](#사용자-정의-리포지토리)
  - [사용법](#사용법)
- [🍦 스프링 데이터 페이징 활용1](#-스프링-데이터-페이징-활용1)
  - [Querydsl 페이징 연동](#querydsl-페이징-연동)
  - [CountQuery 최적화](#countquery-최적화)

<br>

## 🍋 스프링 데이터 JPA 리포지토리로 변경

Spring Data JPA를 활용한 Repository 생성

```java
public interface MemberRepository extends JpaRepository<Member, Long> {

    //select m from Member m where m.username = ?
    List<Member> findByUsername(String username);
}
```

<br>

## ⛑ 사용자 정의 리포지토리

Querydsl 을 활용한 쿼리는 Spring Data JPA로는 할 수 없음 ⇒ 사용자 정의 리포지토리 필요

![스크린샷 2023-05-04 오전 11.52.48.png](/img/querydsl/section6/custom_repository.png)

<br>

### 사용법

1. **사용자 정의 인터페이스 작성**
    
    [MemberRepositoryCustom]
    
    ```java
    public interface MemberRepositoryCustom {
    
        List<MemberTeamDto> search(MemberSearchCondition condition);
    }
    ```
    
2. **사용자 정의 인터페이스 구현**
    
    [MemberRepositoryImpl] - `MemberRepository` 와 이름 맞추고 “Impl” 을 붙여주면 됨
    
    ```java
    public class MemberRepositoryImpl implements MemberRepositoryCustom{
    
        private final JPAQueryFactory queryFactory;
    
        public MemberRepositoryImpl(EntityManager em) {
            this.queryFactory = new JPAQueryFactory(em);
        }
    
        @Override
        public List<MemberTeamDto> search(MemberSearchCondition condition) {
            return queryFactory
                .select(new QMemberTeamDto(
                    member.id.as("memberId"),
                    member.username,
                    member.age,
                    team.id.as("teamId"),
                    team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                    usernameEq(condition.getUsername()),
                    teamNameEq(condition.getTeamName()),
                    ageGoe(condition.getAgeGoe()),
                    ageLoe(condition.getAgeLoe())
                )
                .fetch();
        }
    
        private BooleanExpression usernameEq(String username) {
            return hasText(username) ? member.username.eq(username) : null;
        }
    
        private BooleanExpression teamNameEq(String teamName) {
            return hasText(teamName) ? team.name.eq(teamName) : null;
        }
    
        private BooleanExpression ageGoe(Integer ageGoe) {
            return ageGoe != null ? member.age.goe(ageGoe) : null;
        }
    
        private BooleanExpression ageLoe(Integer ageLoe) {
            return ageLoe != null ? member.age.loe(ageLoe) : null;
        }
    
    }
    ```
    
3. **스프링 데이터 리포지토리에 사용자 정의 인터페이스 상속**
    
    ```java
    public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom { .. } 
    ```
    

> *또는 특정 상황에 특화되어 있다면 별도의 repository 만든 후 @Repository 붙여서 구현체 만들어줌!*
> 

<br>

## 🍦 스프링 데이터 페이징 활용1


### Querydsl 페이징 연동

Page, Pageable 활용
 - 전체 카운트 한 번에 조회
     
     ```java
     @Override
     public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable) {
         QueryResults<MemberTeamDto> results = queryFactory
             .select(new QMemberTeamDto(
                 member.id.as("memberId"),
                 member.username,
                 member.age,
                 team.id.as("teamId"),
                 team.name.as("teamName")
             ))
             .from(member)
             .leftJoin(member.team, team)
             .where(
                 usernameEq(condition.getUsername()),
                 teamNameEq(condition.getTeamName()),
                 ageGoe(condition.getAgeGoe()),
                 ageLoe(condition.getAgeLoe())
             )
             .offset(pageable.getOffset())
             .limit(pageable.getPageSize())
             .fetchResults();
     
         List<MemberTeamDto> content = results.getResults();
         long total = results.getTotal();
     
         return new PageImpl<>(content, pageable, total);
     }
     ```
     
     `fetchResults()` 사용 → 실제 쿼리 2번 호출
     
     > *QueryDsl의 fetchResults()가 곧 사라짐 ← group By 등 복잡한 쿼리에서 count 쿼리를 자동 생성하는데 오류 문제*
     > 
     > 
     > *count query 따로 작성*
     > 
     
 - 데이터 내용, 전체 카운트 별도 조회
     
     ```java
     @Override
     public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable) {
         List<MemberTeamDto> content = queryFactory
             .select(new QMemberTeamDto(
                 member.id.as("memberId"),
                 member.username,
                 member.age,
                 team.id.as("teamId"),
                 team.name.as("teamName")
             ))
             .from(member)
             .leftJoin(member.team, team)
             .where(
                 usernameEq(condition.getUsername()),
                 teamNameEq(condition.getTeamName()),
                 ageGoe(condition.getAgeGoe()),
                 ageLoe(condition.getAgeLoe())
             )
             .offset(pageable.getOffset())
             .limit(pageable.getPageSize())
             .fetch();
     
         long total = queryFactory
             .select(member)
             .from(member)
             .leftJoin(member.team, team)
             .where(usernameEq(condition.getUsername()),
                 teamNameEq(condition.getTeamName()),
                 ageGoe(condition.getAgeGoe()),
                 ageLoe(condition.getAgeLoe()))
             .fetchCount();
     
         return new PageImpl<>(content, pageable, total);
     }
     ```
     
     `fetch()` 와 `fetchCount()` 를 따로 호출하여 분리 → 전체 카운트를 조회할 때 조인 쿼리를 줄인다면 성능 이점 있음
        
<br>

### CountQuery 최적화

**[count 쿼리 생략 가능한 경우]**

- 페이지 시작이면서 컨텐츠 사이즈가 페이지 사이즈보다 작을 때
- 마지막 페이지 일 때 (offset + 컨텐츠 사이즈로 전체 사이즈 구함)

⇒ 스프링 데이터 라이브러리가 제공하는 `PageableExecutionUtils.getPage()` 활용

```java
JPAQuery<Member> countQuery = queryFactory
    .select(member)
    .from(member)
    .leftJoin(member.team, team)
    .where(usernameEq(condition.getUsername()),
        teamNameEq(condition.getTeamName()),
        ageGoe(condition.getAgeGoe()),
        ageLoe(condition.getAgeLoe()));

return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount);
```

위의 두 조건의 경우 `fetchCount()` 호출 X