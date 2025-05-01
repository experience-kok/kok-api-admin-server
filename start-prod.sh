#!/bin/bash

# 프로덕션 환경에서 스프링 부트 애플리케이션 실행 스크립트

# 서비스 이름
SERVICE_NAME="adminservice"
# JAR 파일 경로
JAR_PATH="./build/libs/$SERVICE_NAME-0.0.1-SNAPSHOT.jar"

# 프로필 설정
PROFILE="prod"

# 메모리 설정
JAVA_OPTS="-Xms512m -Xmx1024m"

# JWT 시크릿 키 (실제 배포 시 보안을 위해 환경 변수 또는 외부 설정 사용 권장)
export JWT_SECRET="your_strong_production_secret_key_here"

# 애플리케이션 시작
echo "Starting $SERVICE_NAME with profile: $PROFILE"
java $JAVA_OPTS -jar $JAR_PATH --spring.profiles.active=$PROFILE
