<template>
  <div id="UserCam">
    <div id="container">
      <div id="room" style="display: none;">

          <div class=""  style="height: 350px;">
            <div id="participants" class="d-flex m-5 text99  type99 scroll99 justify-content-center" @mousewheel.prevent="ChangeXYZ"></div>
          </div>

          <div class="row">
            <div class="col-12 col-md-6">
                <div id="videoBig" style="margin-top:44px;">
                <video id="videoRtp" autoplay playsinline
                    width="100%" height="100%" poster="../../assets/images/spinner.gif"></video>
                </div>
                <div class="m-2">
                    <p class="fw-bold" >{{now_play}}</p>
                </div>
                
                <b-icon v-if="is_play===false" icon="play-btn"  @click="clickMusicStart" class="m-1" style="width: 50px; height: 50px;"></b-icon> 
                <b-icon v-else-if="is_play===true" icon="stop-btn"  @click="clickMusicStop" class="m-1" style="width: 50px; height: 50px;"></b-icon> 
                
            </div>
            <div class="col-12 col-md-6" style="width: 45%; height: 420px;">
                <div v-if="route == 2">
                    <button class="room-tag1" @click="clickSearch">노래 예약</button>
                    <button class="room-tag2" @click="clickMusicList">예약 리스트</button>
                </div>
                <div v-else>
                    <button class="room-tag2" @click="clickSearch">노래 예약</button>
                    <button class="room-tag1" @click="clickMusicList">예약 리스트</button>
                </div>
                <div class="room-search-back">
                    <div v-if="route == 2">
                        <Search-Music-From>
                        </Search-Music-From>
                        <!-- 검색된 노래 리스트 컴포넌트 -->
                        <div>
                            <div class="d-flex m-5 text type scroll"   @mousewheel.prevent="ChangeXY">
                                <Search-Music-Item 
                                style="width:250px; margin-right:6px;"
                                v-for="music in searchmusics"
                                :key="music.videoId"
                                :music="music"
                                >
                                </Search-Music-Item>
                            </div>
                        </div>
                    </div>

                    <div v-else-if="route == 1">
                        <div id="MusicList" class="mb-3">
                            <div style="width:60%; color:white; display:inline-block;">노래 제목</div>
                            <div style="width:20%; color:white; display:inline-block;">예약자</div>
                            <div style="width:15%; display:inline-block;"></div>                            
                        </div>
                        <Music-List
                        v-for="music in musiclists"
                        :key="music.videoId"
                        :music="music">
                        </Music-List>
                    </div>
                </div>
            </div>
          </div>
      </div>
    </div>
    
  </div>
</template>

<script>
import SearchMusicItem from '@/components/singingroom/SearchMusicItem'
import SearchMusicFrom from '@/components/singingroom/SearchMusicFrom'
import MusicList from '@/components/singingroom/MusicList'
import kurentoUtils from "kurento-utils"
import axios from 'axios'

// 상대 url
var ws = {};

var participants = {};
var name;

// for Kurento rtp receiver

let videoRtp;
let webRtcPeer;

function explainUserMediaError(err)
{
  const n = err.name;
  if (n === 'NotFoundError' || n === 'DevicesNotFoundError') {
    return "Missing webcam for required tracks";
  }
  else if (n === 'NotReadableError' || n === 'TrackStartError') {
    return "Webcam is already in use";
  }
  else if (n === 'OverconstrainedError' || n === 'ConstraintNotSatisfiedError') {
    return "Webcam doesn't provide required tracks";
  }
  else if (n === 'NotAllowedError' || n === 'PermissionDeniedError') {
    return "Webcam permission has been denied by the user";
  }
  else if (n === 'TypeError') {
    return "No media tracks have been requested";
  }
  else {
    return "Unknown error";
  }
}

/* ============================= */
/* ==== WebSocket signaling ==== */
/* ============================= */


// PROCESS_SDP_ANSWER ----------------------------------------------------------

function handleProcessSdpAnswer(jsonMessage)
{
  console.log("[handleProcessSdpAnswer] SDP Answer received from Kurento Client; process in Kurento Peer");

console.log("Got SDPANSWER @@@@@@@@@@@@@@@@@@@@@@@@@@@")
    console.log(jsonMessage.sdpAnswer);
  webRtcPeer.processAnswer(jsonMessage.sdpAnswer, (err) => {
    if (err) {
      console.error("[handleProcessSdpAnswer] " + err);
      return;
    }

    console.log("[handleProcessSdpAnswer] SDP Answer ready; start remote video");
    startVideo(videoRtp);
  });
}

