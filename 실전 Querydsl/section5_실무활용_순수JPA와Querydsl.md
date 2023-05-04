**목차**
- [🎃 순수 JPA 리포지토리와 Querydsl](#순수-jpa-리포지토리와-querydsl)
- [🌧 동적 쿼리와 성능 최적화 조회](#동적-쿼리와-성능-최적화-조회)
  - [Builder 사용](#builder-사용)
  - [Where절 파라미터 사용](#where절-파라미터-사용)
- [🖥 조회 API 컨트롤러 개발](#조회-api-컨트롤러-개발)

<br>

## 🎃 순수 JPA 리포지토리와 Querydsl

```java
@Repository
public class MemberJpaRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public MemberJpaRepository(EntityManager em, JPAQueryFactory jpaQueryFactory) {
        this.em = em;
        this.queryFactory = jpaQueryFactory;
    }

    public void save(Member member) {
        em.persist(member);
    }

    public Optional<Member> findById(Long id) {
        Member findMember = em.find(Member.class, id);
        return Optional.ofNullable(findMember);
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class).getResultList();
    }

    public List<Member> findAll_Querydsl() {
        return queryFactory
                .selectFrom(member)
                .fetch();
    }

    public List<Member> findByUsername(String username) {
        return em.createQuery("select m from Member m where m.username = :username", Member.class)
            .setParameter("username", username)
            .getResultList();
    }

    public List<Member> findByUsername_Querydsl(String username) {
        return queryFactory
            .selectFrom(member)
            .where(member.username.eq(username))
            .fetch();
    }
}
```

> JpaQueryFactory는 main application에 bean등록해도 됨
> 
> 
> ```java
> @Bean
> JPAQueryFactory jpaQueryFactory(EntityManager em) {
> 		return new JPAQueryFactory(em);
> }
> ```
> 

*spring이 주입해주는 entity manager는 proxy entity manager이고 이 가짜 entity manager가 실제 사용 시점에 트랜잭션 단위로 실제 entity manager를 할당하여 동시성 문제를 해결해주므로 멀티쓰레드 환경에서 문제 없음!*

<br>

## 🌧 동적 쿼리와 성능 최적화 조회

### Builder 사용

<br>

[조회 최적화 DTO 추가]

```java
@Data
public class MemberTeamDto {

    private Long memberId;
    private String username;
    private int age;
    private Long teamId;
    private String teamName;

    @QueryProjection
    public MemberTeamDto(Long memberId, String username, int age, Long teamId, String teamName) {
        this.memberId = memberId;
        this.username = username;
        this.age = age;
        this.teamId = teamId;
        this.teamName = teamName;
    }
}
```

- `@QueryProjection` 으로 `QMemberTeamDto` 생성 ← Querydsl에 의존

<br>

[회원 검색 조건]

```java
@Data
public class MemberSearchCondition {

    //회원명, 팀명, 나이(ageGoe, ageLoe)

    private String username;
    private String teamName;
    private Integer ageGoe;
    private Integer ageLoe;
}
```

<br>

[동적쿼리 Builder 사용]

```java
public List<MemberTeamDto> searchByBuilder(MemberSearchCondition condition) {

    BooleanBuilder builder = new BooleanBuilder();
    if (hasText(condition.getUsername())) {
        builder.and(member.username.eq(condition.getUsername()));
    }
    if (hasText(condition.getTeamName())) {
        builder.and(team.name.eq(condition.getTeamName()));
    }
    if (condition.getAgeGoe() != null) {
        builder.and(member.age.goe(condition.getAgeGoe()));
    }
    if (condition.getAgeLoe() != null) {
        builder.and(member.age.loe(condition.getAgeLoe()));
    }

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
        .where(builder)
        .fetch();
}
```

> `QMemberTeamDto` 에서 생성자를 사용하기 때문에 필드 이름을 맞추지 않아도 됨 → `member.id`
> 

<br>

### Where절 파라미터 사용

```java
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
```

- 재사용 & 조건 조합 가능

<br>

## 🖥 조회 API 컨트롤러 개발

조회하는 API 개발 

- 샘플 데이터 추가할 때 테스트 케이스에 영향 주지 않기 위해 프로파일 설정
    
    [src/main/resources/application.yml]
    
    ```java
    spring:
    	profiles:
    		active: local
    ```
    
    [샘플 데이터 추가]
    
    ```java
    @Profile("local")
    @Component
    @RequiredArgsConstructor
    public class InitMember {
    
        private final InitMemberService initMemberService;
    
        @PostConstruct
        public void init() {
            initMemberService.init();
        }
    
        @Component
        static class InitMemberService {
    
            @PersistenceContext
            private EntityManager em;
    
            @Transactional
            public void init() {
                Team teamA = new Team("teamA");
                Team teamB = new Team("teamB");
                em.persist(teamA);
                em.persist(teamB);
    
                for (int i = 0; i < 100; i++) {
                    Team selectedTeam = i % 2 == 0 ? teamA : teamB;
                    em.persist(new Member("member"+i, i, selectedTeam));
                }
            }
        }
    
    }
    ```
    
    - `PostConstruct` 가 있는 `init()` 에서 `initMemberService` 의 `init()` 을 호출
        
        > *왜 직접 구현하면 안될까?*
        > 
        > 
        > transactional을 걸어줘야 하는데 PostConstruct에 같이 걸 수 없으므로 따로 메서드 작성 필요
        > 

  <br>
  
- 조회 컨트롤러 작성
    
    ```java
    @RestController
    @RequiredArgsConstructor
    public class MemberController {
    
        private final MemberJpaRepository memberJpaRepository;
    
        @GetMapping("/v1/members")
        public List<MemberTeamDto> searchMemberV1(MemberSearchCondition condition) {
            return memberJpaRepository.search(condition);
        }
    }
    ```
    
    [localhost:8080/v1/members?teamName=teamB&ageGoe=31](http://localhost:8080/v1/members?teamName=teamB&ageGoe=31) 와 같이 테스트!