# 정산 시스템 프로젝트

## 📝  프로젝트 소개
- 동영상 스트리밍 사이트의 수익 정산을 위한 배치 처리 시스템

- 조회수 통계를 생성하고, 이를 기반으로 광고 수익을 계산하여 정산 데이터를 생성하는 Batch 작업 멀티 모듈 아키텍처 프로젝트
- [프로젝트 기록 보드](https://political-tray-5d5.notion.site/dd43f31b68374e18966c16874e39546a?pvs=4)

  
## 🛠 기술 스택

![Java](https://img.shields.io/badge/Java-21-007396?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.2-6DB33F?style=for-the-badge&logo=spring-boot)
![Spring Batch](https://img.shields.io/badge/Spring_Batch-5.1.0-6DB33F?style=for-the-badge&logo=spring)
![Gradle](https://img.shields.io/badge/Gradle-8.8-02303A?style=for-the-badge&logo=gradle)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-336791?style=for-the-badge&logo=postgresql)
![Redis](https://img.shields.io/badge/Redis-6-D82C20?style=for-the-badge&logo=redis)
![Docker Compose](https://img.shields.io/badge/Docker_Compose-3.8-2496ED?style=for-the-badge&logo=docker)




## 💡 기능 
- #### 로그인
  - 회원가입 & 카카오 로그인
  - 판매자와 사용자 구분, 판매자만 영상 업로드 가능
  
- #### 영상
  - 비디오 조회수 통계및 광고 조회수에 대한 수익 정산 배치 작업
  - 사용자 경험 향상을 위한 마지막 재생 위치 저장
  - 영상 전체 & 특정 비디오 조회
  
- #### 광고
  - 비디오 업로드시 광고 자동 할당
  - 스케줄러를 통해 계약일에 맞춰 광고 생애 주기 자동화
  
- #### 통계
  - 일일 & 주간 & 월간 Top  5 조회수별, 재생시간별 조회
  - 배치 작업을 통해 조회수 및 재생시간 통계 자동화 처리
  
- #### 정산
  - 일일 & 주간 & 월간 총 정산금액, 영상별 정산금액, 광고별 정산금액 영상별 조회
  - 조회수 및 광고 조회수에 따른 차등 수익 모델 적용
  <details>
    <summary>정산 계산 공식 및 단가 표</summary>

    - 동영상별 정산금액 = 업로드 영상 정산 금액 + 광고 영상 정산 금액, 1원 단위 이하 절사
    - 업로드 영상 정산 금액 = 영상별 단가 X 조회 수
    - 광고 영상 정산 금액 = 광고별 단가 X 광고 조회 수

    ### 영상별 단가

    | 조회수 10만 미만 | 조회수 10만 이상 50만 미만 | 조회수 50만 이상 100만 미만 | 조회수 100만 이상 |
    |------------------|----------------------------|------------------------------|--------------------|
    | 1원              | 1.1원                      | 1.3원                        | 1.5원              |

    ### 광고별 단가

    | 조회수 10만 미만 | 조회수 10만 이상 50만 미만 | 조회수 50만 이상 100만 미만 | 조회수 100만 이상 |
    |------------------|----------------------------|------------------------------|--------------------|
    | 10원             | 12원                       | 15원                         | 20원               |

  </details>

  

## 💻 프로젝트 경험
### 1. 배치 작업 성능 개선 내용

### 📈 성능 개선 추이

| 단계     | 데이터 규모  | 처리 시간  | 개선율    | 상세                              |
|--------|---------|--------|--------|---------------------------------|
| 기존     | 50,000건 | 60분+   | -      | 모놀리식 아키텍처 및 tasklet 방식 배치 처리    |
| 1차 최적화 | 50,000건     | 40분+   | 33%+ ↓ | 멀티 모듈 아키텍처로 전환, Chunk 기반 처리로 변경 |
| 2차 최적화 | 50,000건     | **3분** | 95% ↓  | 병렬 처리 도입, [최적 청크 사이즈 설정](https://political-tray-5d5.notion.site/105955eb57fc80838dbedb0856bc89fd?pvs=4)          |

### 📊 최종 성능

- 60분 이상 걸리던 처리시간을 3분으로 **95% 단축** 
- 시스템 확장성 및 유지보수성 대폭 개선

| 작업명           | 실행 시간 (초) | 시작 CPU 사용률(%) | 종료 CPU 사용률(%) | 메모리 사용량(MB) |
|------------------|----------------|--------------------|--------------------|-------------------|
| VideoStatsBatchJob | 183.22초 | 258.74% | 954.54% | 99MB |

### 2. 트러블 슈팅
- [대량의 비디오 업로드시 데드락 문제](https://political-tray-5d5.notion.site/10c955eb57fc80cd8533e2f88c683ecd?pvs=4)
- [파티셔닝을 이용한 대량 비디오 처리 개선](https://political-tray-5d5.notion.site/6254cf5dbfb44ec486b8a2a6948ace1e?pvs=4)
- [배치 속도 개선](https://political-tray-5d5.notion.site/0ddeebb90ead42b6acf48a62f7c835e0?pvs=4)


## 🌐 API 문서

[Postman API 명세서](https://documenter.getpostman.com/view/37736920/2sAXjQ1VVz)



## ⏩ 프로세스 플로우
<details>
  <summary>사용자 인증 플로우</summary>

![사용자 인증 플로우](./docs/images/user-auth-flow.png)

</details>

<details>
  <summary>사용자 유형별 기능 플로우</summary>

![사용자 유형별 기능 플로우](./docs/images/user-type-flow.png)

</details>

<details>
  <summary>일일 배치 처리 플로우</summary>

![일일 배치 처리](./docs/images/daily-batch-flow.png)

</details>




## 🗄 ERD
![ERD](./docs/images/settlement-table.png)