// ADD_ICE_CANDIDATE -----------------------------------------------------------

function handleAddIceCandidate(jsonMessage)
{
	console.log("Add Ice Candidate");
	console.log(jsonMessage);
  webRtcPeer.addIceCandidate(jsonMessage.candidate, (err) => {
    if (err) {
      console.error("[handleAddIceCandidate] " + err);
      return;
    }
  });
}

// END_PLAYBACK ----------------------------------------------------------------

function handleEndPlayback(jsonMessage)
{
    console.log(jsonMessage);
  hideSpinner(videoRtp);
}

// ERROR -----------------------------------------------------------------------

function error(errMessage)
{
  console.error("[error] " + errMessage);
}

function handleError(jsonMessage)
{
  const errMessage = jsonMessage.message;
  error(errMessage);
}


/* ================== */
/* ==== UI state ==== */
/* ================== */

function showSpinner()
{
  for (let i = 0; i < arguments.length; i++) {
    arguments[i].poster = './img/transparent-1px.png';
    arguments[i].style.background = "center transparent url('./img/spinner.gif') no-repeat";
  }
}

function hideSpinner()
{
  for (let i = 0; i < arguments.length; i++) {
    arguments[i].src = '';
    arguments[i].poster = './img/webrtc.png';
    arguments[i].style.background = '';
  }
}

function startVideo(video)
{
  // Manually start the <video> HTML element
  // This is used instead of the 'autoplay' attribute, because iOS Safari
  //  requires a direct user interaction in order to play a video with audio.
  // Ref: https://developer.mozilla.org/en-US/docs/Web/HTML/Element/video
  video.play().catch((err) => {
    if (err.name === 'NotAllowedError') {
      console.error("[start] Browser doesn't allow playing video: " + err);
    }
    else {
      console.error("[start] Error in video.play(): " + err);
    }
  });
}

// for Kurento rtp receiver

window.onbeforeunload = function() {
    ws.close();
};

function onNewParticipant(request) {
    receiveVideo(request.name);
}

function receiveVideoResponse(result) {
    participants[result.name].rtcPeer.processAnswer (result.sdpAnswer, function (error) {
        if (error) return console.error (error);
    });
}

function callResponse(message) {
    if (message.response != 'accepted') {
        console.info('Call not accepted by peer. Closing call');
        stop();
    } else {
        kurentoUtils.webRtcPeer.processAnswer(message.sdpAnswer, function (error) {
            if (error) return console.error (error);
        });
    }
}

function onExistingParticipants(msg) {
    var constraints = {
        audio : true,
        video : {
            mandatory : {
                maxWidth : 320,
                maxFrameRate : 15,
                minFrameRate : 15
            }
        }
    };

    var participant = new Participant(name);
    participants[name] = participant;
    var video = participant.getVideoElement();

    var options = {
                localVideo: video,
                mediaConstraints: constraints,
                onicecandidate: participant.onIceCandidate.bind(participant)
            }
    participant.rtcPeer = new kurentoUtils.WebRtcPeer.WebRtcPeerSendonly(options,
        function (error) {
            if(error) {
                return console.error(error);
            }
            this.generateOffer (participant.offerToReceiveVideo.bind(participant));
    });

    msg.data.forEach(receiveVideo);
}

function receiveVideo(sender) {
    var participant = new Participant(sender);
    participants[sender] = participant;
    var video = participant.getVideoElement();

    var options = {
            remoteVideo: video,
            onicecandidate: participant.onIceCandidate.bind(participant)
        }

    participant.rtcPeer = new kurentoUtils.WebRtcPeer.WebRtcPeerRecvonly(options,
            function (error) {
                if(error) {
                    return console.error(error);
                }
                this.generateOffer (participant.offerToReceiveVideo.bind(participant));
    });
}

function onParticipantLeft(request) {
    console.log('Participant ' + request.name + ' left');
    var participant = participants[request.name];
    participant.dispose();
    delete participants[request.name];
}

function sendMessageOnMounted(message) {
    var jsonMessage = JSON.stringify(message);
    console.log('111Sending message: ' + jsonMessage);
    ws.onopen = () => {
        ws.send(jsonMessage);
        console.log('222Sending message: ' + jsonMessage);
    };
}

