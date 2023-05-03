**목차**

- [🌵 프로젝트 생성](#프로젝트-생성)
- [✔️ Querydsl 설정과 검증](#️querydsl-설정과-검증)
- [📚 라이브러리 살펴보기](#라이브러리-살펴보기)
  - [Querydsl](#querydsl)
  - [Spring boot](#spring-boot)
  - [테스트](#테스트)
- [🪐 스프링 부트 설정 - JPA, DB](#스프링-부트-설정---jpa-db)

<br>

## 🌵 프로젝트 생성

- Gradle - Groovy
- Java 11
- Spring Boot 2.7.11
- Group : study
- Artifact : querydsl
- dependencies
    - Spring Web
    - Spring Data JPA
    - H2 Database
    - Lombok

<br>

## ✔️ Querydsl 설정과 검증

Spring Boot 2.6 이상 설정

**[build.gradle]**

```java
buildscript {
	ext {
		queryDslVersion = "5.0.0"
	}
}
plugins {
	id 'java'
	id 'org.springframework.boot' version '2.7.11'
	id 'io.spring.dependency-management' version '1.0.15.RELEASE'
	//queryDSL
	id "com.ewerk.gradle.plugins.querydsl" version "1.0.10"
}
...
dependencies {
	...
	//querydsl 추가
	implementation "com.querydsl:querydsl-jpa:${queryDslVersion}"
	annotationProcessor "com.querydsl:querydsl-apt:${queryDslVersion}"
	...
}

...

//querydsl 추가 시작
def querydslDir = "$buildDir/generated/querydsl"
querydsl {
	jpa = true
	querydslSourcesDir = querydslDir
}
sourceSets {
	main.java.srcDir querydslDir
}
configurations {
	querydsl.extendsFrom compileClasspath
}
compileQuerydsl {
	options.annotationProcessorPath = configurations.querydsl
}
//querydsl 추가 끝
```

`Gradle → Tasks → other → compileQuerydsl` 

Q타입 생성 확인 : build/generated/querydsl 내 Q타입의 엔티티 생성 확인

<br>

## 📚 라이브러리 살펴보기

### Querydsl

[http://querydsl.com](http://querydsl.com)

- querydsl-apt : 관련 코드(Q 타입 ..) 생성
- querydsl-jpa : queryDSL 코드 작성 시 필요한 기능 제공

<br>

### Spring boot

- spring-boot-starter-web : 내장 톰캣, 스프링 웹 MVC
- spring-boot-starter-data-jpa
    - hibernate
    - spring-data-jpa
- spring-boot-starter-jdbc
- spring-boot-starter
    - logging : slf4j 구현체 사용
    
<br>

### 테스트

- spring-boot-starter-test
    - junit : 테스트 프레임워크
    - mockito : 목 라이브러리
    - assertj

<br>

## 🪐 스프링 부트 설정 - JPA, DB

**[application.yml]**
```java
spring:
  datasource:
    url: jdbc:h2:tcp://localhost/~/querydsl 
    username: sa
    password:
    driver-class-name: org.h2.Driver

  jpa:
    hibernate:
      ddl-auto: create # 애플리케이션 실행 시점에 테이블 drop 후 생성
    properties:
      hibernate:
#        show_sql: true # System.out에 hibernate 실행 SQL 남김
        format_sql: true

logging.level:
    org.hibernate.SQL: debug # logger 통해 hibernate 실행 SQL 남김
    org.hibernate.type: trace # ? 로 남아있는 파라미터 값 바인딩
```