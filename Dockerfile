# 1. AWS의 OpenJDK 21 기반 Alpine 이미지 사용
FROM amazoncorretto:21-alpine

# 2. 작업 디렉토리 생성
WORKDIR /app

# 3. Gradle이 생성하는 JAR 파일 복사
COPY build/libs/*.jar app.jar

# 4. 도커 컨테이너 내에서 실행될 명령어를 정의
ENTRYPOINT ["java", "-jar", "/app/app.jar"]


