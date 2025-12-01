# Shopping Mall Project

## 1. 프로젝트 개요

본 프로젝트는 Spring Boot를 기반으로 구축된 온라인 쇼핑몰 애플리케이션입니다. 회원 관리, 상품, 주문, 결제, 고객 지원 등 이커머스의 핵심 기능들을 MSA(Microservice Architecture)를 지향하는 모듈형 도메인 구조로 설계하여 각 담당자의 R&R(역할과 책임)에 따라 개발 및 유지보수가 용이하도록 구성되었습니다.

## 2. 주요 기능

- **회원 관리**: 회원가입, 로그인, 정보 조회, 마케팅 수신 동의 변경
- **고객 등급 (RFM)**: RFM 점수 기반의 고객 세분화 및 등급 관리 (배치 처리)
- **상품**: 카테고리별 상품 목록, 상세 조회, 재고 관리
- **장바구니**: 상품 담기, 수량 변경, 항목 삭제
- **주문/결제**: 주문 생성, 결제 연동(PG), 주문 상태 관리(취소/반품)
- **고객 지원**: 1:1 문의 등록 및 답변, FAQ
- **리뷰**: 상품 리뷰 작성, 평점 계산, 댓글 기능
- **프로모션**: 타겟 고객 대상 이메일 마케팅 캠페인
- **공통**: 전역 예외 처리, 공통 응답 포맷, 보안(Spring Security)

## 3. 기술 스택

- **Backend**: Java 21, Spring Boot 4.0.0
- **Database**: MySQL, Spring Data JPA
- **Security**: Spring Security
- **Build Tool**: Gradle
- **Etc**: Lombok

## 4. 프로젝트 구조

```
com.twog.shopping
├── ShoppingApplication.java
│
├── global
│   ├── config (Security, Web, Swagger)
│   ├── error (GlobalExceptionHandler, ErrorCode)
│   └── common (ApiResponse, BaseTimeEntity)
│
└── domain
    ├── support (고객지원, CS, FAQ)
    ├── review (리뷰, 댓글)
    ├── member (회원, 등급)
    ├── analytics (RFM 분석)
    ├── log (행동 로그)
    ├── purchase (주문)
    ├── payment (결제, 정산)
    ├── product (상품)
    ├── cart (장바구니)
    └── promotion (프로모션, 이메일)
```

## 5. 시작하기

### 1) 소스 코드 클론

```bash
git clone [저장소 URL]
```

### 2) 데이터베이스 설정

`src/main/resources/application.properties` 또는 `application.yml` 파일에 본인의 DB 환경에 맞게 datasource 정보를 수정합니다.

```properties
# application.properties 예시
spring.datasource.url=jdbc:mysql://localhost:3306/shoppingmall
spring.datasource.username=root
spring.datasource.password=password
spring.jpa.hibernate.ddl-auto=update
```

### 3) 애플리케이션 실행

Gradle을 사용하여 애플리케이션을 실행합니다.

```bash
./gradlew bootRun
```

또는 사용하시는 IDE(IntelliJ, Eclipse 등)에서 `ShoppingApplication.java` 파일을 직접 실행할 수 있습니다.
