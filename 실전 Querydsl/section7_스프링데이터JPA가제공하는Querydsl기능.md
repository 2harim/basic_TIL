**목차**
- [인터페이스 지원 - QuerydslPredicateExecutor](#인터페이스-지원---querydslpredicateexecutor)
  - [한계점](#한계점)
- [Querydsl Web 지원](#querydsl-web-지원)
  - [한계점](#한계점-1)
- [리포지토리 지원 - QuerydslRepositorySupport](#리포지토리-지원---querydslrepositorysupport)
  - [장점](#장점)
  - [한계점](#한계점-2)


<br>

> *다음에 소개할 기능은 제약이 커서 복잡한 실무 환경에서 사용하기에는 많이 부족함..!*
> 

## 인터페이스 지원 - QuerydslPredicateExecutor

리포지토리에 적용

```java
public interface MemberRepository
		extends JpaRepository<Member, Long>, MemberRepositoryCustom, **QuerydslPredicateExecutor<Member>** { ... }
```

사용

```java
Iterable<Member> result = memberRepository.findAll(
        member.age.between(10, 40)
        .and(member.username.eq("member1")));
```

Querydsl의 조건을 Spring Data JPA와 함께 사용 가능

<br>

### 한계점

- 조인이 안됨(묵시적 조인은 o, left join X)
- 클라이언트가 Querydsl에 의존해야 함 → Service 계층이 Querydsl 구현 기술에 의존 필요

<br>

## Querydsl Web 지원

parameter binding을 Querydsl의 Predicate로 만들어줌 

`?firstname=Dave&lastname=Matthews` → `QUser.user.firstname.eq("Dave").and(QUser.user.lastname.eq("Matthews"))` 

<br>

### 한계점

- eq, contains, in 만 가능
- 추가 작업 필요 ← 복잡
- 컨트롤러가 Querydsl에 의존해야 함

<br>

## 리포지토리 지원 - QuerydslRepositorySupport

```java
public class MemberRepositoryImpl
        extends QuerydslRepositorySupport implements MemberRepositoryCustom {

		public MemberRepositoryImpl(Class<?> domainClass) { //JPAQueryFactory 생성자 대신!
        super(Member.class); 
    }

		...

		@Override
    public List<MemberTeamDto> search(MemberSearchCondition condition) {

        List<MemberTeamDto> result = from(member)
            .leftJoin(member.team, team)
            .where(
                usernameEq(condition.getUsername()),
                teamNameEq(condition.getTeamName()),
                ageGoe(condition.getAgeGoe()),
                ageLoe(condition.getAgeLoe())
            )
            .select(
                new QMemberTeamDto(
                    member.id.as("memberId"),
                    member.username,
                    member.age,
                    team.id.as("teamId"),
                    team.name.as("teamName")
                )
            )
            .fetch();
				return result;
		}

		@Override
    public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable) {

        JPQLQuery<MemberTeamDto> jpaQuery = from(member)
            .leftJoin(member.team, team)
            .where(usernameEq(condition.getUsername()),
                teamNameEq(condition.getTeamName()),
                ageGoe(condition.getAgeGoe()),
                ageLoe(condition.getAgeLoe()))
            .select(new QMemberTeamDto(
                member.id.as("memberId"),
                member.username,
                member.age,
                team.id.as("teamId"),
                team.name.as("teamName")));

        JPQLQuery<MemberTeamDto> query = getQuerydsl().applyPagination(pageable, jpaQuery);//offset, limit 적용
        return query.fetch();
		}
```

<br>

### 장점

- 스프링 데이터가 제공하는 페이징을 Querydsl로 편리하게 변환 가능 `getQuerydsl().applyPagination()`
- `from` 으로 시작 가능 → 하지만 최근엔 `select()` 로 시작해서 더 명시적
- EntityManager 제공

<br>

### 한계점

- Querydsl 3.x 버전 대상으로 만들었기 때문에 4.X에 나온 JPAQueryFactory X
- 스프링 데이터 Sort 기능 정상 동작 X