function sendMessage(message) {
    var jsonMessage = JSON.stringify(message);
    console.log('Sending message2: ' + jsonMessage);
    ws.send(jsonMessage);
}


const PARTICIPANT_MAIN_CLASS = 'participant main';
const PARTICIPANT_CLASS = 'participant';

/**
 * Creates a video element for a new participant
 *
 * @param {String} name - the name of the new participant, to be used as tag
 *                        name of the video element.
 *                        The tag of the new element will be 'video<name>'
 * @return
 */
function Participant(name) {
    this.name = name;
    var container = document.createElement('div');
    container.className = isPresentMainParticipant() ? PARTICIPANT_CLASS : PARTICIPANT_MAIN_CLASS;
    container.id = name;
    container.style.display = "inline-block";
    var span = document.createElement('span');
    var video = document.createElement('video');
    video.style.display = "block";
    var rtcPeer;
    console.log(rtcPeer)
    container.appendChild(video);
    container.appendChild(span);
    container.onclick = switchContainerClass;
    document.getElementById('participants').appendChild(container);

    span.appendChild(document.createTextNode(name));
    
    video.className += "m-2"
    container.className += "container-back"
    span.className += "con-span fw-bold fs-5"
    

    video.id = 'video-' + name;
    video.autoplay = true;
    video.controls = false;


    this.getElement = function() {
        return container;
    }

    this.getVideoElement = function() {
        return video;
    }

    function switchContainerClass() {
        if (container.className === PARTICIPANT_CLASS) {
            var elements = Array.prototype.slice.call(document.getElementsByClassName(PARTICIPANT_MAIN_CLASS));
            elements.forEach(function(item) {
                    item.className = PARTICIPANT_CLASS;
                });

                container.className = PARTICIPANT_MAIN_CLASS;
            } else {
            container.className = PARTICIPANT_CLASS;
        }
    }

    function isPresentMainParticipant() {
        return ((document.getElementsByClassName(PARTICIPANT_MAIN_CLASS)).length != 0);
    }

    this.offerToReceiveVideo = function(error, offerSdp, wp){
        if (error) return console.error ("sdp offer error")
        console.log('Invoking SDP offer callback function');
        var msg =  { id : "receiveVideoFrom",
                sender : name,
                sdpOffer : offerSdp
            };
        console.log(wp)
        sendMessage(msg);
    }


    this.onIceCandidate = function (candidate, wp) {
            console.log("Local candidate" + JSON.stringify(candidate));
            console.log(wp)
            var message = {
                id: 'onIceCandidate',
                candidate: candidate,
                name: name
            };
            sendMessage(message);
    }

    Object.defineProperty(this, 'rtcPeer', { writable: true});

    this.dispose = function() {
        console.log('Disposing participant ' + this.name);
        this.rtcPeer.dispose();
        container.parentNode.removeChild(container);
    };
}

console.log(callResponse)

