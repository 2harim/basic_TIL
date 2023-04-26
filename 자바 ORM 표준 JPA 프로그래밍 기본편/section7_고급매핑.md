**목차**
- [🎯 상속 관계 매핑](#상속-관계-매핑)
  - [슈퍼타입 서브타입 논리모델을 실제 물리 모델로 구현하는 방법](#슈퍼타입-서브타입-논리모델을-실제-물리-모델로-구현하는-방법)
  - [조인 전략](#조인-전략)
  - [**단일 테이블 전략**](#단일-테이블-전략)
  - [**구현 클래스마다 테이블 전략**](#구현-클래스마다-테이블-전략)
- [🦘 Mapped Superclass - 매핑 정보 상속](#mapped-superclass---매핑-정보-상속)
- [🪴 실전 예제 4 - 상속관계 매핑](#실전-예제-4---상속관계-매핑)
  - [도메인 모델](#도메인-모델)
  - [테이블 설계](#테이블-설계)

<br>

## 🎯 상속 관계 매핑

- RDB는 상속관계 X
- **슈퍼타입, 서브타입 관계** 모델링 기법이 객체 상속과 유사

<br>

### 슈퍼타입 서브타입 논리모델을 실제 물리 모델로 구현하는 방법

![Untitled](/img/jpa_basic/section7/supertype_subtype.png)

- 각각 테이블로 변환 ⇒ **조인전략**
- 통합 테이블로 변환 ⇒ **단일 테이블 전략**
- 서브타입 테이블로 변환 ⇒ **구현 클래스마다 테이블 전략**

<br>

### 조인 전략

![Untitled](/img/jpa_basic/section7/join.png)

`@Inheritance(strategy = InheritanceType.*JOINED*)`  

`@DiscriminatorColumn` : DTYPE 컬럼 생성, 컬럼명 설정 가능

`@DiscriminatorValue` : DTYPE 컬럼 값을 간단하게 설정 가능(movie → m)

- 각 서브타입 테이블에서 슈퍼 타입 테이블의 pk를 가지고 있음
- dtype 없어도 괜찮지만 있으면 테이블 보고 어떤 서브타입 테이블에 들어간 데이터인지 빠르게 파악 가능
- 장점
    - 테이블 정규화=
    - 외래키 참조 무결성 제약조건 활용 O → 다른 테이블에서 ITEM 테이블 볼 때 서브타입 테이블 볼 필요 X
    - 저장공간 효율화
- 단점
    - 조회 시 조인 많이 사용 → 성능 저하, 조회 쿼리 복잡
    - 데이터 저장 시 insert 쿼리 2번 발생

```java
Movie movie = new Movie();
movie.setDirector("a");
movie.setActor("bbb");
movie.setName("바람과함께사라지다");
movie.setPrice(10000);

em.persist(movie);

em.flush();
em.clear();

Movie findMovie = em.find(Movie.class, movie.getId());
```

```sql
Hibernate: 
    /* insert hellojpa.Movie
        */ insert 
        into
            Item
            (name, price, DTYPE, id) 
        values
            (?, ?, 'Movie', ?)

Hibernate: 
    /* insert hellojpa.Movie
        */ insert 
        into
            Movie
            (actor, director, id) 
        values
            (?, ?, ?)

Hibernate: 
    select
        movie0_.id as id2_2_0_,
        movie0_1_.name as name3_2_0_,
        movie0_1_.price as price4_2_0_,
        movie0_.actor as actor1_6_0_,
        movie0_.director as director2_6_0_ 
    from
        Movie movie0_ 
    inner join
        Item movie0_1_ 
            on movie0_.id=movie0_1_.id 
    where
        movie0_.id=?
```

<br>

### **단일 테이블 전략**

![Untitled](/img/jpa_basic/section7/singe_table.png)

`@Inheritance(strategy = InheritanceType.*SINGLE_TABLE*)` 

- 서브 타입 클래스에서 사용하는 컬럼을 모두 한 테이블에 넣은 다음 구분하는 컬럼(DTYPE) 사용
    
    여기서는 `@DiscriminatorColumn` 지정안해줘도 자동으로 DTYPE 컬럼 생김 (강제)
    
- 장점
    - 성능이 가장 좋음 → insert query 한 번 발생, select 도 조인 없이 바로 가져옴
- 단점
    - 자식 엔티티가 매핑한 컬럼은 모두 null 허용해야 함
    - 단일 테이블에 모든 것을 저장하므로 테이블이 커짐 → 상황에 따라 조회 성능 저하

```sql
create table Item (
    DTYPE varchar(31) not null,
    id bigint not null,
    name varchar(255),
    price integer not null,
    artist varchar(255),
    author varchar(255),
    isbn varchar(255),
    actor varchar(255),
    director varchar(255),
    primary key (id)
)
```

<br> 

### **구현 클래스마다 테이블 전략**

![Untitled](/img/jpa_basic/section7/table_per_class.png)

`@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)`
- 서브타입 테이블만 생성한 다음 각각 공통 컬럼을 갖고 있음 ⇒ Item 테이블 생성 X
- dtype 의미 X → 각각 테이블이 다르기 때문
- 장점
    - 서브 타입 명확히 구분해서 처리할 때 효과적
    - not null 제약조건 사용 가능
- 단점
    - **사실상 이 방식 사용 X !! DB 설계자와 ORM 전문가 둘다 추천 X**
    - 여러 테이블 함께 조회할 때 성능 느림 - union
    - 자식 테이블 통합해서 쿼리문 작성하기 어려움
    묶어야 시스템 통합이 가능한데 다 따로 있음 → 나중에 추가할 때 안좋음

```sql
Hibernate: 
    /* insert hellojpa.Movie
        */ insert 
        into
            Movie
            (name, price, actor, director, id) 
        values
            (?, ?, ?, ?, ?)
Hibernate: 
    select
        movie0_.id as id1_2_0_,
        movie0_.name as name2_2_0_,
        movie0_.price as price3_2_0_,
        movie0_.actor as actor1_6_0_,
        movie0_.director as director2_6_0_ 
    from
        Movie movie0_ 
    where
        movie0_.id=?
```

부모 타입으로 find할 때 union all 로 다 확인함

```sql
select
    item0_.id as id1_2_0_,
    item0_.name as name2_2_0_,
    item0_.price as price3_2_0_,
    item0_.artist as artist1_0_0_,
    item0_.author as author1_1_0_,
    item0_.isbn as isbn2_1_0_,
    item0_.actor as actor1_6_0_,
    item0_.director as director2_6_0_,
    item0_.clazz_ as clazz_0_ 
from
    ( select
        id,
        name,
        price,
        artist,
        null as author,
        null as isbn,
        null as actor,
        null as director,
        1 as clazz_ 
    from
        Album 
    union
    all select
        id,
        name,
        price,
        null as artist,
        author,
        isbn,
        null as actor,
        null as director,
        2 as clazz_ 
    from
        Book 
    union
    all select
        id,
        name,
        price,
        null as artist,
        null as author,
        null as isbn,
        actor,
        director,
        3 as clazz_ 
    from
        Movie 
) item0_ 
where
item0_.id=?
```

<br>

**[결론]**

기본적으로 조인 테이블 사용하고 테이블이 단순하고 데이터도 많지 않고 확장 가능성도 없다면 단일 테이블 고민 

비즈니스적으로 복잡하면 조인 테이블 !

<br>

## 🦘 Mapped Superclass - 매핑 정보 상속

![Untitled](/img/jpa_basic/section7/mapped_superclass.png)

- 공통 매핑 정보가 필요할 때 사용
    - 객체 입장에서 id, name 필드가 중복으로 나오는 경우 부모 클래스에 두고 상속받아서 사용하고 싶음
- **위의 상속 관계와 다름!!**

```java
@MappedSuperclass
public class BaseEntity { ... }
```

```java
@Entity
public class Member extends BaseEntity { ... }
```

```sql
create table Member (
   MEMBER_ID bigint not null,
    createdBy varchar(255),
    createdDate timestamp,
    lastModifiedBy varchar(255),
    lastModifiedDate timestamp,
    USERNAME varchar(255),
    LOCKER_ID bigint,
    TEAM_ID bigint,
    primary key (MEMBER_ID)
)
```

`BaseTimeEntity` → 엔티티 X, 테이블과 매핑 X, 직접 사용할 일 없으므로 추상클래스 권장

**테이블과 관계 없고 단순히 엔티티가 공통으로 사용하는 매핑 정보 모으는 역할**

<br>

## 🪴 실전 예제 4 - 상속관계 매핑

**[요구사항 추가]**

```markdown
상품 종류는 음반, 도서, 영화 있고 이후 더 확장될 수 있다
모든 데이터는 등록일과 수정일이 필수다
```

<br>

### 도메인 모델

![Untitled](/img/jpa_basic/section4/domain.png)

<br>

### 테이블 설계

예제에서 싱글테이블로 설계

![Untitled](/img/jpa_basic/section4/table.png)