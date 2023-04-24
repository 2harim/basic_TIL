**목차**
- [🥣 Hello JPA - 프로젝트 생성](#hello-jpa---프로젝트-생성)
  - [H2 데이터베이스 설치와 실행](#h2-데이터베이스-설치와-실행)
  - [maven](#maven)
  - [프로젝트 생성](#프로젝트-생성)
  - [pom.xml](#pomxml)
  - [jpa 설정 - persistence.xml](#jpa-설정---persistencexml)
  - [데이터베이스 방언](#데이터베이스-방언)
- [👩‍💻 Hello JPA - 애플리케이션 개발](#hello-jpa---애플리케이션-개발)
  - [JPA 구동 방식](#jpa-구동-방식)
  - [실습 - JPA 동작 확인](#실습---jpa-동작-확인)
  - [JPQL](#jpql)

<br>

## 🥣 Hello JPA - 프로젝트 생성

### H2 데이터베이스 설치와 실행

http://www.h2database.com/

![Untitled](/img/jpa_basic/section2/h2db.png)

Mac → All Platforms 다운로드 (v1.4.200)

실습용DB로 제일 좋음

- Web Console 환경 가능
- 용량 작고 가벼움
- MySQL, Oracle DB 시뮬레이션 기능
- 시퀀스, AUTO INCREMENT 모두 지원
- DB 띄우지 않고 실행 가능

> `Database "~/test" not found, and IFEXISTS=true, so we cant auto-create it [90146-199]` 와 같은 오류 발생하면
`jdbc:h2~/test` 로 연결하여 DB 파일 생성하며 연결
이후 다시 접속할 때는 **`jdbc:h2:tcp://localhost/~/test` 로 접속**
> 

<br>

### maven

https://maven.apache.org/

자바 라이브러리/빌드 관리

<br>

### 프로젝트 생성

- Java 8 ↑
- maven project
    - groupId : jpa-basic
    - artifactId : ex1-hello-jpa
    - version : 1.0.0

<br>

### pom.xml

`hibernate-entitymanager` : 하이버네이트가 jpa 구현체로 동작하도록 jpa 표준을 구현한 라이브러리

`com.h2database` : h2 database driver, version 다운 받은 것과 맞춰 사용

<br>

### jpa 설정 - persistence.xml

jpa 설정 파일 

경로 : `/META-INF/persistence.xml` 

<br>

**필수 속성**

`javax.persistence.jdbc.driver` : DB 접근 정보

`javax.persistence.jdbc.user` 

`javax.persistence.jdbc.password` 

`javax.persistence.jdbc.url` 

**`hibernate.dialect` : 데이터베이스 방언 지정**

`hibernate.show_sql` : DB에 쿼리 나가는거 보기 설정

`hibernate.format_sql` : format 으로 예쁘게 출력 

`hibernate.use_sql_comments` : 쿼리의 설명, 주석문

> `javax.persistence` → JPA 표준 속성, hibernate 말고 다른 ORM으로 바꿔도 사용할 수 있음
> 
> 
> `hibernate` → hibernate 전용 속성, hibernate에서만 사용 가능
> 

<br>

### 데이터베이스 방언

SQL 표준을 지키지 않은 특정DB만의 고유한 기능, 벤더마다 다른 것

가변문자 : MySQL - `VARCHAR` , Oracle - `VARCHAR2` 

문자열 자르는 함수 : SQL 표준- `SUBSTRING()` , Oracle - `SUBSTR()`

페이징 : MySQL - `LIMIT` , Oracle - `ROWNUM` 

JPA는 특정 DB에 종속 X

![Untitled](/img/jpa_basic/section2/db_dialect.png)

<br>

## 👩‍💻 Hello JPA - 애플리케이션 개발

### JPA 구동 방식

![Untitled](/img/jpa_basic/section2/jpa_system.png)

1. `Persistence` 클래스 사용
2. `Persistence` 가 설정정보(persistence.xml) 조회하여 `EntityManagerFactory` 클래스 생성
3. 필요할 때마다 `EntityManager` 만듦

<br>

### 실습 - JPA 동작 확인

1. `EntityManagerFactory` 생성
    
    ![Untitled](/img/jpa_basic/section2/persistence_unit.png)
    
    ```java
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
    ```
    
    `unitname` : persistence.xml 에 작성한 unit name과 맞춰줌
    
<br>

2. 객체와 테이블 생성 & 매핑
    
    ```sql
    create table Member ( 
    id bigint not null,
    name varchar(255),
    primary key (id)
    );
    ```
    
    ```java
    @Entity
    public class Member {
    
        @Id
        private Long id;
        private String name;
    
    	// ...getter, setter 
    }
    ```
    
    `@Entity` : JPA가 관리할 객체라는 것을 알려주는 애노테이션
    
    `@Id` : DB의 PK 매핑
    
<br>

3. 회원 저장
    
    ```java
    public static void main(String[] args) {
          EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
    
          EntityManager em = emf.createEntityManager();
    
          Member findMember = em.find(Member.class, 1L);
          findMember.setName("HelloJPA");
    
          em.close();
    
          emf.close();
      }
    ```
    
    위와 같이 작성하면 오류 발생! 
    
    **JPA 모든 데이터 변경은 꼭 트랜잭션 안에서 실행**

    <br>
    
    ```java
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
    
        EntityManager em = emf.createEntityManager();
    
        EntityTransaction tx = em.getTransaction();
        tx.begin();
    
        try {
            Member findMember = em.find(Member.class, 1L);
            findMember.setName("HelloJPA");
    
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
    
        emf.close();
    }
    ```
    
    `EntityManagerFactory` : 하나만 생성, 애플리케이션 전체 공유
    
    `EntityManager` : 쓰레드 간 공유 X, 요청 올 때마다 생성하고 사용 후 close
    
<br>

- 조회 : `em.find()`
- 저장 : `em.persist()`
- 삭제 : `em.remove()`
- 수정 : `member.setXxx()`
    
    JPA를 통해 객체를 가져오면 JPA가 관리함 → 변경이 되었는지를 커밋 시점에 확인함 → 수정 단순(update 쿼리 알아서 날림)

<br>   

### JPQL

가장 단순한 조회 방법 → `em.find()` / 객체 그래프 탐색 ( `a.getB().getC()` )

*조건이 들어가게 되면?*

JPA 사용하면 엔티티 객체 중심으로 개발 

검색할 때도 테이블이 아닌 객체를 대상으로 검색해야 하는데 애플리케이션이 필요한 데이터만 DB에서 불러오려면 SQL이 어느정도 필요

```java
List<Member> result = em.createQuery("select m from Member as m", Member.class).getResultList();
```

- JPQL은 엔티티 객체 대상으로 커리
- 객체 지향 쿼리
- 특정 DB SQL에 의존 X