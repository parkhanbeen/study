# 도커 핵심 명령어

* docker ps : 실행 중인 모든 컨데이너 표시
* docker ps -a : 중지된 컨테이너 포함 모든 컨테이너 표시
* docker ps --help : docker ps에 사용 가능한 모든 구성 옵션 표시
* docker images : 이미지 리스팅
    * 컨테이너에 속한 이미지는 제거할 수 없음
    * 즉, 모든 컨테이너를 삭제하고 이미지를 삭제해야함


* docker build . : Dockerfile 기반으로 이미지를 만듬
* docker build -t {name:tag} . : 도커 이미지 빌드 이름 태그 추가     
* docker tag {이전 이름 : 새이름} : 태그 다시 지정(새 태그 추가 생략 가능)
    ex) docker tag node-demo:latest phb0228/node-hello-world:태그

* docker start {중지된 컨테이너 id 또는 이름} : 중지된 컨테이너 다시 실행
    * 백그라운드에서 실행됨 
    * default : detached 모드
    * -a : attached 모드로 설정
        * ex) docker start -a {id}


* docker run : 컨테이너 실행
    * 포어그라운드에서 실행됨
    * default : attached 모드
    * -d : detached 모드로 설정
        * ex) docker run -p 3000:80 -d {id}


* docker rm : 컨테이너 삭제
    * ex) docker rm {id or name}
* docker image prune : 사용되지 않은 모든 이미지를 제거
* docker rmi : 이미지 삭제
    * ex) docker rmi {image id}

* docker pull {이미지 명} : docker hub에 등록된 이미지를 가져옴
    * 태그를 안 붙이면 가장 최신 이미지를 가져옴
* docker run {이미지 명} : pull 말고 run을 하면 로컬 pc 이미지를 찾지 못하면 hub에서 이미지 확인 후 있으면 자동으로 pull 한 후 run 함
    * 이전에 로컬에 이미지를 받았을 경우 최신 이미지를 hub에서 받을려면 pull 명령을 해야함


* docker exec : 해당 컨테이너가 실행하는 기본 명령 외에 실행 중인 컨테이너 내에서 특정 명령을 실행할 수 있음
    * ex) docker exec {컨테이너 이름}


## 볼륨

* docker volume ls : 도커가 현재 관리 중인 모든 볼륨을 리스팅
* docker volume rm {VOL_NAME} or docker volume prune : 볼륨 삭제

* docker run -p 3000:80 -d --rm --name feedback-app -v feeback:/app/feedback feedback-node:volumes
    * -v feeback:/app/feedback : -v {볼륨명}:{경로}


## 인수와 환경 변수 설정

* --env PORT=8000 : env 환경 설정 포트 8000 키 값으로 지정(-e PORT=8000)
    * docker run -d -p 3000:8000 --env PORT=8000

* Dockerfile에 환경 설정이 되어 있을 경우 도커 이미지 별로 설정 하고 싶을떼
    * ARG DEFAULT_PORT=80 
* docker build -t feedback-node:dev --build-arg DEFAULT_PORT=8000 :  feedback-node:dev 이미지에 별도의 디폴트 포트 지정(8000)


## 컨테이너 정보

* docker container inspect mongodb : mongodb 컨테이너 정보

## 네트워크

* docker network create {이름} : 네트워크 생성
* docker run --network {이름} : 생성된 네트워크 사용 

## 도커 컴포즈

* docker-compose up -d : 도커 컴포즈 실행
    * 모든 이미지를 가져온 다음 컨테이너를 시작
* docker-compose down -v : 모든 컨테이너 삭제 및 생성된 디폴트 네트워크와 모든 것이 종료됨.(-v 플래그로 볼륨도 삭제)

* docker-compose run : 여러 서비스중 단일로 지정해 명령을 전달
    * ex) docker-compose run --rm npm init
