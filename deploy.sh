#!/bin/bash

# 배포 스크립트
echo "AdminService 배포를 시작합니다..."

# 환경 변수 설정
export DB_USERNAME=postgres
export DB_PASSWORD=your_secure_password
export JWT_SECRET=your_jwt_secret_key

# 애플리케이션 디렉토리로 이동
cd /path/to/adminservice

# Git 리포지토리에서 최신 코드 가져오기
git pull origin main

# Gradle 빌드
./gradlew clean build -x test

# 실행 중인 컨테이너 확인 및 중지
RUNNING_CONTAINER=$(docker ps -q --filter "name=adminservice")
if [ ! -z "$RUNNING_CONTAINER" ]; then
    echo "실행 중인 AdminService 컨테이너를 중지합니다..."
    docker stop $RUNNING_CONTAINER
    docker rm $RUNNING_CONTAINER
fi

# Docker 이미지 빌드
docker build -t adminservice:latest .

# Docker 컨테이너 실행
docker run -d \
    --name adminservice \
    -p 8081:8081 \
    -e SPRING_PROFILES_ACTIVE=prod \
    -e DB_USERNAME=$DB_USERNAME \
    -e DB_PASSWORD=$DB_PASSWORD \
    -e JWT_SECRET=$JWT_SECRET \
    --restart unless-stopped \
    adminservice:latest

echo "AdminService 배포가 완료되었습니다."
