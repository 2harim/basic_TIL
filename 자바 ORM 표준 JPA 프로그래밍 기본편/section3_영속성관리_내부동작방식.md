**목차**

- [📦 영속성 컨텍스트](#영속성-컨텍스트)
  - [영속성 컨텍스트 ✨](#영속성-컨텍스트-)
  - [엔티티 생명주기](#엔티티-생명주기)
  - [장점](#장점)
- [📀 플러시](#플러시)
  - [플러시 발생](#플러시-발생)
  - [영속성 컨텍스트를 플러시하는 방법](#영속성-컨텍스트를-플러시하는-방법)
  - [플러시 모드 옵션](#플러시-모드-옵션)
- [🚿 준영속 상태](#준영속-상태)
  - [준영속 상태로 만드는 방법](#준영속-상태로-만드는-방법)


<br>

## 📦 영속성 컨텍스트

엔티티 매니저 팩토리와 엔티티 매니저

![Untitled](/img/jpa_basic/section3/emf_em.png)

요청이 들어오면 `EntityManagerFactory` 가 `EntityManager` 를 생성함 

`EntityManager` 는 내부적으로 DB 커넥션을 사용해서 접근

<br>

### 영속성 컨텍스트 ✨

엔티티를 영구 저장하는 환경

`EntityManager.persist(entity);` 

> ✔︎ DB에 저장하는게 아니라 영속성 컨텍스트를 통해 엔티티를 영속화 하는 것, 영속성 컨텍스트에 저장!
> 

논리적인 개념

엔티티 매니저를 통해 영속성 컨텍스트에 접근

<br>

**J2SE 환경**

엔티티 매니저와 영속성 컨텍스트 1:1 매핑

눈에는 보이지 않는 영속성 컨텍스트 장소가 마련됨

![Untitled](/img/jpa_basic/section3/em_persistenceContext.png)

**J2EE, 스프링 프레임워크같은 컨테이너 환경**

엔티티 매니저와 영속성 컨텍스트 N:1 매핑

![Untitled](/img/jpa_basic/section3/container_em_persistenceContext.png)

<br>

### 엔티티 생명주기

- 비영속(new/transient)
    
    ![Untitled](/img/jpa_basic/section3/transient.png)
    
    ```java
    Member member = new Member();
    member.setId(1L);
    member.setUserName("memberA");
    ```
    
    최초의 멤버 객체 생성한 상태, 영속성 컨텍스트와 전혀 관계 없는 새로운 상태
    
    JPA와 관계없이 객체 생성하기만 한 것
    
- 영속(managed)
    
    ![Untitled](/img/jpa_basic/section3/managed.png)
    
    ```java
    EntityManger em = emf.createEntityManager();
    em.getTransaction().begin();
    
    em.persist(member);
    ```
    
    영속성 컨텍스트에 관리되는 상태
    
    객체를 저장한 상태(영속 상태)
    
    아직 저장하는 쿼리문 X → 실제로는 커밋하는 시점에 영속성 컨텍스트에 있는 데이터가 DB 쿼리로 날라감
    
- 준영속(detached)
    
    ```java
    em.detach(member);
    ```
    
    영속성 컨텍스트에 저장되었다가 분리된 상태
    
    즉 영속성 컨텍스트에서 지워버림
    

- 삭제(removed)
    
    ```java
    em.remove(member);
    ```
    
    삭제된 상태
    
    실제 DB 데이터 삭제 요청
    

### 장점

1. **1차 캐시**
    
    ![Untitled](/img/jpa_basic/section3/first_cache.png)
    
    `em.persist(member)` 했을 때 pk값을 @Id로 하는 memer 객체를 영속성 컨텍스트 내 ***1차 캐시*** 에 저장
    
    <br>

    ![Untitled](/img/jpa_basic/section3/first_cache_find1.png)
    
    `Member member1 = em.find(Member.class, “member1”);` 
    
    조회 시도했을 때 먼저 영속성 컨텍스트의 1차 캐시에서 데이터를 조회 후 반환
    
    <br>

    ![Untitled](/img/jpa_basic/section3/first_cache_find2.png)
    
    `Member member2 = em.find(Member.class, “member2”);` 
    
    1차캐시에 없음 → DB 조회해서 가져온 `member2` 를 1차 캐시에 저장 후 반환 
    
    *똑같은 entity를 동시에 2번 조회하면 처음에는 select query가 실행되지만 두 번째 조회 때는 1차 캐시에서 가져오므로 전체 select 문은 1번 발생*

<br>
    
2. **영속 엔티티의 동일성 보장**
    
    ```java
    Member a = em.find(Member.class, "member1");
    Member b = em.find(Member.class, "member1");
    System.out.println(a == b); //동일성 비교 true
    ```
    
    자바 컬렉션에서 꺼내왔을 때 비교하면 같은 것처럼, 1차 캐시에서 꺼내기 때문에 동일성 보장해줌
    
    1차 캐시로 반복 가능한 읽기(REPEATABLE READ) 등급의 트랜잭션 격리 수준을 DB가 아닌 애플리케이션 차원에서 제공
    
<br>

3. **엔티티 등록시 트랜잭션 지원하는 쓰기 지연**
    
    ```java
    em.persist(memberA);
    em.persist(memberB);
    // 이때까지 INSERT SQL 보내지 X
    
    // commit 순간 INSERT SQL 보냄
    transaction.commit();
    ```
    
    ![Untitled](/img/jpa_basic/section3/lazy.png)
    
    `em.persist(memberA);` 
    
    `memberA` 가 1차 캐시에 저장되고 insert query를 생성해서 쓰기 지연 SQL 저장소에 저장
    
    <br>

    ![Untitled](/img/jpa_basic/section3/lazyB.png)
    
    `em.persist(memberB);` 
    
    `memberB` 가 1차 캐시에 저장되고 insert query를 생성해서 쓰기 지연 SQL 저장소에 저장
    
    <br>

    ![Untitled](/img/jpa_basic/section3/lazy_commit.png)
    
    `transaction.commit();` 
    
    쓰기 지연 SQL 저장소에 있는 쿼리문이 **flush** 됨(DB에 쿼리문을 날림)
    
    실제 DB의 트랜잭션 commit됨
    
    <br>

    **버퍼링**
    
    member1, member2 가 쌓임
    
    DB에 한 번에 보낼 수 있음 → `batchsize` 
    
    사이즈만큼 모아서 한 번에 쿼리 보내고 DB 커밋

<br>

4. **엔티티 수정시 변경 감지 (Dirty Checking)**
    
    ```java
    Member member = em.find(Member.class, 150L);
    member.setName("zzzz");
    ```
    
    찾아서 값만 변경했는데 알아서 update 쿼리 나감
    
    ```sql
    update
        Member 
    set
        name=? 
    where
        id=?
    ```
    
    JPA에서는 Dirty Checking 즉 변경 감지 기능으로 엔티티 변경하도록 함
    
    ![Untitled](/img/jpa_basic/section3/dirtychecking.png)
    
    commit하는 시점에,
    
    `flush()` 호출됨 → 엔티티와 스냅샷 비교 (1차 캐시 안) → 변경 감지하면 UPDATE SQL 생성 in `쓰기 지연 SQL 저장소` → flush → commit

<br>

5. 엔티티 삭제
    
    ```sql
    Member member = em.find(Member.class, 150L);
    em.remove(member); //엔티티 삭제
    ```
    
    커밋 시점에 DELETE 쿼리문 발생
    
<br>

## 📀 플러시

영속성 컨텍스트의 변경 내용을 DB에 반영

<br>

### 플러시 발생

**DB 트랜잭션 commit 발생 시** 플러시 발생

1. 변경 감지
2. 수정된 엔티티 쓰지 지연 SQL 저장소에 등록
3. 쓰기 지연 SQL 저장소의 쿼리를 DB에 전송

<br>

### 영속성 컨텍스트를 플러시하는 방법

- `em.flush()` - 직접 호출
    
    강제하고 싶거나 미리 쿼리 보고 싶은 경우 em.flush() 사용  
    
- `트랜잭션 커밋` - 플러시 자동 호출
- `JPQL 쿼리 실행` - 플러시 자동 호출
    
    ```java
    em.persist(memberA);
    em.persist(memberA);
    em.persist(memberA);
    
    List<Member> members = em.createQuery("select m from Member m", Member.class).getResultList();
    ```
    
    아직 DB에 저장되지 않았기 때문에( `em.persist()` ) 쿼리문을 날려도 조회되지 않음 ⇒ JPQL 쿼리 실행할 때 무조건 플러시되도록 설정되어 있음
    

> 플러시한다고 1차 캐시(+영속성 컨텍스트)가 지워지지 않음
> 

<br>

### 플러시 모드 옵션

`FlushModeType.AUTO` : 커밋이나 쿼리 실행할 때 플러시 (기본값)

`FlushModeType.COMMIT` : 커밋할 때만 플러시 ex) 전혀 다른 테이블을 조회할 때 

대부분 바꾸지 않고 사용

<br>

플러시란 영속성 컨텍스트의 변경 내용을 DB에 동기화하는 것

트랜잭션 단위 덕분에 가능, 커밋 직전에만 동기화하면 되기 때문

<br>

## 🚿 준영속 상태

영속 상태가 되는 경우

- `em.persist()`
- `em.find()` → 영속성 컨텍스트에 없어서 DB에서 조회해서 1차 캐시에 올려지는 경우

<br>

**준영속 상태** : ***영속 상태의 엔티티가 영속성 컨텍스트에서 분리된 것*** detached

→ 영속성 컨텍스트가 제공하는 기능 X

<br>

### 준영속 상태로 만드는 방법

- `em.detach(entity)`
    
    특정 엔티티만 준영속 상태로 전환
    
    ```java
    Member member = em.find(Member.class, 150L); 
    member.setName("AAAAA"); //원래 name zzzz
    
    em.detach(member); //영속성에서 분리 -> 준영속 상태, 영속성 반영X 이름 안바뀜
    
    tx.commit();
    ```
    
- `em.clear()`
    
    영속성 컨텍스트 완전히 초기화
    
- `em.close()`
    
    영속성 컨텍스트 종료