export default {
  name: "UserCam",
  components:{
    SearchMusicItem,
    SearchMusicFrom,
    MusicList,
  },
  data: function () {
    return {
      route : 1,
      now_play : "",
      is_play : false
    }
  },
  methods: {
    clickMusicStart: function(){
        const music = this.$store.state.music_list[0]
        if (music) {
            this.now_play = music.title
            sendMessage({
                id: 'MUSIC_START',
                videoId: music.videoId
            });
            const token = localStorage.getItem('jwt')
            axios.delete(
                this.$store.state.http_url+ this.$store.state.url_sing + "list/" + this.$store.state.enterroom.id +
                "/" + music.songId,
                {
                headers: {
                    "Content-Type": `application/json`,
                    "access-token": token,
                },
                }
            )
            .then(() => {
                this.is_play = true
                this.$store.dispatch('Set_MusicList', this.$store.state.enterroom.id)
            })
        } else {
            alert("노래를 추가해주세요")
        }


        
    },
    clickMusicStop: function(){
        this.now_play = ""
        this.is_play = false
        sendMessage({
            id: 'MUSIC_STOP'
        });
    },

    ChangeXY: function(event){
      if (event.wheelDelta < 0){
        document.querySelector('.text').scrollLeft += 150;
      } else if (event.wheelDelta > 0){
        document.querySelector('.text').scrollLeft -= 150;
      }
      
    },
    ChangeXYZ: function(event){
      if (event.wheelDelta < 0){
        document.querySelector('.text99').scrollLeft += 150;
      } else if (event.wheelDelta > 0){
        document.querySelector('.text99').scrollLeft -= 150;
      }
      
    },
    clickSearch: function(){
      this.route = 2
    },

    clickMusicList: function(){
      this.route = 1
      this.$store.dispatch('Set_MusicList', this.$store.state.enterroom.id)
    },
    start: function() {
  console.log("[start] Create WebRtcPeerRecvonly");
  showSpinner(videoRtp);

  const options = {
    localVideo: null,
    remoteVideo: videoRtp,
    mediaConstraints: { audio: true, video: true },
    onicecandidate: (candidate) => {
	sendMessage({ id: 'ADD_ICE_CANDIDATE',candidate: candidate, name: "youtube"})},
  };

  webRtcPeer = new kurentoUtils.WebRtcPeer.WebRtcPeerRecvonly(options,
      function(err)
  {
    if (err) {
      console.error("[start/WebRtcPeerRecvonly] Error in constructor: "
          + explainUserMediaError(err));
      return;
    }

    console.log("[start/WebRtcPeerRecvonly] Created; generate SDP Offer");
    webRtcPeer.generateOffer((err, sdpOffer) => {
      if (err) {
        console.error("[start/WebRtcPeerRecvonly/generateOffer] " + err);
        return;
      }

      const useComedia = true;

      console.log("[start/WebRtcPeerRecvonly/generateOffer] Use COMEDIA: "
          + useComedia);


      sendMessage({
        id: 'PROCESS_SDP_OFFER',
        sdpOffer: sdpOffer,
        useComedia: useComedia,
      });

      console.log("[start/WebRtcPeerRecvonly/generateOffer] Done!");
    });
  });
},

    startWithOutHost : function() {
        console.log("[startWithOutHost]");
        showSpinner(videoRtp);

    const options = {
        localVideo: null,
        remoteVideo: videoRtp,
        mediaConstraints: { audio: true, video: true },
        onicecandidate: (candidate) => {
            sendMessage({ id: 'ADD_ICE_CANDIDATE',candidate: candidate})
        },
    };

    webRtcPeer = new kurentoUtils.WebRtcPeer.WebRtcPeerRecvonly(options,
        function(err)
    {
        if (err) {
        console.error("[startWithOutHost/WebRtcPeerRecvonly] Error in constructor: "
            + explainUserMediaError(err));
        return;
        }

        console.log("[startWithOutHost/WebRtcPeerRecvonly] Created; Get SDP Offer From server");
        webRtcPeer.generateOffer((err, sdpOffer) => {
        if (err) {
            console.error("[startWithOutHost/WebRtcPeerRecvonly/generateOffer] " + err);
            return;
        }
        sendMessage({
            id: 'YOTUBE_SDP_OFFER',
            sdpOffer: sdpOffer,
        });

        console.log("[startWithOutHost/WebRtcPeerRecvonly/generateOffer] Done!");
        });
    });
    },

    stop :function() {
    console.log("[stop]");

    sendMessage({
        id: 'STOP',
    });

    if (webRtcPeer) {
        webRtcPeer.dispose();
        webRtcPeer = null;
    }

    hideSpinner(videoRtp);
    }
   
  },
    created() {
        ws = new WebSocket('wss://i6a505.p.ssafy.io:8443/anymeetnsing/kurento');
        // ws = new WebSocket('wss://localhost:8443/anymeetnsing/kurento');
        console.log('웹소켓', ws);
        var ref = this;

        ws.addEventListener('message', function (message) {
            console.log(message)
            var parsedMessage = JSON.parse(message.data);
            console.info('Received message: ' + message.data);

            switch (parsedMessage.id) {
            case 'existingParticipants':
                onExistingParticipants(parsedMessage);
                // 방을 생성한 호스트는 start(), 그 외에는 startWithOutHost() 실행
                if(parsedMessage.data.length == 0) {
                    ref.start();
                } else {
                    ref.startWithOutHost();
                }
                break;
            case 'newParticipantArrived':
                onNewParticipant(parsedMessage);
                break;
            case 'participantLeft':
                onParticipantLeft(parsedMessage);
                break;
            case 'receiveVideoAnswer':
                receiveVideoResponse(parsedMessage);
                break;
            case 'iceCandidate':
                participants[parsedMessage.name].rtcPeer.addIceCandidate(parsedMessage.candidate, function (error) {
                            if (error) {
                            console.error("Error adding candidate: " + error);
                            return;
                            }
                    });
                    break;
            case 'PROCESS_SDP_ANSWER':
                handleProcessSdpAnswer(parsedMessage);
                break;
            case 'ADD_ICE_CANDIDATE':
                handleAddIceCandidate(parsedMessage);
                break;
            case 'END_PLAYBACK':
                handleEndPlayback(parsedMessage);
                break;
            case 'ERROR':
                handleError(parsedMessage);
                break;
            
            default:
                console.error('Unrecognized message', parsedMessage);
            }
        })
    },
    mounted() {
        this.$nextTick(function () {
            name = this.$store.state.mynickname
            var room = this.$store.state.enterroom.id

            document.getElementById('room').style.display = 'block';

            var message = {
                id : 'joinRoom',
                name : name,
                room : room,
            }
            sendMessageOnMounted(message);
        });

        videoRtp = document.getElementById('videoRtp');
        console.log("created_uisetsetate");
    },

    beforeDestroy: function(){
        
        sendMessage({
            id : 'leaveRoom'
        });
        for ( var key in participants) {
            participants[key].dispose();
        }
        
        ws.close();
            
    },
    computed: {
        searchmusics: function(){
        return this.$store.state.search_music
        },
        musiclists: function(){
        return this.$store.state.music_list
        },
    }
}
</script>

