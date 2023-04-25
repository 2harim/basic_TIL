**목차**
- [🤔 연관관계 매핑 시 고려사항](#연관관계-매핑-시-고려사항)
- [✨ 다대일 \[N:1\]](#다대일-n1)
  - [다대일 단방향](#다대일-단방향)
  - [다대일 양방향](#다대일-양방향)
- [🥝 일대다 \[1:N\]](#일대다-1n)
  - [일대다 단방향](#일대다-단방향)
  - [일대다 양방향](#일대다-양방향)
- [🥝 일대일 \[1:1\]](#일대일-11)
  - [주 테이블에 외래키 단방향](#주-테이블에-외래키-단방향)
  - [주 테이블에 외래키 양방향](#주-테이블에-외래키-양방향)
  - [대상 테이블에 외래키 단방향](#대상-테이블에-외래키-단방향)
  - [대상 테이블에 외래키 양방향](#대상-테이블에-외래키-양방향)
  - [정리](#정리)
- [🥝 다대다 \[N:M\]](#다대다-nm)
  - [한계](#한계)
  - [극복](#극복)
- [🍀 실전 예제 3 - 다양한 연관관계 매핑](#실전-예제-3---다양한-연관관계-매핑)
  - [테이블](#테이블)
  - [엔티티](#엔티티)
  - [@JoinColumn](#joincolumn)
  - [@ManyToOne](#manytoone)

<br>

## 🤔 연관관계 매핑 시 고려사항

- **다중성**
    - 다대일 `@ManyToOne`
    - 일대다 `@OneToMany`
    - 일대일 `@OneToOne`
    - 다대다 `@ManyToMany`
- **단방향, 양방향**
    - 테이블
        - 외래키 하나로 양쪽 조인 가능
        - 방향 개념 X
    - 객체
        - 참조용 필드가 있는 쪽으로만 참조 가능
        - 한쪽만 참조하면 `단방향` , 양쪽이 서로 참조하면 `양방향`
- **연관관계 주인**
    - 객체 양방향 관계의 참조 2군데 중 테이블의 외래키를 관리할 곳을 지정해야 함
    - 외래키를 관리하는 참조
    - 주인의 반대편 : 외래키 영향 X, 단순 조회만 가능

> *다대다는 실무에서 사용 X*
> 

<br>

## ✨ 다대일 [N:1]

### 다대일 단방향

![Untitled](/img/jpa_basic/section6/ManyToOne_OW.png)

- 가장 많이 사용
- 반대는 일대다

<br>

### 다대일 양방향

![Untitled](/img/jpa_basic/section6/ManyToOne_TW.png)

- 테이블에 영향 X, 객체만 추가
- 외래키 있는 곳이 연관관계 주인
- 양쪽 서로 참조하도록 개발

<br>

## 🥝 일대다 [1:N]

### 일대다 단방향

![Untitled](/img/jpa_basic/section6/OneToMany_OW.png)

- `일(1)` 쪽이 연관관계 주인이 됨
- 테이블에서는 외래키가 `다(N)` 쪽에 들어가게 되어있음 ⇒ 관계가 틀어짐
    - 객체와 테이블 차이 때문에 반대편 테이블의 외래키 관리하게 됨
- `@JoinComlumn` 꼭 사용해야 함
    - 사용하지 않으면 조인 테이블 방식 사용하게 됨
- 이 모델 권장 X
    - 데이터 저장할 때 쿼리가 추가로 나감 (update)
        - 반대 테이블에 가서 update를 할 수밖에 없음
    - 테이블이 수십개인 상황에서 운영 힘듦

***⇒ 일대다 단방향 매핑보다는 다대일 양방향 매핑 사용하자***

<br>

### 일대다 양방향

![Untitled](/img/jpa_basic/section6/OneToMany_TW.png)

```java
@Entity
public class Member { 
		...
		@ManyToOne
		@JoinColumn(name = "TEAM_ID", insertable = false, updatable = false)
		private Team team;
		...
}
```

연관관계 주인이 둘인 것처럼 되기 때문에  `inserable` , `updatable` 설정하여 무효화시킴 → **읽기 전용**

- 이런 매핑은 공식적으로 존재 X
- `@JoinColumn(insertable=false, updatable=false)`
    - 읽기전용필드를 사용해서 양방향처럼 사용

***다대일 양방향을 사용하자***

<br>

## 🥝 일대일 [1:1]

- 반대도 일대일
- 주 테이블이나 대상 테이블 중 외래키 선택 가능 (아무 곳에 넣어도 상관 없음)
- 외래키에 DB 유니크 제약조건 추가해야함

<br>

### 주 테이블에 외래키 단방향

![Untitled](/img/jpa_basic/section6/OneToOne_Main_OW.png)

- 다대일 단방향 관계와 유사

<br>

### 주 테이블에 외래키 양방향

![Untitled](/img/jpa_basic/section6/OneToOne_Main_TW.png)

- 다대일 양방향과 유사
- 외래키가 있는 곳이 연관관계 주인
- 반대편은 `mappedBy`
  
<br>

### 대상 테이블에 외래키 단방향

![Untitled](/img/jpa_basic/section6/OneToOne_Sub_OW.png)

- JPA 지원 X

<br>

### 대상 테이블에 외래키 양방향

![Untitled](/img/jpa_basic/section6/OneToOne_Sub_TW.png)

<br>

***Member, Locker 어디다 외래키 넣어야할지?***

> 만약 시간이 지나서 한 명의 회원이 여러 개의 라커를 가질 수 있다면,
`Locker`에 외래키 설정시 유니크 제약조건만 빼게 되면 문제 없음, 자연스럽게 일대다로 변환
> 
> 
> 반면 `Member` 에 외래키가 있다면 변경 포인트가 많고 `locker_id` 컬럼을 지워야 함
> 
> 하지만 개발자 입장에서는
> `Member`에 `Locker`가 있는게 성능 면에서 유용
> 
> `Member` 테이블을 조회하는 경우가 많다고 생각했을 때 이미 조회하면 `locker` 까지 가지고 오므로 장점 많음
> 

<br>

### 정리

- **주 테이블에 외래키**
    - 주 객체가 대상 객체의 참조를 가지는 것처럼 주테이블에 외래키 두고 대상 테이블 찾음
    - 객체지향 개발자 선호
    - JPA 매핑 편리
    - 주테이블만 조회해도 대상 테이블에 데이터가 있는지 확인 가능
    - 값이 없으면 외래키에 null 허용
- **대상 테이블에 외래키**
    - 전통적인 DB 개발자 선호
    - 주 테이블과 대상 테이블을 일대일 → 일대다 변경 시 테이블 구조 유지
    - 반대편의 데이터를 넣을 수 없으므로 양방향으로 설계해야 함
    - **프록시 기능 한계로 지연 로딩으로 설정해도 항상 즉시 로딩됨**

<br>

## 🥝 다대다 [N:M]

- 실무에서 잘 사용 X
- 관계형 DB는 정규화된 테이블 2개로 다대다 관계 표현 X
- **연결테이블**을 추가해서 일대다, 다대일 관계로 풀어내야 함
- 객체는 컬렉션 사용해서 객체 2개로 다대다 관계 가능 (서로 컬렉션으로 참조)

```java
@ManyToMany
@JoinTable(name = "MEMBER_PRODUCT")
private List<Product> products = new ArrayList<>();
```

- `@ManyToMany`
- `@JoinTable` : 연결 테이블 지정

<br>

### 한계

- 연결 테이블이 단순히 연결만 하고 끝나지 않음
    - 주문시간, 수량 같은 데이터가 들어올 수 있음 but 추가 정보를 넣을 수 X
- 쿼리가 예상하지 못하는 방식으로 만들어지는 경우가 많음

<br>

### 극복

**연결 테이블용 엔티티 추가(연결테이블을 엔티티로 승격)**

![Untitled](/img/jpa_basic/section6/ManyToMany.png)

> 테이블 PK 값 복합키로 하는 것보다는 비즈니스와 관련 없는 GeneratedValue로 하는 것이 유연성 측면에서 좋음
> 

<br>

## 🍀 실전 예제 3 - 다양한 연관관계 매핑

### 테이블

- 주문 - 배송 : `@OneToOne`
- 상품 - 카테고리 : `@ManyToMany`

![Untitled](/img/jpa_basic/section4/table.png)

<br>

### 엔티티

![Untitled](/img/jpa_basic/section4/entity.png)

<br>

### @JoinColumn

외래키 매핑할 때 사용

> 외래키 제약조건을 매핑하는 것이 아니라 외래키를 매핑하는 것
> 
- `name` : 매핑할 외래키 이름
- `referencedColumnName` : 외래키가 참조하는 대상 테이블의 컬럼명
- `foreignKey(DDL)` : 외래키 제약조건 직접 지정, 테이블 생성할 때만 가능
- `unique` , `nullable` , `insertable` , `updatable` , `columnDefinition`

<br>

### @ManyToOne

다대일 관계 매핑

- `optional` : 기본값 true, false인 경우 연관된 엔티티 항상 있어야 함
- `fetch` : global fetch 전략, EAGER/LAZY
- `cascade` : 영속성 전이 기능 사용
- `targetEntity` : 연관된 엔티티 타입 정소 설정, 요즘 제네릭 덕분에 사용 X

> 다대일은 `mappedBy` 가 없음 → 무조건 연관관계 주인이어야 함
>