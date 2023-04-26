**목차**
- [✔︎ **JPA의 데이터 타입 분류**](#︎-jpa의-데이터-타입-분류)
- [🍰 기본값 타입](#기본값-타입)
- [🎂 임베디드 타입 (복합값 타입)](#임베디드-타입-복합값-타입)
  - [예제](#예제)
  - [사용법](#사용법)
  - [장점](#장점)
  - [테이블 매핑](#테이블-매핑)
  - [임베디드 타입과 연관관계](#임베디드-타입과-연관관계)
- [🚫 값 타입과 불변 객체](#값-타입과-불변-객체)
  - [값 타입 공유 참조](#값-타입-공유-참조)
  - [객체 타입의 한계](#객체-타입의-한계)
  - [불변 객체](#불변-객체)
- [⚖️ 값 타입의 비교](#️값-타입의-비교)
- [🗞 값 타입 컬렉션](#값-타입-컬렉션)
  - [값 타입 컬렉션 사용](#값-타입-컬렉션-사용)
  - [값 타입 컬렉션 제약사항](#값-타입-컬렉션-제약사항)
- [🌴 실전 예제 6 - 값 타입 매핑](#실전-예제-6---값-타입-매핑)

<br>

## ✔︎ **JPA의 데이터 타입 분류**

- **엔티티 타입**
    - @Entity 정의하는 객체
    - 데이터 변해도 식별자로 지속 추적 가능 → 회원 엔티티의 속성을 변경해도 식별자로 인식 가능
- **값 타입**
    - int, Integer, String 처럼 단순히 값으로 사용하는 자바 기본 타입이나 객체
    - 식별자 없고 값만 있음 → 변경 시 추적 불가
    - 분류
        - 기본값 타입 : 자바 원시 타입, 래퍼 클래스, String
        - 임베디드 타입
        - 컬렉션 값 타입

<br>

## 🍰 기본값 타입

- 생명주기를 엔티티에 의존
    
    ex) 회원 삭제하면 나머지 필드 함께 삭제됨
    
- 값 타입 공유 X
    
    ex) 회원 이름 변경 시 다른 회원 이름도 함께 변경되면 X
    

> *원시 타입은 절대 공유 X → 항상 값을 복사함!*
> 
> 
> *래퍼 클래스나 String 같은 특수 클래스는 공유 가능한 객체이지만 변경 X*
> 

<br>

## 🎂 임베디드 타입 (복합값 타입)

- 새로운 값 타입 직접 정의 가능
- 주로 기본값 타입 모아서 만들어서 복합값 타입이라고도 함
- int, String 처럼 `값 타입` 임!

<br>

### 예제

회원 엔티티는 `이름, 근무 시작일, 근무 종료일, 주소 도시, 주소 번지, 주소 우편번호` 가짐

→ 공통 속성들 보임

⇒ 회원 엔티티는 `이름, 근무기간, 집 주소` 가짐

![Untitled](/img/jpa_basic/section9/value_type.png)

<br>

### 사용법

`@Embeddable` : 값 타입 정의하는 곳에 

`@Embedded` : 값 타입 사용하는 곳에

기본생성자 필수

<br>

### 장점

- 재사용성
- 높은 응집도 → 해당 값 타입만 사용하는 의미있는 메서드 만들 수 있음
- 임베디드 타입 포함한 모든 값 타입은 값 타입을 소유한 엔티티에 생명주기 의존

<br>

### 테이블 매핑

![Untitled](/img/jpa_basic/section9/value_type_table.png)

```java
Member member = new Member();
member.setUsername("name");
member.setHomeAddress(new Address("city", "street", "10000"));
member.setWorkPeriod(new Period());
em.persist(member);
```

```sql
/* insert hellojpa.Member
    */ insert 
    into
        Member
        (city, street, zipcode, USERNAME, endDate, startDate, MEMBER_ID) 
    values
        (?, ?, ?, ?, ?, ?, ?)
```

임베디드 타입은 엔티티 값일 뿐 → 매핑 테이블 변화 X

객체와 테이블을 아주 세밀하게 매핑하는 것이 가능 ⇒ 임베디드 타입만의 메서드 사용 가능, 모델링 깔끔

임베디드 타입 자체가 null이면 매핑한 컬럼 모두 null

<br>

### 임베디드 타입과 연관관계

임베디드 타입이 엔티티 객체를 가지고 있을 수 있음

`@AttributeOverride` 속성 재정의 → homeAddress, workAddress 컬럼

<br>

## 🚫 값 타입과 불변 객체

값 타입 : 복잡한 객체 세상 조금이라도 단순화하려고 만든 개념, **단순하고 안전하게** 다룰 수 있어야 함

<br>

### 값 타입 공유 참조

임베디드 타입 같은 값 타입을 여러 엔티티에서 공유하면 위험 → 부작용 발생

![Untitled](/img/jpa_basic/section9/value_type_share.png)

```java
Address address = new Address("city", "street", "10000");

Member member1 = new Member();
member1.setUsername("member1");
member1.setHomeAddress(address);
em.persist(member1);

Member member2 = new Member();
member2.setUsername("member2");
member2.setHomeAddress(address);
em.persist(member2);

member1.getHomeAddress().setCity("newCity"); // 두 번의 update 초래
```


⇒ 값 타입 복사해서 사용

![Untitled](/img/jpa_basic/section9/value_type_copy.png)

```java
Address copyAddress = new Address(address.getCity(), address.getStreet(), address.getZipcode());
Member member2 = new Member();
member2.setUsername("member2");
member2.setHomeAddress(copyAddress);
em.persist(member2);
```

<br>

### 객체 타입의 한계

항상 값을 복사해서 사용하면 공유 참조로 인해 발생하는 부작용 피할 수 있음

문제는 임베디드 타입처럼 직접 정의한 값 타입은 자바의 기본 타입이 아니라 **객체 타입**

객체 타입은 참조 값을 직접 대입 → 막을 수 없음, 객체의 공유 참조는 피할 수 없음

<br>

### 불변 객체

객체 타입 수정할 수 없게 부작용 원천 차단 → **값 타입은 불변 객체로 설계** (생성 이후 변경 X)

- 생성자로만 값 설정하고 수정자 만들지 않으면 됨 또는 private → ex) Integer, String

**값을 바꾸고 싶을때는 어떻게??**

새롭게 객체 만들고 할당해야함

```java
Address newAddress = new Address(address.getCity(), address.getStreet(), "11111");
member1.setHomeAddress(newAddress);
```

<br>

## ⚖️ 값 타입의 비교

인스턴스가 달라도 그 안에 값이 같으면 같은 것으로 봐야 함

```java
int a = 10;
int b = 10;
print(a==b); //true

Address address1 = new Address("city", "street", "10000");
Address address2 = new Address("city", "street", "10000");
System.out.println("address1 == address2 " + (address1==address2)); //false
```

동일성 비교 identity : 참조 값 비교, `==`  

동등성 비교 equivalence : 값 비교, `equals()` 

⇒ 값 타입은 동등성 비교! `equals` 재정의 (→ `hashCode` 도 재정의 필요 )

<br>

## 🗞 값 타입 컬렉션

![Untitled](/img/jpa_basic/section9/value_type_collection.png)

값 타입을 컬렉션에 담아서 사용

DB에는 컬렉션 개념이 없기 때문에 ***별도의 테이블 생성***

```java
public class Member { 
		...		
		@ElementCollection
    @CollectionTable(name = "FAVORITE_FOOD", joinColumns =
    @JoinColumn(name = "MEMBER_ID"))
    @Column(name = "FOOD_NAME")
    private Set<String> favoriteFoods = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "ADDRESS", joinColumns =
    @JoinColumn(name = "MEMBER_ID"))
    private List<Address> addressHistory = new ArrayList<>();
		...
}
```

`@ElementCollection` : 값 타입 컬렉션으로 사용할 때 필요

`@Collectiontable` : table 이름, join column 등 지정

```sql
Hibernate: 
    
    create table FAVORITE_FOOD (
       MEMBER_ID bigint not null,
        FOOD_NAME varchar(255)
    )
Hibernate: 
    
    create table Member (
       MEMBER_ID bigint not null,
        city varchar(255),
        street varchar(255),
        zipcode varchar(255),
        USERNAME varchar(255),
        endDate timestamp,
        startDate timestamp,
        primary key (MEMBER_ID)
    )
Hibernate: 
    
    create table ADDRESS (
       MEMBER_ID bigint not null,
        city varchar(255),
        street varchar(255),
        zipcode varchar(255)
		)
```

<br>

### 값 타입 컬렉션 사용

- 저장
    
    ```java
    Member member1 = new Member();
    member1.setUsername("member1");
    member1.setHomeAddress(new Address("homeCity", "street", "10000"));
    
    member1.getFavoriteFoods().add("치킨");
    member1.getFavoriteFoods().add("족발");
    member1.getFavoriteFoods().add("피자");
    
    member1.getAddressHistory().add(new Address("old1", "street", "10000"));
    member1.getAddressHistory().add(new Address("old2", "street", "10000"));
    
    em.persist(member1);
    ```
    
    발생하는 쿼리문
    
    - insert member
    - insert address * 2 (← addressHistory )
    - insert favoriteFood * 3
    - insert address

- 조회 → 지연 로딩 전략
- 수정
    
    ```java
    //homeCity -> newCity
    //findMember.getHomeAddress().setCity() -> 이렇게 하면 안 됨!
    Address a = findMember.getHomeAddress();
    findMember.setHomeAddress(new Address("newCity", a.getStreet(), a.getZipcode()));
    
    //치킨 -> 한식
    findMember.getFavoriteFoods().remove("치킨");
    findMember.getFavoriteFoods().add("한식");
    
    //equals 사용해서 제거
    findMember.getAddressHistory().remove(new Address("old1", "street", "10000"));
    findMember.getAddressHistory().add(new Address("newCity1", "street", "10000"));
    ```
    
    컬렉션 값만 변경해도 알아서 delete, insert 쿼리 발생 → 마치 영속성 전이
    
    `AddressHistory` 수정할 때는 멤버와 관련된 모든 값을 다 delete 한 후 다시 다 insert
    

> **값 타입 컬렉션은 영속성 전이(cascade) + 고아 객체 제거 기능 필수로 가짐**
> 

<br>

### 값 타입 컬렉션 제약사항

엔티티와 다르게 식별자 개념이 없음 → 변경하면 추적 어려움

따라서 값 타입 컬렉션 변경 사항 발생하면 주인 엔티티와 연관된 모든 데이터 삭제, 값 타입 컬렉션에 있는 현재 값 모두 저장

값 타입 컬렉션 매핑 테이블은 모든 컬럼 묶어서 기본키 구성(null X, 중복 X)

> *실무에서는 값 타입 컬렉션 대신 **일대다 관계** 고려*
> 
> 
> 영속성 전이 + 고아 객체 제거 사용해서 값 타입 컬렉션처럼 사용
> 

<br>

값 타입 컬렉션 **언제 사용?**

select box (multi check) 이런식으로 단순 문자열과 같이 추적할 필요가 없고 update 할 필요 없을 때 

<br>

## 🌴 실전 예제 6 - 값 타입 매핑

값 타입으로 쓸만한 것 : `Address` 

```java
@Embeddable
public class Address {
		private String city;
    private String street;
    private String zipcode;

		...

		@Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Address address = (Address) o;
        return Objects.equals(getCity(), address.getCity()) && Objects.equals(getStreet(), address.getStreet()) && Objects.equals(getZipcode(), address.getZipcode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCity(), getStreet(), getZipcode());
    }
}
```

> `equals` 만들 때 `getter` 이용해야 함 아니면 proxy일 때 접근 안됨
>