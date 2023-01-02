# 도커 핵심 명령어

* docker ps : 실행 중인 모든 컨데이너 표시
* docker ps -a : 중지된 컨테이너 포함 모든 컨테이너 표시
* docker ps --help : docker ps에 사용 가능한 모든 구성 옵션 표시
* docker images : 이미지 리스팅
    * 컨테이너에 속한 이미지는 제거할 수 없음
    * 즉, 모든 컨테이너를 삭제하고 이미지를 삭제해야함

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
