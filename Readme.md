# 정산시스템 프로젝트

## 프로젝트 소개
동영상 플랫폼의 수익 정산을 위한 배치 처리 시스템

비디오 조회수 통계를 생성하고, 이를 기반으로 광고 수익을 계산하여 정산 데이터를 자동으로 생성하는 Batch 작업 프로젝트

## 기술 스택

![Java](https://img.shields.io/badge/Java-21-007396?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.2-6DB33F?style=for-the-badge&logo=spring-boot)
![Spring Batch](https://img.shields.io/badge/Spring_Batch-5.1.0-6DB33F?style=for-the-badge&logo=spring)
![Gradle](https://img.shields.io/badge/Gradle-8.8-02303A?style=for-the-badge&logo=gradle)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-336791?style=for-the-badge&logo=postgresql)
![Redis](https://img.shields.io/badge/Redis-6-D82C20?style=for-the-badge&logo=redis)
![Docker Compose](https://img.shields.io/badge/Docker_Compose-3.8-2496ED?style=for-the-badge&logo=docker)


## 프로세스 플로우
![사용자 인증 플로우](./docs/images/user-auth-flow.png)
![사용자 유형별 기능 플로우](./docs/images/user-type-flow.png)
![일일 배치 처리](./docs/images/daily-batch-flow.png)

## 프로젝트 주요 기능 
1. 판매자 회원가입 & 카카오 로그인
   <table>
     <tr>
       <td><img src="https://github.com/user-attachments/assets/4a388fb5-7830-4c26-99a0-6ac7ce1c3624" width="400"/></td>
       <td><img src="https://github.com/user-attachments/assets/bf6a5c8f-18d1-40d5-84a5-e639af833873" width="400"/></td>
     </tr>
   </table>

2. 비디오 업로드시 광고 자동 할당

3. 비디오 조회수 통계 생성
  
4. 비디오 조회수 및 광고 조회수에 대한 수익 정산

## 프로젝트 경험
### 1. 배치 작업 성능 개선 내용( 추후 작성 예정)

### 📊 최종 성능

N건 기준 실측 결과: --

### 📈 성능 개선 추이

| 단계 | 데이터 규모 | 처리 시간 | 개선율 |
|------|------------|-----------|--------|
| 모놀리식 | N 건 | N분+ | - |
| 멀티 모듈 | N 건 | N분 | --%+ ↓ |
| 1차 최적화 | N 건 | N분 | --% ↓ |
| 2차 최적화 | N 건 | N분 | --% ↓ |
| 3차 최적화 | N 건 | N분 | --% ↓ |

### 2. 트러블 슈팅

## 프로젝트 상세

### API 문서

[Postman API 명세서](https://documenter.getpostman.com/view/37736920/2sAXjQ1VVz)




## ERD
![ERD](./docs/images/settlement-table.png)
