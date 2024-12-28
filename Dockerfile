## 빌드 단계
#FROM openjdk:17-jdk-slim AS builder
#
#
## 작업 디렉토리를 설정합니다.
#WORKDIR /workspace/app
#
## 프로젝트의 모든 파일을 Docker 이미지 내부로 복사합니다.
#COPY . .
#
## Gradle을 사용하여 프로젝트를 빌드합니다.
#RUN ./gradlew clean build -x test
#
## 런타임 이미지
#FROM openjdk:17-jdk-slim
#
## 8443 포트를 외부로 노출합니다.
#EXPOSE 8443
#
## 빌드된 JAR 파일을 런타임 이미지로 복사합니다.
#COPY --from=builder /workspace/app/build/libs/*.jar app.jar
#
## Spring Boot 애플리케이션을 실행합니다.
#ENTRYPOINT ["java", "-jar", "/app.jar"]

### github action version ###
# 빌드 단계
FROM openjdk:17-jdk-slim AS builder

# 작업 디렉토리를 설정합니다.
WORKDIR /workspace/app

# Gradle Wrapper 및 프로젝트의 모든 파일을 Docker 이미지 내부로 복사합니다.
COPY gradlew .
COPY gradle/wrapper/ gradle/wrapper/
COPY . .

# Gradle 실행 권한 부여
RUN chmod +x gradlew

# Gradle을 사용하여 프로젝트를 빌드합니다.
RUN ./gradlew clean build -x test

# 런타임 이미지
FROM openjdk:17-jdk-slim

# 8443 포트를 외부로 노출합니다.
EXPOSE 8443

# 빌드된 JAR 파일을 런타임 이미지로 복사합니다.
COPY --from=builder /workspace/app/build/libs/*.jar app.jar

# Spring Boot 애플리케이션을 실행합니다.
ENTRYPOINT ["java", "-jar", "/app.jar"]
