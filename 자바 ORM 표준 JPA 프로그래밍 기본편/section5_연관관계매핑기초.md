**목차**
- [➡️ 단방향 연관관계](#️-단방향-연관관계)
  - [객체를 테이블에 맞추어 모델링](#객체를-테이블에-맞추어-모델링)
  - [단방향 연관관계](#단방향-연관관계)
- [↔️ 양방향 연관관계와 연관관계의 주인 1 - 기본](#️양방향-연관관계와-연관관계의-주인-1---기본)
  - [양방향 매핑](#양방향-매핑)
  - [객체와 테이블 관계 맺는 차이](#객체와-테이블-관계-맺는-차이)
  - [연관관계 주인](#연관관계-주인)
- [⚠️ 양방향 연관관계와 연관관계의 주인 2 - 주의점, 정리](#️양방향-연관관계와-연관관계의-주인-2---주의점-정리)
  - [연관관계 주인에 값을 입력하자](#연관관계-주인에-값을-입력하자)
  - [정리](#정리)
- [☘️ 실전 예제 2 - 연관관계 매핑 시작](#️실전-예제-2---연관관계-매핑-시작)

<br>

## ➡️ 단방향 연관관계

### 객체를 테이블에 맞추어 모델링

![Untitled](/img/jpa_basic/section5/table.png)

```java
@Entity
public class Member {

    @Id
    @GeneratedValue
    @Column(name = "MEMBER_ID")
    private Long id;

    @Column(name = "USERNAME")
    private String username;

    @Column(name = "TEAM_ID")
    private Long teamId;
}
```

```java
@Entity
public class Team {

    @Id
    @GeneratedValue
    @Column(name = "TEAM_ID")
    private Long id;

    private String name;
}
```

```java
Team team = new Team();
team.setName("TeamA");
em.persist(team);

Member member = new Member();
member.setUsername("member1");
member.setTeamId(team.getId());
em.persist(member);

Member findMember = em.find(Member.class, member.getId());

Long teamId = findMember.getTeamId();
Team findTeam = em.find(Team.class, teamId);
```

> *내부적으로 H2 DB가 SEQUENCE 사용
따로 PK값 사용하고 싶다면 매핑 따로 해야 함*
> 

<br>

**협력 관계를 만들 수 없음**

- 테이블은 외래 키로 조인해서 연관된 테이블을 찾음
- 객체는 참조를 사용해서 연관된 객체 찾음

<br>

### 단방향 연관관계

![Untitled](/img/jpa_basic/section5/oneway.png)

```java
@Entity
public class Member {
		...
		@ManyToOne
    @JoinColumn(name = "TEAM_ID")
    private Team team;
}
```

`@ManyToOne` : Member 기준으로 다대일 관계이므로 설정

`@JoinColumn` : join할 컬럼명 지정 `TEAM_ID` 

JPA가 알아서 join해서 가져옴

```java
member.setTeam(team);

Team findTeam = findMember.getTeam();
System.out.println("findTeam = " + findTeam.getName());
```

<br>

## ↔️ 양방향 연관관계와 연관관계의 주인 1 - 기본

### 양방향 매핑

![Untitled](/img/jpa_basic/section5/twoway.png)

테이블 연관관계는 변화 X → FK로 join하면 되므로

> *테이블에서는 방향이 존재하지 않음 사실상 **양방향***
> 

team에서도 member 볼 수 있도록 객체 연관관계 설정

```java
@OneToMany(mappedBy = "team")
private List<Team> members = new ArrayList<>();
```

`mappedBy` : 반대편에 걸려있는 필드명 작성

<br>

### 객체와 테이블 관계 맺는 차이

- 객체 연관관계 2개
    - 회원 → 팀 (단방향) `team`
    - 팀 → 회원 (단방향) `members`
    - 양방향 관계가 아니라 서로 다른 단방향 2개
        
        ```java
        A → B(a.getB())
        
        B → A(b.getA())
        ```
        
- 테이블 연관관계 1개
    - 회원 ↔ 팀 (양방향) `TEAM_ID`
    - 외래키 하나로 두 테이블의 연관관계 관리, 양쪽 조인 가능
    
<br>

### 연관관계 주인

- 양방향 매핑 관계일 때 규칙
    - 객체 두 관계 중 하나를 연관관계 주인으로 지정
    - 주인만이 외래키 관리(등록, 수정)
    - 주인이 아닌 쪽은 읽기만
    - `mappedBy`
        - 주인이면 사용 X
        - 주인이 아니면 해당 속성으로 주인 지정

- 누구를 주인으로 ?
    - **외래키가 있는 곳을 주인으로 정하기** → `다` 쪽
        - 이렇게 하지 않을 때, 변경이 있으면 다른 테이블에 update 쿼리가 발생함
    - `Member.team` 이 연관관계 주인
    - 연관관계 주인은 비즈니스적으로 중요하다는 것을 의미하는 건 아님

<br>

## ⚠️ 양방향 연관관계와 연관관계의 주인 2 - 주의점, 정리

### 연관관계 주인에 값을 입력하자

```java
team.getMembers().add(member); //데이터 제대로 안들어감

member.setTeam(team); //이렇게 연관관계 주인에 데이터 입력
```

team이 갖고 있는 members는 연관관계 주인이 아니기 때문에 이것을 통해 데이터를 넣어주면 실제 DB에 데이터가 제대로 안들어감

둘다 넣어주는건 ok

<br>

- **순수한 객체 관계를 고려한다면 항상 양쪽 다 값을 입력해야 함!!**

연관관계 주인 쪽에 데이터를 넣어주는 로직을 짠 후 `flush` , `clear` 하지 않고 `find` 한다면 **1차캐시**에 존재 → 컬렉션에 값이 존재하지 않음 (반대편 연관관계)

`team` 이 그대로 영속성 컨텍스트에 들어가있기 때문에 컬렉션( `members` )에 값이 존재하지 않게 됨 

테스트케이스 작성할 때는 JPA 없이 해야할 때 반대도 넣어줘야 하는 경우 필요

- 연관관계 편에 메소드 생성하자! ← 둘다 값 넣어주는 !
    
    한 쪽에만 메소드 생성 
    

```java
public void setTeam(Team team) { // -> 보통은 changeTeam과 같이 setter 이름 사용 X
    this.team = team;
    team.getMembers().add(this);
}
```

- 양방향 매핑 시에 무한 루프 조심하자
    
    `toString` , `lombok` , JSON 생성 라이브러리(Controller에는 entity 절대 반환 안하면 문제 X)
    
<br

### 정리

- 단방향 매핑만으로도 이미 연관관계 매핑은 완료
    - 처음 설계 시에는 단방향으로 끝내야 함, 추후 필요할 때 추가해도 됨(테이블에 영향 X)
- 양방햔 매핑은 반대 방향으로의 조회 기능 추가된 것 ⇒ 객체 그래프 탐색
- JPQL에서 역방향으로 탐색할 일 많음

<br>

## ☘️ 실전 예제 2 - 연관관계 매핑 시작

![Untitled](/img/jpa_basic/section5/example.png)