<style scoped>
.scroll99 {
  overflow-x: scroll;
  overflow-y: hidden;
}

li::marker {
  display: none !important;
}


/* 스크롤바 설정*/ 
.type99::-webkit-scrollbar{
   /* 스크롤바 막대 너비 설정 */ 
   width: 2px; 
   } 
   /* 스크롤바 막대 설정*/ 
.type99::-webkit-scrollbar-thumb{ 
    /* 스크롤바 막대 높이 설정 */ 
    height: 13%;
    background-color: rgba(255, 255, 255, 0.205);
     /* 스크롤바 둥글게 설정 */ 
     border-radius: 10px; 
     } 
  /* 스크롤바 뒷 배경 설정*/ 
  .type99::-webkit-scrollbar-track
  { 
    background-color: rgba(0,0,0,0); 
    display: none;
  }

.container-back {
    background: rgba(0, 0, 0, 0.6);
    /* backdrop-filter: blur(3px); */
    padding: 30px;
    border-radius: 10px;
    margin: 30px;
}

.con-span {
    color: white;
}

.room-tag1 {
    background: rgba(0, 0, 0, 0.6);
    /* backdrop-filter: blur(3px); */
    border-top-left-radius: 10px;
    border-top-right-radius: 10px;
    border: none;
    color: white;
    padding: 10px;
    width: 150px;
}
.room-tag2 {
    background: rgba(0, 0, 0, 0.2);
    /* backdrop-filter: blur(3px); */
    border-top-left-radius: 10px;
    border-top-right-radius: 10px;
    border: none;
    color: white;
    padding: 10px;
    width: 150px;
}

.room-search-back {
    background: rgba(0, 0, 0, 0.6);
    /* backdrop-filter: blur(3px); */
    padding: 30px;
    border-radius: 10px;
    height: 412.5px;
}

.block {
    display: block;
}

body {
    font: 13px/20px "Lucida Grande", Tahoma, Verdana, sans-serif;
    color: #404040;
    background: #0ca3d2;
}

