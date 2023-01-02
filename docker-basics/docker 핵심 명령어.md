# 도커 핵심 명령어

* docker ps : 실행 중인 모든 컨데이너 표시
* docker ps -a : 중지된 컨테이너 포함 모든 컨테이너 표시
* docker ps --help : docker ps에 사용 가능한 모든 구성 옵션 표시

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