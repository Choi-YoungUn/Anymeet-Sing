# 포팅 매뉴얼 for Any Meet & Sing

본 매뉴얼은 Any Meet & Sing을 빌드 및 배포하는데 필요한 방법을 정의하였습니다.

**목차**
1. [**개발 환경**](#개발-환경)
* [공통](#공통)
* [Front end](#front-end)
* [Back end](#back-end)

2. [**EC2 서버 설정**](#ec2-서버-설정)
* [기본 정보](#기본-정보)
* [기본 포트](#기본-포트)

3. [**Jenkins 설정**](#jenkins-설정)
* [Jenkins 설치](#jenkins-설치)
* [Jenkins 플러그인 설치](#jenkins-플러그인-설치)
* [Jenkins Global Tool Configuration](#jenkins-global-tool-configuration)
* [Jenkins Project](#jenkins-project)
* [Jenkins sudo 사용법](#jenkins-sudo-사용법)

4. [**Docker 설정**](#docker-설정)
* [Docker 설치](#docker-설치)
* [sudo 없이 docker 명령어 사용법](#sudo-없이-docker-명령어-사용법)
* [MySQL](#mysql)
* [Kurento Media Server and Coturn](#kurento-media-server-and-coturn)
* [Nginx and Certbot](#nginx-and-certbot)

5. [**기타 설정**](#기타-설정)
* [Youtube API 설정](#youtube-api-설정)
* [yt-dlp](#yt-dlp)
* [gstreamer](#gstreamer)
* [Backend Https 관련](#backend-https-관련)

6. [**빌드 및 배포**](#빌드-및-배포)

-----

## 1. 개발 환경
### 공통
> * Windows 10 64bit
> * MySQL Workbench 8.0 Version 8.0.21 Community
> * Ubuntu 20.04 LTS

### Front end
> * Vue.js, HTML, CSS, JavaScript
> * Visual Studio Code Version 1.64.2

### Back end
> * Java, Spring Boot
> * Spring Tool Suites 3 Version 3.9.15
	  * (Eclipse 2020-06)
	  * Lombok v1.18.22
> *OpenJDK Version 1.8.0_192
	  * Zulu 8.33.0.1-win64
> *Apache Tomcat Version 9.0.56
> *MyBatis Spring Boot Starter Version 2.2.1


---------------

## 2. EC2 서버 설정
### 기본 정보
> * DNS 주소 : i6a505.p.ssafy.io
> * public IPv4 주소 : 3.36.108.155
> * private IPv4 주소 : 172.26.2.248
> * Jenkins Version 2.319.2
> * OpenJDK version 1.8.0_312


-----

### 기본 포트
아래의 명령어를 통해 서버의 포트들을 사용하고 허용된 포트들을 확인할 수 있습니다.
```
$ sudo ufw enable
$ sudo ufw allow {port}
$ sudo ufw status
```
아래의 포트들은 본 프로젝트에 사용되는 포트입니다.
미리 설정해두면 이후 진행될 세팅 때 포트 작업을 생략할 수 있습니다.

> * **3306** : Docker Container - MySQL
> * **Nginx HTTP** : Docker Container - Nginx 규칙
> * **8080** : Docker Container - Backend (http)
> * **80** : Docekr Container - Nginx & Frontend (http)
> * **OpenSSH** : Jenkins & OpenSSH 규칙
> * **9090** : Jenkins
> * **8888** : Docker Container - Kurento Media Server
> * **3478** : Coturn
> * **49152:65535/udp** : Coturn
> * **8443** : Docker Container - Backend (https)
> * **443** : Docker Container - Nginx & Frontend (https)

-----

## 3. Jenkins 설정
### Jenkins 설치
Jenkins를 ubuntu에 최신 버전을 설치할 수 있도록 먼저 아래의 명령어를 수행해야 합니다.
```
$ wget -q -O - https://pkg.jenkins.io/debian-stable/jenkins.io.key \
| sudo apt-key add -
$ sudo sh -c 'echo deb http://pkg.jenkins.io/debian-stable binary/ \
> /etc/apt/sources.list.d/jenkins.list'
```
위 명령어로 repository key를 가져오고 ubuntu 용 Debian package를 가져오기 위한 repository address를 sources.list에 저장합니다.
그 다음 아래의 명령을 실행하면 Jenkins 설치를 하고 실행할 수 있습니다.
```
1. $ sudo apt-get update
2. $ sudo apt-get install jenkins
3. $ sudo systemctl start jenkins
4. $ sudo systemctl status jenkins
```
1,2를 수행하면 Jenkins 설치가 진행되고 3을 통해 Jenkins를 시스템에 동작하도록 할 수 있습니다.
4를 통해 Jenkins 서비스가 active(exited)가 녹색으로 떠 있다면 잘 실행 중인 것을 알 수 있습니다. 
```
$ sudo ufw allow 9090
$ sudo ufw allow OpenSSH
$ sudo ufw enable
```
본 프로젝트는 8080은 Backend와 관련된 포트이므로 아래의 명령어를 통해 Jenkins 시스템 환경 설정에 접근하여 Jenkins를 9090 포트를 통해 접근할 수 있도록 합니다.
```
$ sudo vim /etc/sysconfig/jenkins
$ JENKINS_PORT="9090"
```
위 작업이 완료되었다면 **http://i6a505.p.ssafy.io:9090** URL을 브라우저로 접속하면 Jenkins 설치를 위한 비밀번호를 입력하는 창이 뜹니다.
해당 페이지에 입력해야 하는 비밀번호는 vim 또는 cat으로 확인할 수 있습니다.
```
$ sudo cat /var/lib/jenkins/secrets/initialAdminPassword
또는
$ sudo vim /var/lib/jenkins/secrets/initialAdminPassword
```
이를 통해 확인한 비밀번호를 복사하여 넣으면 Jenkins 설치가 시작됩니다.

**Customize Jenkins**라는 제목의 페이지가 나타나며 **Install suggested plugins**를 선택하고 기본 플러그인들이 설치될 때까지 기다립니다.

그 다음으로 **Create First Admin User** 페이지가 나타나며, 관리자 계정을 생성하고 우측 하단의 **Save and Continue**를 선택합니다.

다음 페이지인 **Instance Configuration**에서는 **http://i6a505.p.ssafy:9090**을 입력하고 **Save and Finish**를 눌러줍니다.

그러면 **Jenkins is ready!** 페이지가 나타나고 **Start using Jenkins**를 선택하면 Jenkins를 사용할 수 있게 됩니다.


-----

###  Jenkins 플러그인 설치
> 본 설명은 한국어 버전 Jenkins를 기반으로 작성하였습니다

Jenkins를 사용할 수 있게 되었다면 프로젝트의 Gitlab과 연결하는 작업이 필요합니다.
좌측 메뉴의 **Jenkins 관리** 를 눌러 관리 페이지로 이동합니다.

관리 페이지가 나타나면 **플러그인 관리** 을 선택합니다.

플러그인 관리 페이지에서 **설치 가능** 탭을 선택해주고 위의 검색 창에 아래의 플러그인들을 찾아 관련 플러그인들을 다 설치해 줍니다.
> NodeJS
> Gitlab
> SSH

이름 옆 Install 체크를 한 뒤 **Install without restart** 를 누르면 설치 확인 페이지로 이동합니다.  
성공/Success가 뜨면 설치가 완료된 것이므로 **메인 페이지로 돌아가기** 를 눌러 메인 페이지로 돌아간 뒤 다른 플러그인들도 설치를 진행합니다.


-----

### Jenkins Global Tool Configuration
> 본 설명은 한국어 버전 Jenkins를 기반으로 작성하였습니다

플러그인들을 다 설치했다면 Jenkins에서 Build를 진행할 때 필요한 Global Tool을 설정해주어야 합니다.
**Jenkins 관리 > System Configuration의 Global Tool Configuration** 으로 이동합니다.

Global Tool Configuration 페이지로 이동하면 Maven Configuration, JDK, Git, Gradle 등의 프로그램들 이름이 나열되어 있는 것을 확인할 수 있는데 본 설정에서는 Git, Maven, NodeJS를 활성화 할 것입니다.

Git 칸을 확인하면 **Add Git** 이라는 드롭다운 메뉴가 있으며 클릭 후 Git을 눌러 Git을 추가합니다.

Maven 칸을 확인하여 **Add Maven** 을 클릭하고 **Name**과 **Version**을 설정합니다. 이름은 구별할 수 있는 이름이면 충분하고 버전은 드롭다운 메뉴의 최신 버전으로 설정하면 됩니다. Install automatically에 체크가 되어 있는지도 확인해 줍니다.

NodeJS 칸을 확인하여 **Add NodeJS** 를 클릭하고 **Name**과 **Version**을 설정합니다. Maven 때와 같은 부분만 확인해주면 됩니다.

세 도구 설정이 완료 되었다면 아래의 **Save** 버튼을 눌러 설정을 저장합니다.


-----

### Jenkins Project
> 본 설명은 한국어 버전 Jenkins를 기반으로 작성하였습니다

플러그인 설치와 Global Tool Configuration이 끝났다면 Jenkins가 CD/CI를 진행할 프로젝트를 만들어야 합니다. 좌측 메뉴의 **새로운 Item**을 누르면 새로운 페이지로 이동합니다.

프로젝트 이름을 **Enter an item name** 에 입력해주시고 Freestyle project를 선택하고 하단의 **OK** 를 누릅니다.

프로젝트가 생성되고 프로젝트 설정을 입력할 수 있는 페이지가 나타납니다. 해당 페이지는 메인 페이지에서 프로젝트 테이블에서 프로젝트 이름을 클릭하고 프로젝트 페이지 좌측 메뉴의 **구성** 을 통해 다시 접근할 수 있습니다.

만약 잘못해서 해당 프로젝트를 지우고 다시 만들고자 하면 좌측 메뉴의 **Project 삭제** 를 선택하면 됩니다.

구성에서 본격적으로 프로젝트 Repository와 연결을 진행합니다.
**소스 코드 관리** 에 Git 라디오 버튼을 클릭하고 나타나는 **Repository URL** 에 본 프로젝트의 Repository Clone 주소를 입력합니다.
> https://lab.ssafy.com/s06-webmobile1-sub2/S06P12A505.git 

**Credential** 은 처음 설정에는 없으므로 옆의 **Key Add** 를 눌러 Jenkins를 클릭하고 https://lab.ssafy.com/ 에서 사용하는 **Username** 계정 아이디(**이메일 주소 @ 앞만**)와 **비밀번호**를 입력하고 ID (key 식별을 위한 이름) 을 입력하고 Add를 선택합니다. 그러면 Credential 드롭다운 메뉴에 방금 등록한 키를 선택할 수 있습니다.

우측의 **고급...** 버튼을 누르면 어떤 branch의 코드를 가져올 지 설정할 수 있는데 git flow를 따르는 프로젝트였기에 */master를 선택하였습니다. 필요 시에는 다른 branch도 **Add Branch** 를 클릭하여 추가할 수 있습니다.

**빌드 유발** 은 어떤 행동을 하면 빌드를 진행할 지 선택할 수 있습니다. 본 프로젝트의 경우 push를 하거나 merge를 성공했을 때 빌드를 진행하도록 설정하였습니다
```
Build when a change is pushed to GitLab. GitLab webhook URL:
Push Events
Accepted Merge Request Events
Approved Merge Request (EE-only)
```
설정을 하였다면 우측의 **고급...** 버튼을 누르고 스크롤을 내리면 **Secret token**을 확인할 수 있습니다. 이를 복사하고 일단 하단의 **저장** 버튼을 클릭하여 설정을 저장합니다.

복사를 하고 본 프로젝트 Repository에 접속하여 좌측 메뉴 맨 아래에 있는 톱니 모양 메뉴 **Settings** 의 **Webhooks** 에 접근합니다.
> https://lab.ssafy.com/s06-webmobile1-sub2/S06P12A505

Webhook 페이지에 들어갔다면 **URL**, **Secret token**, **Trigger** 메뉴를 확인할 수 있습니다. 각 칸에 Jenkins 프로젝트 주소, 앞서 복사했던 Secret token을 입력하고 Trigger는 Push events를 선택하고 Branch 이름은 **master**를 입력합니다.
> Jenkins 프로젝트 주소 : http://i6a505.p.ssafy.io:9090/project/{프로젝트 이름}

입력을 다 하였다면 아래의 **Add webhook** 을 클릭하여 하단에 Project Hooks에 등록한 webhook이 등록되며 Test 드롭다운 메뉴에서 **Push events**를 클릭하면 페이지가 새로고침 되면서 파란색 200 태그가 생성되면 신호가 잘 보내졌음을 확인할 수 있습니다.

다시 Jenkins에 돌아가서 프로젝트 페이지로 이동하면 좌측 하단의 **Build History** 에 번호와 함께 빌드가 진행되면 정상적으로 연결된 것입니다.

하지만 Build 명령을 입력하지 않았기에 Success가 뜰 것이며 Build History의 빌드 기록을 클릭하면 해당 빌드 페이지로 이동하는데 Console Output에서 Jenkins에서 수행한 기록을 확인할 수 있습니다.

다시 프로젝트 페이지로 이동하여 **구성** 페이지로 돌아가서 Build 항목의 **Add build step** 드롭다운 메뉴를 클릭하고 **Execute shell**을 선택합니다. 그러면 명령어를 입력할 수 있는 칸이 생성되며 해당 칸에 exec 폴더에 있는 **build_command.txt** 내용을 복사하여 붙여 넣고 하단의 **저장** 버튼을 클릭하여 저장합니다.

하지만 아직 세팅이 완전히 되지 않아 이대로 build를 진행하면 오류가 발생할 것입니다.
아래의 세팅을 전부 진행해야 제대로 작동할 것입니다.
> 최초 빌드를 진행할 경우 docker stop과 docker rmi 명령(11,12번째 줄)은 주석 처리 하여야 합니다. 


-----

### Jenkins sudo 사용법
처음 Jenkins로 build command에 sudo를 사용할 수 없는 것을 확인할 수 있으며 이를 해결하기 위한 설정 방법입니다. 
```
1. $ sudo touch /etc/sudoers.d/jenkins
2. $ sudo vim jenkins
3. $ jenkins ALL=(ALL) NOPASSWD: ALL
4. $ sudo chmod 0440 jenkins 
5. $ sudo restart
```
1을 통해 sudo 명령어를 사용할 수 있는 인원을 등록할 파일을 만듭니다.
2를 통해 vi 또는 vim을 사용하여 생성한 jenkins 파일을 수정하러 들어갑니다.
3을 jenkins 파일에 입력하고 :w로 저장하고 :q로 종료합니다.
4를 통해 jenkins 파일이 readonly로 동작하도록 합니다
5를 통해 서버를 재시작 합니다.


-----

## 4. Docker 설정
> 만약 서버를 재시작하면 Docker내 컨테이너들이 exited (종료)된 상태가 되므로 아래의 명령어를 통해 다시 실행해주어야 합니다.
```
$ sudo docker start <컨테이너 이름>
```


-----

### Docker 설치
주요 시스템들을 등록하여 사용하기 위해 Docker와 Docker-compose를 설치합니다.
* Docker Version : 20.10.12
```
$ sudo apt-get update
$ sudo apt-get install apt-transport-https ca-certificates curl \
gnupg lsb-release
$ curl -fsSL https://download.docker.com/linux/ubuntu/gpg \
| sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
$ echo "deb \
[arch=amd64 signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] \
https://download.docker.com/linux/ubuntu \
$(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list \
> /dev/null
$ sudo apt-get update
$ sudo apt-get install docker
```
Jenkins 때처럼 최신 버전을 설치하기 위한 명령들을 수행한 뒤 Docker를 설치합니다.

* docker-compose Version: 1.29.2
```
$ sudo apt-get install docker-compose
```


-----

### sudo 없이 docker 명령어 사용법
docker 명령어를 수행할 때 sudo를 입력해야 작동할 때 편의를 위해 sudo를 사용하지 않게 설정하는 방법입니다.
```
1. $ sudo groupadd docker
2-1. $ sudo usermod -aG docker ${USER}
2-2. $ sudo usermod -aG docker jenkins
3. $sudo service docker restart
4. docker run hello-world
```
1을 통해 docker group을 추가합니다. 대체로 docker을 설치할 때 group에 추가되므로 생략해도 됩니다.
2를 통해 사용자를 등록합니다 ${USER} 변수는 현재 사용자를 입력하므로 현재 사용자를 추가하고 **jenkins** 역시 등록해 줍니다 (build command 실행 관련 설정)
3을 통해 docker 서비스를 재시작 합니다.
원하는 경우 4를 통해 sudo를 사용하지 않아도 되는지 확인하면 됩니다.


-----

### MySQL
Backend와 통신하며 정보들을 저장할 데이터베이스인 MySQL를 설치합니다.
> 컨테이너 이름 : mysql_a505
> Version : Latest
* 설치 명령어
```
$ docker pull mysql:latest
$ docker run --name mysql_a505 -p 3306:3306 -e MYSQL_ROOT_PASSWORD=prj_a505! \
-e MYSQL_DATABASE=anymeetsong -d mysql:latest
````

* DB 접근 설정
> Hostname: i6a505.p.ssafy.io
Port: 3306
Username: root 또는 prj_a505
> Password: prj_a505!

접근 설정을 완료하였다면 MySQL Workbench를 실행하고  **MySQL Connections +** 를 눌러 DB 연결을 세팅합니다.
**Connection name** 은 prj_a505로 설정하고 (식별할 수 있는 이름) 
**Host Name** , **Port** , **Username** 은 위 정보에 따라 입력하고 **Password**는 **Store in Vault...** 를 클릭하고 위 정보에 따라 비밀번호를 입력합니다.

**Test Connection** 을 통해 연결이 되는지 확인할 수 있으며 아래와 같이 뜨면 연결이 잘 된 것이므로 **Ok**를 누릅니다
> Successfully made the MySQL Connection

설정을 완료하면 Home 페이지에 connection 이름과 username과 hostname이 적힌 칸이 생성되며 이를 클릭하면 본 프로젝트에 사용되는 MySQL에 접근할 수 있습니다.

해당 탭으로 들어가면 새로운 창이 상단 탭으로 생성되며 좌측 SCHEMAS에 우측 클릭을 하고 
**Create Scheama...** 를 선택합니다. 그러면 새로운 DB를 만드는 창이 중앙에 나타나고 **Name**을 입력할 수 있는데 **anymeetsong** 이라고 입력하고 중앙 우측 하단의 **Apply**를 눌러 DB를 생성합니다.
> 만약 Apply 와 같이 중앙 우측 하단에 버튼이 보이지 않는다면 Workbench 프로그램을 최대화 하여 확인해보시기 바랍니다. 

생성이 되면 SCHEMAS 메뉴에 anymeetsong DB가 생성되며 이를 두번 클릭하면 해당 DB가 볼드 처리가 됩니다. 이는 해당 DB를 사용하겠다는 의미이며, 다음으로 상단 메뉴의 **Server** 에 있는 **Data Import** 를 선택합니다.

**Import from Self-Contained File** 을 선택한 뒤 exec 폴더에 있는 anymeetnsing.sql 을 다운받고 선택하여 줍니다. 그리고 중앙 우측 하단의 **Start Import** 를 눌러 데이터를 import 합니다. 

해당 과정이 완료되면 데이터베이스 설정이 완료됩니다.
> DB 구조는 exec 폴더 내 erd.png 이미지를 통해 확인할 수 있습니다.


-----

### Kurento Media Server and Coturn
Web RTC를 구현하고 클라이언트 간 통신을 하기 위해 사용하는 미디어 서버인 Kurento Media Server와 다른 네트워크 환경에 연결된 사용자들이 Kurento Media Server를 통해 서로를 인식하고 통신할 수 있도록 하는 Coturn을 설치합니다.
> 컨테이너 이름 : kms_a505
> Version : Latest
* 설치 명령어
```
$ docker pull kurento/kurento-media-server:latest
$ docker run -d --name kms_a505 \ 
--network host kurento/kurento-media-server:latest

$ sudo apt-get update
$ sudo apt-get install --no-install-recommends --yes coturn
```
Kurento Media Server는 Docker Container로 설치하고 Coturn은 Docker 밖에 설치합니다.

* 설정
[기본 포트](#기본-포트)의 설명을 따라 아래의 포트가 허용되어 있는지 확인합니다.
> 3478
> 49152:65535/udp

포트가 확인되었다면 본격적인 설정을 진행합니다. 먼저 /etc/default/coturn 파일에 접근하여 TURNSERVER_ENABLED 주석을 풀어준 뒤 :w :q로 저장하고 vim을 종료합니다.
```
$ sudo vim /etc/default/coturn

TURNSERVER_ENABLED=1
```

다음으로 /etc/turnserver.conf 파일에 접근하여 아래의 정보들의 주석을 풀고 수정해 줍니다.
IPv4 정보는 [기본 정보](#기본-정보)에 정의되어 있습니다.
```
$ sudo vim /etc/turnserver.conf

listening-port=3478
tls-listening-port=5349
listening-ip=172.26.2.248
relay-ip=172.26.2.248
external-ip=3.36.108.155/172.26.2.248
fingerprint
lt-cred-mech
user=prj_a505:prj_a505!
realm=prj_a505_realm
log-file=/var/tmp/turn.log
simple-log
```
수정을 완료했다면 Coturn 서비스를 재시작 합니다.
```
$ sudo service coturn restart
```
Coturn 설정이 완료되었다면 이제 Kurento Media Server 설정을 진행합니다.
```
$ docker ps -a
$ docker exec -it kms_a505 bash
```
먼저 Docker의 컨테이너들을 확인하고 kms_a505라는 이름의 컨테이너를 확인하고 두 번째 명령어를 통해 해당 컨테이너에 접속합니다. 
접속을 하게 되면 root 계정으로 나타나며 /etc/kurento/modules/kurento/WebRtcEndpoint.conf.ini 파일을 수정하러 접근합니다.
만약 vim이 설치되어 있지 않다는 오류가 발생하면 아래의 명령어를 수행하고 진행합니다.
```
$ apt-get update
$ apt-get install vim
```
설치가 이미 되어있거나 설치를 완료하였다면 아래 파일에 접근하여 주석을 해제하고 수정을 진행합니다. 수정이 완료되면 :w :q로 저장하고 나오며 마지막 exit 명령어를 통해 컨테이너에서 나옵니다.
```
$ vim /etc/kurento/modules/kurento/WebRtcEndpoint.conf.ini

stunServerAddress=3.36.108.155
stunServerPort=3478
turnURL=prj_a505:prj_a505!@3.36.108.155:3478?transport=udp

$ exit
```
이로서 Kurento Media Server와 Coturn 설정이 완료되었습니다.


-----

### Nginx and Certbot
> 컨테이너 이름 : nginx_a505
> Version Nginx : stable-alpine
* 설치 준비
	* cd ~ 위치에서 exec > 포팅 파일 폴더에 있는 세 파일(docker-compose.yml, app.conf, init-letsencrypt.sh)을 가져와 아래와 같이 준비합니다.
		* docker-compose.yml
		* /home/ubuntu/data/nginx/app.conf
		* /home/ubuntu/init-letsencrypt.sh

Web RTC 및 Kurento를 통해 화상 통화를 하기 위해서는 Frontend가 Https 통신을 하도록 만들어야 했기 때문에 기존 설치와 다르게 docker-compose 파일을 통해 두 이미지를 한번에 같이 처리하도록 구현하였습니다.

Nginx는 Frontend에 접속할 수 있도록 해주는 웹 서버 소프트웨어이며 Certbot은 무료로 ssl 인증을 하여 https 통신을 할 수 있게 해주는 시스템 입니다.

**docker-compose.yml** 는 Nginx와 Certbot 을 Docker 컨테이너로 등록하기 위한 설정이 적혀있으며 컨테이너 이름, 사용할 이미지, 사용할 포트, 공유 폴더 설정과 SSL 자동 재인증 관련된 명령어가 입력되어 있습니다.

**app.conf** 는 Nginx의 default conf 파일을 대체하여 Nginx 시스템 설정을 정의한 파일입니다.
http로 접근할 시 https로 접근하도록 설정하였으며 Certbot과 관련된 위치 설정과 ssl 파일 관련 설정, https://i6a505.p.ssafy.io 에 접근 시 보여줄 페이지 (index.html) 위치 설정이 입력되어 있습니다.

**init-letsencrypt.sh** 는 아래의 명령어를 통해 얻은 파일로 vim 파일로 열어 domain과  email를 수정한 파일입니다. domain은 i6a505.p.ssafy.io로 설정하였습니다. 본 파일을 실행하면 SSL 인증이 된 Nginx Docker 컨테이너를 생성합니다.
> curl -L https://raw.githubusercontent.com/wmnnd/nginx-certbot/master/init-letsencrypt.sh 

* 설치 명령어
```
$ sudo ./init-letsencrypt.sh
```
위 파일을 실행하면 자동적으로 Nginx Docker 컨테이너와 Certbot 컨테이너가 생성되고 Nginx에 SSL 인증 관련 작업이 진행됩니다.
> 일주일에 5번 인증 제한이 걸려있기에 최초 인증 진행 시
>  $ vim init-letsencrypt.sh 로 파일에 접근하고
>  staging=1을 staging=0으로 설정하여 진행하고
>  이후 다시 staging=1로 수정하여 여러 번 실행해도 새로운 인증을 받지 않게 합니다.

y를 입력하여 작업을 다 진행하고 아래의 명령어를 통해 Nginx_a505 컨테이너가 등록된 것을 확인하면 Nginx & Certbot https 설정이 완료된 것입니다.
```
$ docker ps -a
```


-----

## 5. 기타 설정

### Youtube API 설정
유튜브에서 노래 검색 기능을 활용하기 위해서 API KEY를 발급해줘야 합니다.
우선 구글 계정을 준비하고, 구글 개발자 콘솔(https://console.developers.google.com/)로 접속하여 새 프로젝트를 생성해줍니다. 
프로젝트 생성 후 'API 및 서비스 사용 설정'을 클릭하고, youtube를 검색해서 'Youtube Data API v3'를 선택한 뒤에 사용 버튼을 눌러줍니다.
그 뒤, 사용자 인증 정보 만들기를 마치면 API Key가 주어지고 이 Key를 통해서 Youtube 검색 기능을 활용할 수 있습니다.

####  Youtube API Request
```
https://www.googleapis.com/youtube/v3/search?key={본인의_API_KEY}
```

#### Youtube Request Parameter
-   part=id,snippet
-   channelId=UCZUhx8ClCv6paFW7qi3qljg (TJ미디어 채널)
-   maxResults=25 (상의 후 조절)
-   q= (검색어가 들어갈 자리)
-   regionCode= KR (필요시 변경)
-   type=video
-   fields=pageInfo.totalResults,items(id,snippet(title))


---

### yt-dlp
> Version: 2022.02.04

유튜브 영상을 가져오기 위해서 사용하는 라이브러리 입니다. 설치하기 위해 다음 명령어를 사용합니다.
```
$ sudo wget https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp -O \
/usr/local/bin/yt-dlp
$ sudo chmod a+rx /usr/local/bin/yt-dlp
```

argument로 해당 영상의 해상도, 포멧, 음성의 품질, 포멧 등을 미리 설정하고 Youtube URL로 해당 영상을 다운받아옵니다. 다음 명령어를 통해서 수행할 수 있습니다.
```
$ yt-dlp --format "worst[ext=mp4][protocol=https]" --get-url ${URL}
```
기본 옵션을 사용해서 다운 받는다면, 버퍼링 문제가 심하기 때문에 몇 가지 argument를 추가해서 버퍼링을 줄일 수 있습니다.
```
$ yt-dlp --extractor-args youtube:player_client=android \
 --throttled-rate 100K --format "worst[ext=mp4][protocol=https]" \
 --get-url https://www.youtube.com/watch?v=$3
```


---

### gstreamer
> Version: gstreamer1.0

gStreamer는 yt-dlp를 통해서 다운받아온 영상을 gStreamer를 통해 RTP 포멧으로 변환하고 WebRTC 서버로 보내줍니다.
gStreamer는 다음 명령어를 수행하여 설치할 수 있습니다.
```
$ sudo apt-get update && sudo apt-get install --no-install-recommends --yes \
    gstreamer1.0-{tools,libav} gstreamer1.0-plugins-{base,good,bad,ugly}
```

열려져 있는 포트로 영상을 전달하기 위해서 비디오 포트와 오디오 포트가 필요합니다. 아래와 같은 명령어로 영상을 전달할 수 있습니다.
```
PEER_A=${AUDIO_PORT} PEER_V=${VIDEO_PORT}
PEER_IP=172.26.2.248 SELF_A=5006
SELF_ASSRC=445566 SELF_V=5004 SELF_VSSRC=112233
bash -c 'gst-launch-1.0 -e \ 
rtpbin name=r sdes="application/x-rtp-source-sdes,cname=(string)\"user\@example.com\"" \
 uridecodebin uri="$(youtube-dl --extractor-args youtube:player_client=android \ 
 --throttled-rate 100K --format "worst[ext=mp4][protocol=https]" \ 
 https://www.youtube.com/watch?v=TboZvvxIhco)" name=d \ 
 d. ! queue \ 
 ! audioconvert ! audioresample ! opusenc \ 
 ! rtpopuspay \ 
 ! "application/x-rtp,payload=(int)96,clock-rate=(int)48000,ssrc=(uint)$SELF_ASSRC" \ 
 ! r.send_rtp_sink_0 \ d. ! queue \ 
 ! videoconvert ! x264enc tune=zerolatency \ 
 ! rtph264pay \ 
 ! "application/x-rtp,payload=(int)103,clock-rate=(int)90000,ssrc=(uint)$SELF_VSSRC" \ 
 ! r.send_rtp_sink_1 \ r.send_rtp_src_0 \ 
 ! udpsink host=$PEER_IP port=$PEER_A bind-port=$SELF_A \ r.send_rtcp_src_0 \ 
 ! udpsink host=$PEER_IP port=$((PEER_A+1)) bind-port=$((SELF_A+1)) sync=false async=false \
  udpsrc port=$((SELF_A+1)) \ 
  ! r.recv_rtcp_sink_0 \ 
  r.send_rtp_src_1 \ 
  ! udpsink host=$PEER_IP port=$PEER_V bind-port=$SELF_V \ 
  r.send_rtcp_src_1 \ 
  ! udpsink host=$PEER_IP port=$((PEER_V+1)) bind-port=$((SELF_V+1)) sync=false async=false \
   udpsrc port=$((SELF_V+1)) \ 
   ! tee name=t \ 
   t. ! queue ! r.recv_rtcp_sink_1 \ 
   t. ! queue ! fakesink dump=true async=false'
```
위 명령어는 쉘 스크립트로 생성되어, 자바에서 자동으로 실행되도록 구현되어 있습니다.
exec > 포팅 파일 폴더의 **stream.sh** 파일이며 /home/ubuntu 에 위치하면 됩니다.

### Backend Https 관련
Frontend는 Nginx와 Certbot을 사용하여 Https를 구현하였고 이때 생성된 인증서 파일로 Backend에서도 사용할 수 있도록 하겠습니다.
먼저 위 설치 과정을 진행하였다면 ~/data/certbot/conf/live/i6a505.p.ssafy.io 위치에 **cert.pem** , **privkey.pem**, **chain.pem** 파일이 있는 것을 확인할 수 있습니다. 해당 위치로 이동하셔서 아래의 명령어를 수행하면 **keystore.p12** 인증서 파일이 생성됩니다.
```
$ sudo openssl pkcs12 -export -in cert.pem -inkey privkey.pem -out keystore.p12 -name ttp -CAfile chain.pem -caname root
```

keystore.p12 파일이 만들어 진 것을 확인하였다면 해당 파일을 build 전 application.properties 파일이 있는 backend/AnyMeetSong/src/main/resources 폴더에 넣고 build를 수행합니다. 해당 작업은 인증서를 갱신할 때 (90일) 함께 진행해주셔야 하며, 즉 Gitlab에 해당 파일을 갱신해주어야 합니다.

-----

## 6. 빌드 및 배포
EC2 내 Jenkins, Docker, Nginx, MySQL, KMS, Coturn 설정이 완료되었다면 빌드 및 배포를 진행할 수 있습니다. Master branch에 push 이벤트나 merge 이벤트가 발생하면 Jenkins는 build command의 명령어를 수행합니다.
```
cd frontend/anymeetsong	
sudo npm install
sudo npm run build
docker cp dist nginx_a505:/usr/share/nginx/html
cd ../../backend/AnyMeetSong

/var/lib/jenkins/tools/hudson.tasks.Maven_MavenInstallation/3.8.4/bin/mvn \
install -DskipTests=true

sudo kill -15 `cat /home/ubuntu/app.pid`
sudo rm /home/ubuntu/app.pid
sudo rm /home/ubuntu/app.out
sudo rm /home/ubuntu/app.war
sudo cp target/anymeetsong-0.0.1-SNAPSHOT.war /home/ubuntu/app.war
sudo bash -c "nohup java -jar /home/ubuntu/app.war > /home/ubuntu/app.out 2>&1 & echo \$! > /home/ubuntu/app.pid"

cd ../..
```
먼저 git master branch에서 프로젝트를 가져옵니다. 저장 위치는 아래와 같습니다.
> /var/lib/jenkins/workspace/prj_a505

그리고 cd 명령어를 통해 Frontend의 폴더로 이동합니다. 그리고 NPM 명령을 실행하여 Dependency를 설치하고 Frontend 빌드를 진행합니다.

빌드가 완료되면 Nginx Docker Container의 root 폴더에 빌드한 파일을 전송합니다. 이를 통해 https://i6a505.p.ssafy.io 에 접근하면 Frontend 웹 페이지들을 볼 수 있게 됩니다.

이후 다시 처음으로 돌아와 이번에는 Backend의 폴더로 이동합니다. 그리고 mvn 명령(Maven)을 수행하여 Backend 빌드를 진행합니다.
> Backend는 개발을 하면서 Java Keytool을 통해 인증서를 만들어 해당 파일을 Repository에 함께 등록했기에 따로 SSL 관련 설정을 진행하지 않고 바로 빌드를 진행하면 됩니다.

sudo kill -15 `cat /home/ubuntu/app.pid` 부터 sudo rm /home/ubuntu/app.war 까지는 기존의 Backend 정보들을 삭제하는 작업이므로 **최초**로 빌드를 진행할 때는 #으로 주석 처리를 해두고 한번 빌드를 진행한 이후에는 주석을 풀어 사용해야 합니다.

bash 명령어를 통해 빌드된 .war 파일을 백그라운드에 실행하며 해당 과정에서 콘솔 정보를 출력하는 app.out 파일과 pid를 저장하는 app.pid 파일을 생성합니다.

그리고 다시 처음 시작했던 폴더 위치로 돌아가며 빌드 명령 수행이 끝납니다.

Build History를 통해 빌드 성공 실패를 확인할 수 있으며 Console Output을 통해 수행한 작업을 확인하거나 오류 사항을 확인할 수 있습니다.

빌드를 성공하면 https://i6a505.p.ssafy.io 에 접속하여 본 프로젝트가 제대로 빌드되어 배포 및 정상 작동하는지 확인할 수 있습니다.

이로써 포팅 매뉴얼을 마칩니다.
