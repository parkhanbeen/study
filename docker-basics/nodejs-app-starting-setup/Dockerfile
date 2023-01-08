FROM node:12

WORKDIR /app

COPY package.json /app

# 첫번째 .은 현재 디렉토리 및 하위 파일을 모두 복사
# 두번쩨 .은 그 파일을 저장해야 하는 이미지 내부 경로
# /app 은 루트 디렉토리가 아닌 서브 디렉토리를 사용 (app이라는 디렉토리)
# 즉 Dockerfile 선상에 위치하는 모든 디렉토리 및 하위 파일을 /app 이라는 컨테이너 내부 서브 폴더에 복사
COPY . /app

RUN npm install

EXPOSE 80

# 이미지 기반 컨테이너가 시작될 때 실행됨
# CMD에 특정하지 않으면 베이스 이미지가 실행되고, 없을 경우 에러 발생
CMD [ "node", "server.js" ]