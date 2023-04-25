**목차**
- [👋 엔티티 매핑 소개](#엔티티-매핑-소개)
- [🎯 객체와 테이블 매핑](#객체와-테이블-매핑)
  - [주의사항](#주의사항)
  - [@Entity 속성](#entity-속성)
  - [@Table](#table)
- [🚙 데이터베이스 스키마 자동 생성](#데이터베이스-스키마-자동-생성)
  - [속성](#속성)
  - [주의](#주의)
  - [DDL 생성 기능](#ddl-생성-기능)
- [🎯 필드와 컬럼 매핑](#필드와-컬럼-매핑)
  - [@Column](#column)
  - [@Enumerated](#enumerated)
  - [@Temporal](#temporal)
  - [@Lob](#lob)
  - [@Transient](#transient)
- [🎯 기본 키 매핑](#기본-키-매핑)
  - [직접 할당](#직접-할당)
  - [자동 생성](#자동-생성)
  - [IDENTITY](#identity)
  - [SEQUENCE](#sequence)
  - [TABLE](#table-1)
  - [권장하는 식별자 전략](#권장하는-식별자-전략)
- [🌱 실전 예제 1 - 요구사항 분석과 기본 매핑](#실전-예제-1---요구사항-분석과-기본-매핑)
  - [도메인 모델 분석](#도메인-모델-분석)
  - [테이블 설계](#테이블-설계)
  - [엔티티 설계와 매핑](#엔티티-설계와-매핑)
  - [위와 같은 설계의 문제점](#위와-같은-설계의-문제점)

<br>

## 👋 엔티티 매핑 소개

객체 - 테이블 : `@Entity` , `@Table` 

필드 - 컬럼 : `@Column`

기본 키 : `@Id`

연관관계 : `@ManyToOne` , `@JoinColumn` 

<br>

## 🎯 객체와 테이블 매핑

`@Entity` → JPA가 관리하는 엔티티

JPA 사용해서 테이블과 매핑하는 경우 필수

<br>

### 주의사항

- 기본 생성자 필수 (파라미터 없는 public/protected 생성자)
    
    JPA와 같은 라이브러리들이 reflection 등을 써서 proxing 하는 경우가 있는데 그럴 때 필요
    
- final 클래스, enum, interface, inner 사용 X
- 저장할 필드에 final X

<br>

### @Entity 속성

- `name`
    - 엔티티 이름
    - 기본값 : 클래스 이름 그대로

<br>

### @Table

엔티티와 매핑할 테이블 지정(이름 다르게 할 때)

- `name` : 매핑할 테이블 이름
- `catalog` : DB catalog 매핑
- `schema` : DB schema 매핑
- `uniqueConstraints` : DDL 생성시 유니크 제약 조건

<br>

## 🚙 데이터베이스 스키마 자동 생성

애플리케이션 로딩 시점에 DB 테이블 생성하는 기능(DDL 실행) 지원 (local, dev 환경)

DB 방언을 활용하여 DB에 맞는 DDL 생성

```bash
Hibernate: 
    
    drop table Member if exists
Hibernate: 
    
    create table Member (
       id bigint not null,
        name varchar(255),
        primary key (id)
    )
```

<br>

### 속성

`hibernate.hbm2ddl.auto` 

- **create** : 기존 테이블 삭제 후 다시 생성 (DROP + CREATE)
- **create-drop** : create과 같으나 종료하는 시점에 DROP
- **update** : 변경만 반영 (ALTER ADD 만 가능) → 실제 운영에선 사용 X
- **validate** : 엔티티와 테이블 정상 매핑되었는지만 확인 (변경시 에러 발생)
- **none** : 사용 X

> DB 방언에 따라 달라짐 ex)VARCHAR
> 

<br>

### 주의

🚨 운영 장비에서는 절대 create, create-drop, update 사용 X 🚨 

개발 초기 - create, update

테스트 서버 - update, validate

스테이징, 운영 서버 - validate, none (운영서버는 거의 none)

<br>

### DDL 생성 기능

제약 조건 추가

`@Column` 

- **unique** : unique 지정
- **name** : 컬럼명 지정
- **length** : 길이 제한
- **nullable** : null / not null 조건

> *DDL 자동 생성할 때만 사용되고 JPA 실행 로직에는 영향 X*
> 

<br>

## 🎯 필드와 컬럼 매핑

**[요구사항]**

```markdown
1. 회원은 일반 회원과 관리자로 구분
2. 회원 가입일과 수정일 있어야 함
3. 회원을 설명할 수 있는 필드가 있어야 함. 길이 제한 X
```

```java
@Entity
public class Member {

    @Id
    private Long id;

    @Column(name = "name") //필드명은 username, 컬럼명은 name
    private String username;

    private Integer age; //DB에서 integer랑 가장 적절한 타입 매칭

    @Enumerated(EnumType.STRING) //db에는 보통 enum이 없으므로 STRING으로 저장 위한 애노테이션
    private RoleType roleType;

    @Temporal(TemporalType.TIMESTAMP) //날짜 타입
    private Date createdDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModifiedDate;

    @Lob //DB에 varchar를 넘어서는 큰 값을 넣고 싶을 때
    private String description;

    public Member() {}
}
```

`TemporalType` 

- DATE
- TIME
- TIMESTAMP

java에서 Date는 날짜, 시간 모두 포함

그러나 보통 DB에서는 날짜, 시간, 날짜+시간 으로 구분

<br>

```sql
create table Member (
   id bigint not null,
    age integer,
    createdDate timestamp,
    description clob,
    lastModifiedDate timestamp,
    roleType varchar(255),
    name varchar(255),
    primary key (id)
)
```

| 어노테이션 | 설명 |
| --- | --- |
| @Column | 컬럼 매핑 |
| @Temporal | 날짜 타입 매핑 |
| @Enumerated | enum 타입 매핑 |
| @Lob | BLOB, CLOB 매핑 |
| @Transient  | 매핑 무시, DB랑 관계 없이 메모리에서만 계산하고 싶을 때 |

<br>

### @Column

- `name`
    - 필드와 매핑할 테이블 컬럼명
- `insertable` , `updatable`
    - 등록, 변경 가능 여부 (기본 true)
- `nullable(DDL)`
    - null 값 허용 여부 (기본 true)
    - false → not null 제약 조건
- `unique(DDL)`
    - `@Table의 uniqueConstraints` 같지만 한 컬럼에 간단히 제약조건 걸 때 사용
    - 잘 사용 X → 이름이 랜덤으로 만들어짐
- `columnDefinition`
    - DB 컬럼 정보 직접 작성 → DDL 에 반영
    - default값 , ..
- `length`
    - 문자 길이 제약 조건, String 만 가능 (기본 255)
- `pecision` , `scale(DDL)`
    - BigDecimal/BigInteger 타입에서 사용
    - precision : 소수점 포함 전체 자릿수
    - scale : 소수 자릿수

<br>

### @Enumerated

- `ORDINAL` : enum 순서 저장, 기본값 ⇒ 사용X
    
    > 나중에 값이 추가되는 경우 순서가 바뀔 수 있음, 유지보수 안좋음
    > 
- `STRING` : enum 이름 저장

<br>

### @Temporal

DATE / TIME / TIMESTAMP 중 지정 필요

> Java8 이후 `LocalDate` , `LocalDateTime` 사용한 경우 생략(최신 하이버네이트에서 지원)
>

<br>

### @Lob

필드 타입이 문자면 CLOB, 나머지는 BLOB 매핑

- CLOB : String, char[], java.sql.CLOB
- BLOB : byte[], java.sql.BLOB

<br>

### @Transient

필드 매핑 X → DB에 저장/ 조회 X

주로 메모리 상에서만 임시로 값 보관하고 싶을 때 사용

<br>

## 🎯 기본 키 매핑

### 직접 할당

@Id 만 사용

<br>

### 자동 생성

`@GeneratedValue`

- IDENTITY
    - 기본키 생성을 DB에 위임
- SEQUENCE
    - DB 시퀀스 오브젝트 사용
    - @SequenceGenerator 필요
- TABLE
    - 키 생성 전용 테이블을 사용, 모든 DB에서 가능
    - @TableGenerator 필요
- AUTO
    - 방언에 따라 자동 지정, 기본값

<br>

### IDENTITY

- 기본키 생성을 DB에 위임
- MySQL, PostgreSQL, SQL Server, DB2, .. `AUTO_INCREMENT`
- 1부터 차례로 부여
- JPA는 보통 **commit** 시점에 INSERT SQL 실행
    
    PK을 직접 넣어주는 것이 아니기 때문에 `AUTO_INCREMENT` 는 DB에 INSERT SQL 실행한 이후에 ID를 알 수 있음 ⇒ 따라서 `em.persist()` 시점 즉시 INSERT SQL 실행하고 DB에서 식별자 조회
    
<br>

### SEQUENCE

- sequence : 유일한 값을 순서대로 생성하는 오브젝트
- create sequence 쿼리문 생성, `call next value`
- Oracle, PostgreSQL, DB2, H2, ..
- 숫자 타입이어야 함 → Integer(20억 넘어가면 겹침), **Long**
    
    > 전체 애플리케이션에서는 int, long 영향 없음, 오히려 넘어가는게 더 복잡하고 문제 
    가능하면 Long 선택하자
    > 
- `@SequenceGenerator` 필요
    
    allocationSize = 50(기본값)
    

```java
@Entity
@SequenceGenerator(name = "member_seq_generator", sequenceName = "member_seq")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "member_seq_generator")
    private String id;
		...
}
```

<br>

### TABLE

- 키 생성 전용 테이블을 만들어서 DB 시퀀스 흉내내는 전략
- auto increment, sequence 둘 중 하나 선택할 필요 없이 모든 DB에 적용 가능
- 성능 이슈 존재 (최적화가 안되어있음)
- `@TableGenerator` 필요
    
    allocationSize : 시퀀스 한 번 호출하는 증가하는 수 (성능 최적화에 사용됨)
    
    기본값 50 → 저장할 때마다 `next call` 로 가져오게 되는데 지정한 숫자인 50개를 미리 가져와서 메모리를 통해 PK값 설정
    
    SEQUENCE 전략도 같음
    

```java
@Entity
@TableGenerator(
    name = "MEMBER_SEQ_GENERATOR",
    table = "MY_SEQUENCES",
    pkColumnValue = "MEMBER_SEQ", allocationSize = 1)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "MEMBER_SEQ_GENERATOR")
    private String id;
		...
}
```

```sql
create table MY_SEQUENCES (
   sequence_name varchar(255) not null,
    next_val bigint,
    primary key (sequence_name)
)
insert into MY_SEQUENCES(sequence_name, next_val) values ('MEMBER_SEQ',0)
```

<br>

### 권장하는 식별자 전략

기본 키 제약 조건 : null X, 유일, ***변경 X***

이러한 조건을 만족하는 자연키 찾기 어려움, 비즈니스와 상관없는 대체키 사용 → 주민등록번호 X

**권장 : Long형 + 대체키(uuid, 랜덤값) + 키 생성전략 사용**

<br>

## 🌱 실전 예제 1 - 요구사항 분석과 기본 매핑

**요구사항 분석**

> 회원은 상품 주문
주문 시 여러 종류의 상품 선택 가능
> 

<br>

**기능 목록**

> 회원 - 회원 가입, 조회
상품 - 상품 등록, 수정, 조회
주문 - 상품 주문, 주문내역 조회, 주문 취소
> 

<br>

### 도메인 모델 분석

`회원 - 주문` : 회원은 여러 번 주문 가능 (일대다)

`주문 - 상품` : 주문 할 때 여러 상품 선택 가능, 상품도 여러 번 주문될 수 있음 (다대다) 

![Untitled](/img/jpa_basic/section4/domain.png)

<br>

### 테이블 설계

![Untitled](/img/jpa_basic/section4/table.png)

<br>

### 엔티티 설계와 매핑

![Untitled](/img/jpa_basic/section4/entity.png)

```java
@Entity
public class Member {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "MEMBER_ID")
    private Long id;
		...
}
```

> 인덱스, 길이 조건 등 보통 코드에 적어두는 편 → 개발자가 객체 보고 바로 확인할 수 있도록
> 

> hibernate 나 jpa 사용했을 때는 그대로 컬럼명이 지정되지만
spring boot + jpa 로 했을 때는 기본 설정을 snake case로 알아서 바꿈 (orderDate → order_date)
> 

<br>

### 위와 같은 설계의 문제점

- 객체 설계를 테이블 설계에 맞춘 방식
    - 테이블의 외래키를 객체에 그대로 가져옴
    
    ```java
    Order order = em.find(Order.class, 1L);
    Long memberId = order.getMemberId();
    
    Member member = em.find(Member.class, memberId);
    ```
    
- 객체 그래프 탐색 불가능
- 참조 없으므로 UML도 잘못됨