input[type=checkbox], input[type=radio] {
    border: 1px solid #c0c0c0;
    margin: 0 0.1em 0 0;
    padding: 0;
    font-size: 16px;
    line-height: 1em;
    width: 1.25em;
    height: 1.25em;
    background: #fff;
    background: -webkit-gradient(linear, 0% 0%, 0% 100%, from(#ededed),
        to(#fbfbfb));
    -webkit-appearance: none;
    -webkit-box-shadow: 1px 1px 1px #fff;
    -webkit-border-radius: 0.25em;
    vertical-align: text-top;
    display: inline-block;
}

input[type=radio] {
    -webkit-border-radius: 2em; /* Make radios round */
}

input[type=checkbox]:checked::after {
    content: "�쐱";
    display: block;
    text-align: center;
    font-size: 16px;
    height: 16px;
    line-height: 18px;
}

input[type=radio]:checked::after {
    content: "�뿈";
    display: block;
    height: 16px;
    line-height: 15px;
    font-size: 20px;
    text-align: center;
}

select {
    border: 1px solid #D0D0D0;
    background: url(http://www.quilor.com/i/select.png) no-repeat right
        center, -webkit-gradient(linear, 0% 0%, 0% 100%, from(#fbfbfb),
        to(#ededed));
    background: -moz-linear-gradient(19% 75% 90deg, #ededed, #fbfbfb);
    -webkit-box-shadow: 0 1px 2px rgba(0, 0, 0, .05);
    -moz-box-shadow: 0 1px 2px rgba(0, 0, 0, .05);
    -webkit-box-shadow: 0 1px 2px rgba(0, 0, 0, .05);
    color: #444;
}

.container {
    margin: 50px auto;
    width: 640px;
}

.join {
    position: relative;
    margin: 0 auto;
    padding: 20px 20px 20px;
    width: 310px;
    background: white;
    border-radius: 3px;
    -webkit-box-shadow: 0 0 200px rgba(255, 255, 255, 0.5), 0 1px 2px
        rgba(0, 0, 0, 0.3);
    box-shadow: 0 0 200px rgba(255, 255, 255, 0.5), 0 1px 2px
        rgba(0, 0, 0, 0.3);
    /*Transition*/
    -webkit-transition: all 0.3s linear;
    -moz-transition: all 0.3s linear;
    -o-transition: all 0.3s linear;
    transition: all 0.3s linear;
}

.join:before {
    content: '';
    position: absolute;
    top: -8px;
    right: -8px;
    bottom: -8px;
    left: -8px;
    z-index: -1;
    background: rgba(0, 0, 0, 0.08);
    border-radius: 4px;
}

.join h1 {
    margin: -20px -20px 21px;
    line-height: 40px;
    font-size: 15px;
    font-weight: bold;
    color: #555;
    text-align: center;
    text-shadow: 0 1px white;
    background: #f3f3f3;
    border-bottom: 1px solid #cfcfcf;
    border-radius: 3px 3px 0 0;
    background-image: -webkit-linear-gradient(top, whiteffd, #eef2f5);
    background-image: -moz-linear-gradient(top, whiteffd, #eef2f5);
    background-image: -o-linear-gradient(top, whiteffd, #eef2f5);
    background-image: linear-gradient(to bottom, whiteffd, #eef2f5);
    -webkit-box-shadow: 0 1px whitesmoke;
    box-shadow: 0 1px whitesmoke;
}

.join p {
    margin: 20px 0 0;
}

.join p:first-child {
    margin-top: 0;
}

.join input[type=text], .join input[type=password] {
    width: 278px;
}

.join p.submit {
    text-align: center;
}

:-moz-placeholder {
    color: #c9c9c9 !important;
    font-size: 13px;
}

::-webkit-input-placeholder {
    color: #ccc;
    font-size: 13px;
}

input {
    font-family: 'Lucida Grande', Tahoma, Verdana, sans-serif;
    font-size: 14px;
}

input[type=text], input[type=password] {
    margin: 5px;
    padding: 0 10px;
    width: 200px;
    height: 34px;
    color: #404040;
    background: white;
    border: 1px solid;
    border-color: #c4c4c4 #d1d1d1 #d4d4d4;
    border-radius: 2px;
    outline: 5px solid #eff4f7;
    -moz-outline-radius: 3px;
    -webkit-box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.12);
    box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.12);
}

input[type=text]:focus, input[type=password]:focus {
    border-color: #7dc9e2;
    outline-color: #dceefc;
    outline-offset: 0;
}

input[type=button], input[type=submit] {
    padding: 0 18px;
    height: 29px;
    font-size: 12px;
    font-weight: bold;
    color: #527881;
    text-shadow: 0 1px #e3f1f1;
    background: #cde5ef;
    border: 1px solid;
    border-color: #b4ccce #b3c0c8 #9eb9c2;
    border-radius: 16px;
    outline: 0;
    -webkit-box-sizing: content-box;
    -moz-box-sizing: content-box;
    box-sizing: content-box;
    background-image: -webkit-linear-gradient(top, #edf5f8, #cde5ef);
    background-image: -moz-linear-gradient(top, #edf5f8, #cde5ef);
    background-image: -o-linear-gradient(top, #edf5f8, #cde5ef);
    background-image: linear-gradient(to bottom, #edf5f8, #cde5ef);
    -webkit-box-shadow: inset 0 1px white, 0 1px 2px rgba(0, 0, 0, 0.15);
    box-shadow: inset 0 1px white, 0 1px 2px rgba(0, 0, 0, 0.15);
}

input[type=button]:active, input[type=submit]:active {
    background: #cde5ef;
    border-color: #9eb9c2 #b3c0c8 #b4ccce;
    -webkit-box-shadow: inset 0 0 3px rgba(0, 0, 0, 0.2);
    box-shadow: inset 0 0 3px rgba(0, 0, 0, 0.2);
}

.lt-ie9 input[type=text], .lt-ie9 input[type=password] {
    line-height: 34px;
}

#room {
    width: 100%;
    text-align: center;
}

.participant {
    border-radius: 4px;
    /* border: 2px groove; */
    margin-left: 5;
    margin-right: 5;
    width: 150;
    text-align: center;
    overflow: hide;
    float: left;
    padding: 5px;
    border-radius: 10px;
    -webkit-box-shadow: 0 0 200px rgba(255, 255, 255, 0.5), 0 1px 2px
        rgba(0, 0, 0, 0.3);
    box-shadow: 0 0 200px rgba(255, 255, 255, 0.5), 0 1px 2px
        rgba(0, 0, 0, 0.3);
    /*Transition*/
    -webkit-transition: all 0.3s linear;
    -moz-transition: all 0.3s linear;
    -o-transition: all 0.3s linear;
    transition: all 0.3s linear;
}

.participant:before {
    content: '';
    position: absolute;
    top: -8px;
    right: -8px;
    bottom: -8px;
    left: -8px;
    z-index: -1;
    background: rgba(0, 0, 0, 0.08);
    border-radius: 4px;
}

.participant:hover {
    opacity: 1;
    background-color: 0A33B6;
    -webkit-transition: all 0.5s linear;
    transition: all 0.5s linear;
}

.participant video, .participant.main video {
    width: 100% !important;
    height: auto !important;
}

.participant span {
    color: PapayaWhip;
}

.participant.main {
    width: 20%;
    margin: 0 auto;
}

.participant.main video {
    height: auto;
}

.animate {
    -webkit-animation-duration: 0.5s;
    -webkit-animation-fill-mode: both;
    -moz-animation-duration: 0.5s;
    -moz-animation-fill-mode: both;
    -o-animation-duration: 0.5s;
    -o-animation-fill-mode: both;
    -ms-animation-duration: 0.5s;
    -ms-animation-fill-mode: both;
    animation-duration: 0.5s;
    animation-fill-mode: both;
}

.removed {
    -webkit-animation: disapear 1s;
    -webkit-animation-fill-mode: forwards;
    animation: disapear 1s;
    animation-fill-mode: forwards;
}

@-webkit-keyframes 
disapear { 
    50% 
    {
        -webkit-transform: translateX(-5%);
        transform: translateX(-5%);
    }

    100%
    {
    -webkit-transform: translateX(200%);
    transform: translateX(200%);
    }
}

@keyframes 
disapear {
    50% 
    {
    -webkit-transform: translateX(-5%);
    transform: translateX(-5%);
    }
    100%
    {
    -webkit-transform: translateX(200%);
    transform: translateX(200%);
    }
}
a.hovertext {
    position: relative;
    width: 500px;
    text-decoration: none !important;
    text-align: center;
}

a.hovertext:after {
    content: attr(title);
    position: absolute;
    left: 0;
    bottom: 0;
    padding: 0.5em 20px;
    width: 460px;
    background: rgba(0, 0, 0, 0.8);
    text-decoration: none !important;
    color: #fff;
    opacity: 0;
    -webkit-transition: 0.5s;
    -moz-transition: 0.5s;
    -o-transition: 0.5s;
    -ms-transition: 0.5s;
}

a.hovertext:hover:after, a.hovertext:focus:after {
    opacity: 1.0;
}

video, #console, #kmsInfoText {
	display: block;
	font-size: 14px;
	line-height: 1.42857143;
	color: #555;
	background-color: #fff;
	background-image: none;
	border: 1px solid #ccc;
	border-radius: 4px;
	-webkit-box-shadow: inset 0 1px 1px rgba(0, 0, 0, .075);
	box-shadow: inset 0 1px 1px rgba(0, 0, 0, .075);
	-webkit-transition: border-color ease-in-out .15s, box-shadow
		ease-in-out .15s;
	transition: border-color ease-in-out .15s, box-shadow ease-in-out .15s;
}

.textborder {
    border: 4mm ridge rgba(170, 50, 220, .6);
}
</style>