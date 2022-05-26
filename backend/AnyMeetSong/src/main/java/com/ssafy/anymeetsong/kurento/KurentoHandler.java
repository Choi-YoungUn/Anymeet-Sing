package com.ssafy.anymeetsong.kurento;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.kurento.client.BaseRtpEndpoint;
import org.kurento.client.ConnectionStateChangedEvent;
import org.kurento.client.ErrorEvent;
import org.kurento.client.EventListener;
import org.kurento.client.IceCandidate;
import org.kurento.client.IceCandidateFoundEvent;
import org.kurento.client.IceComponentStateChangeEvent;
import org.kurento.client.IceGatheringDoneEvent;
import org.kurento.client.KurentoClient;
import org.kurento.client.MediaFlowInStateChangeEvent;
import org.kurento.client.MediaFlowOutStateChangeEvent;
import org.kurento.client.MediaPipeline;
import org.kurento.client.MediaStateChangedEvent;
import org.kurento.client.MediaTranscodingStateChangeEvent;
import org.kurento.client.NewCandidatePairSelectedEvent;
import org.kurento.client.OnKeySoftLimitEvent;
import org.kurento.client.RtpEndpoint;
import org.kurento.client.WebRtcEndpoint;
import org.kurento.jsonrpc.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;


public class KurentoHandler extends TextWebSocketHandler {
	private static final Logger log = LoggerFactory.getLogger(KurentoHandler.class);
	private static final Gson gson = new GsonBuilder().create();
		
	@Autowired
	private ManageRooms manageRooms;
	
	@Autowired
	private UserParticipate userParticipate;
	
	@Autowired
	private KurentoClient kurento;
	
	@Autowired
	private RunShellScript shell;
	
	private void joinRoom(JsonObject params, WebSocketSession wss) throws IOException {
		final String roomName = params.get("room").getAsString();
		final String name = params.get("name").getAsString();
		
		log.info("Participant {}: trying to join room {}", name, roomName);
		
		Room room = manageRooms.getRoom(roomName);
		
		final UserSession user = room.join(name, wss);
		
		userParticipate.participate(user);
	}
	
	private void leaveRoom(UserSession user) throws IOException {
		final Room room = manageRooms.getRoom(user.getRoomName());
		
		room.leave(user);
		
		if(room.getParticipants().isEmpty()) {
			manageRooms.removeRoom(room);
		}
	}
	
	private void removeRoom(String name) throws IOException {
		final Room room = manageRooms.getRoom(name);
		manageRooms.removeRoom(room);
	}
	
	@Override
	public void handleTextMessage(WebSocketSession wss, TextMessage msg) throws Exception {
		System.out.println("handleTextMessage 호출");
		final JsonObject jsonMessage = gson.fromJson(msg.getPayload(), JsonObject.class);
		
		final UserSession user = userParticipate.getBySession(wss);
		
		if(user != null) {
			log.debug("Incoming msg from user {}: {}", user.getName(), jsonMessage);
		}
		else {
			log.debug("Incoming msg from new user {}: {}", jsonMessage);
		}
		String sessionId = wss.getId();
	    try {
	    	
			String messageId = jsonMessage.get("id").getAsString();
			log.info(messageId);
			switch(messageId) {
				case "joinRoom":
					joinRoom(jsonMessage, wss);
					break;
				case "receiveVideoFrom":
					final String senderName = jsonMessage.get("sender").getAsString();
					final UserSession sender = userParticipate.getByName(senderName);
					final String sdpOffer = jsonMessage.get("sdpOffer").getAsString();
					
					user.receiveVideoFrom(sender, sdpOffer);
					break;
				case "leaveRoom":
					leaveRoom(user);
					break;
				case "removeRoom":
					removeRoom(jsonMessage.get("room").getAsString());
					break;
				case "onIceCandidate":
					JsonObject candidate = jsonMessage.get("candidate").getAsJsonObject();
					
					if(user != null) {
						IceCandidate cand = new IceCandidate(candidate.get("candidate").getAsString(),
								candidate.get("sdpMid").getAsString(), candidate.get("sdpMLineIndex").getAsInt());
						
						user.addCandidate(cand, jsonMessage.get("name").getAsString());
					}
					break;
				case "PROCESS_SDP_OFFER":
			          handleProcessSdpOffer(wss, jsonMessage);
			          break;
		        case "ADD_ICE_CANDIDATE":
		          handleAddIceCandidate(wss, jsonMessage);
		          break;
		        case "STOP":
		          handleStop(wss, jsonMessage);
		          break;
		        case "YOTUBE_SDP_OFFER":
		        	final String sdpOfferYt = jsonMessage.get("sdpOffer").getAsString();
		        	handleYoutubeSdp(wss, sdpOfferYt);
		        	break;
		        case "MUSIC_START":
		        	handleMusicStart(wss, jsonMessage);
		        	break;
		        case "MUSIC_STOP":
		        	handleMusicStop(wss);
		        	break;
		        default:
		          sendError(wss, "Invalid message, id: " + messageId);
		          break;
			}
	    } catch (Throwable ex) {
	        log.error("[Handler::handleTextMessage] Exception: {}, sessionId: {}",
	            ex, sessionId);
	        sendError(wss, "Exception: " + ex.getMessage());
        }
	}
	
	@Override
	public void afterConnectionClosed(WebSocketSession wss, CloseStatus status) throws Exception {
		UserSession user = userParticipate.removeBySession(wss);
		manageRooms.getRoom(user.getRoomName()).leave(user);
		stop(wss);
	}
	
	  @Override
	  public void handleTransportError(WebSocketSession session, Throwable ex)
	      throws Exception
	  {
	    log.error("[Handler::handleTransportError] Exception: {}, sessionId: {}",
	        ex, session.getId());
	  }
	
	  // PROCESS_SDP_OFFER ---------------------------------------------------------

	  private void addBaseEventListeners(final WebSocketSession session,
	      BaseRtpEndpoint baseRtpEp, final String className)
	  {
	    log.info("[Handler::addBaseEventListeners] name: {}, class: {}, sessionId: {}",
	        baseRtpEp.getName(), className, session.getId());

	    // Event: Some error happened
	    baseRtpEp.addErrorListener(new EventListener<ErrorEvent>() {
	      @Override
	      public void onEvent(ErrorEvent ev) {
	        log.error("[{}::{}] source: {}, timestamp: {}, tags: {}, description: {}, errorCode: {}",
	            className, ev.getType(), ev.getSource().getName(), ev.getTimestamp(),
	            ev.getTags(), ev.getDescription(), ev.getErrorCode());
	        stop(session);
	      }
	    });

	    // Event: Media is flowing into this sink
	    baseRtpEp.addMediaFlowInStateChangeListener(
	        new EventListener<MediaFlowInStateChangeEvent>() {
	      @Override
	      public void onEvent(MediaFlowInStateChangeEvent ev) {
	        log.info("[{}::{}] source: {}, timestamp: {}, tags: {}, state: {}, padName: {}, mediaType: {}",
	            className, ev.getType(), ev.getSource().getName(), ev.getTimestamp(),
	            ev.getTags(), ev.getState(), ev.getPadName(), ev.getMediaType());
	      }
	    });

	    // Event: Media is flowing out of this source
	    baseRtpEp.addMediaFlowOutStateChangeListener(
	        new EventListener<MediaFlowOutStateChangeEvent>() {
	      @Override
	      public void onEvent(MediaFlowOutStateChangeEvent ev) {
	        log.info("[{}::{}] source: {}, timestamp: {}, tags: {}, state: {}, padName: {}, mediaType: {}",
	            className, ev.getType(), ev.getSource().getName(), ev.getTimestamp(),
	            ev.getTags(), ev.getState(), ev.getPadName(), ev.getMediaType());
	      }
	    });

	    // Event: [TODO write meaning of this event]
	    baseRtpEp.addConnectionStateChangedListener(
	        new EventListener<ConnectionStateChangedEvent>() {
	      @Override
	      public void onEvent(ConnectionStateChangedEvent ev) {
	        log.info("[{}::{}] source: {}, timestamp: {}, tags: {}, oldState: {}, newState: {}",
	            className, ev.getType(), ev.getSource().getName(), ev.getTimestamp(),
	            ev.getTags(), ev.getOldState(), ev.getNewState());
	      }
	    });

	    // Event: [TODO write meaning of this event]
	    baseRtpEp.addMediaStateChangedListener(
	        new EventListener<MediaStateChangedEvent>() {
	      @Override
	      public void onEvent(MediaStateChangedEvent ev) {
	        log.info("[{}::{}] source: {}, timestamp: {}, tags: {}, oldState: {}, newState: {}",
	            className, ev.getType(), ev.getSource().getName(), ev.getTimestamp(),
	            ev.getTags(), ev.getOldState(), ev.getNewState());
	      }
	    });

	    // Event: This element will (or will not) perform media transcoding
	    baseRtpEp.addMediaTranscodingStateChangeListener(
	        new EventListener<MediaTranscodingStateChangeEvent>() {
	      @Override
	      public void onEvent(MediaTranscodingStateChangeEvent ev) {
	        log.info("[{}::{}] source: {}, timestamp: {}, tags: {}, state: {}, binName: {}, mediaType: {}",
	            className, ev.getType(), ev.getSource().getName(), ev.getTimestamp(),
	            ev.getTags(), ev.getState(), ev.getBinName(), ev.getMediaType());
	      }
	    });
	  }

	  private void addWebRtcEventListeners(final WebSocketSession session,
	      final WebRtcEndpoint webRtcEp)
	  {
	    log.info("[Handler::addWebRtcEventListeners] name: {}, sessionId: {}",
	        webRtcEp.getName(), session.getId());

	    // Event: The ICE backend found a local candidate during Trickle ICE
	    webRtcEp.addIceCandidateFoundListener(
	        new EventListener<IceCandidateFoundEvent>() {
	      @Override
	      public void onEvent(IceCandidateFoundEvent ev) {
	        log.debug("[WebRtcEndpoint::{}] source: {}, timestamp: {}, tags: {}, candidate: {}",
	            ev.getType(), ev.getSource().getName(), ev.getTimestamp(),
	            ev.getTags(), JsonUtils.toJsonObject(ev.getCandidate()));

	        JsonObject message = new JsonObject();
	        message.addProperty("id", "ADD_ICE_CANDIDATE");
	        message.addProperty("webRtcEpId", webRtcEp.getId());
	        message.add("candidate", JsonUtils.toJsonObject(ev.getCandidate()));
	        sendMessage(session, message.toString());
	      }
	    });

	    // Event: The ICE backend changed state
	    webRtcEp.addIceComponentStateChangeListener(
	        new EventListener<IceComponentStateChangeEvent>() {
	      @Override
	      public void onEvent(IceComponentStateChangeEvent ev) {
	        log.debug("[WebRtcEndpoint::{}] source: {}, timestamp: {}, tags: {}, streamId: {}, componentId: {}, state: {}",
	            ev.getType(), ev.getSource().getName(), ev.getTimestamp(),
	            ev.getTags(), ev.getStreamId(), ev.getComponentId(), ev.getState());
	      }
	    });

	    // Event: The ICE backend finished gathering ICE candidates
	    webRtcEp.addIceGatheringDoneListener(
	        new EventListener<IceGatheringDoneEvent>() {
	      @Override
	      public void onEvent(IceGatheringDoneEvent ev) {
	        log.debug("[WebRtcEndpoint::{}] source: {}, timestamp: {}, tags: {}",
	            ev.getType(), ev.getSource().getName(), ev.getTimestamp(),
	            ev.getTags());
	      }
	    });

	    // Event: The ICE backend selected a new pair of ICE candidates for use
	    webRtcEp.addNewCandidatePairSelectedListener(
	        new EventListener<NewCandidatePairSelectedEvent>() {
	      @Override
	      public void onEvent(NewCandidatePairSelectedEvent ev) {
	        log.info("[WebRtcEndpoint::{}] name: {}, timestamp: {}, tags: {}, streamId: {}, local: {}, remote: {}",
	            ev.getType(), ev.getSource().getName(), ev.getTimestamp(),
	            ev.getTags(), ev.getCandidatePair().getStreamID(),
	            ev.getCandidatePair().getLocalCandidate(),
	            ev.getCandidatePair().getRemoteCandidate());
	      }
	    });
	  }

	  private void initWebRtcEndpoint(final WebSocketSession session,
	      final WebRtcEndpoint webRtcEp, String sdpOffer)
	  {
	    addBaseEventListeners(session, webRtcEp, "WebRtcEndpoint");
	    addWebRtcEventListeners(session, webRtcEp);

	    /*
	    OPTIONAL: Force usage of an Application-specific STUN server.
	    Usually this is configured globally in KMS WebRTC settings file:
	    /etc/kurento/modules/kurento/WebRtcEndpoint.conf.ini

	    But it can also be configured per-application, as shown:
	    log.info("[Handler::initWebRtcEndpoint] Using STUN server: 3.36.108.155:3478");
	    webRtcEp.setStunServerAddress("3.36.108.155");
	    webRtcEp.setStunServerPort(3478);

	    */
	    
	    
	    log.info("Now Using Stun Server: " + webRtcEp.getStunServerAddress() + ":" + webRtcEp.getStunServerPort());


	    // Process the SDP Offer to generate an SDP Answer
	    String sdpAnswer = webRtcEp.processOffer(sdpOffer);

	    log.info("[Handler::initWebRtcEndpoint] name: {}, SDP Offer from browser to KMS:\n{}",
	        webRtcEp.getName(), sdpOffer);
	    log.info("[Handler::initWebRtcEndpoint] name: {}, SDP Answer from KMS to browser:\n{}",
	        webRtcEp.getName(), sdpAnswer);

	    JsonObject message = new JsonObject();
	    message.addProperty("id", "PROCESS_SDP_ANSWER");
	    message.addProperty("sdpAnswer", sdpAnswer);
	    sendMessage(session, message.toString());
	  }

	  private void startWebRtcEndpoint(WebRtcEndpoint webRtcEp)
	  {
	    webRtcEp.gatherCandidates();
	  }

	  private RtpEndpoint makeRtpEndpoint(MediaPipeline pipeline)
	  {
	      return new RtpEndpoint.Builder(pipeline).build();
	  }
	  
	  private void startRtpEndpoint(final WebSocketSession session,
	      RtpEndpoint rtpEp, Boolean useComedia)
	  {
	    log.info("[Handler::startRtpEndpoint] Configure RtpEndpoint, port discovery: {}",
	        useComedia);

	    addBaseEventListeners(session, rtpEp, "RtpEndpoint");

	    // Event: The SRTP key is about to expire
	    rtpEp.addOnKeySoftLimitListener(
	        new EventListener<OnKeySoftLimitEvent>() {
	      @Override
	      public void onEvent(OnKeySoftLimitEvent ev) {
	        log.info("[RtpEndpoint::{}] source: {}, timestamp: {}, tags: {}, mediaType: {}",
	            ev.getType(), ev.getSource(), ev.getTimestamp(), ev.getTags(),
	            ev.getMediaType());
	      }
	    });

	    // ---- RTP configuration BEGIN ----
	    // Set the appropriate values for your setup
	    String senderIp = "127.0.0.1";
	    int senderRtpPortA = 5006;
	    int senderRtpPortV = 5004;
	    int senderSsrcA = 445566;
	    int senderSsrcV = 112233;
	    String senderCname = "user@example.com";
	    String senderCodecV = "H264";
	    // String senderCodecV = "VP8";
	    // ---- RTP configuration END ----

	    /*
	    OPTIONAL: Set maximum bandwidth on reception.
	    This can be useful if there is some limitation on the incoming bandwidth
	    that the receiver is able to process.
	    */
	    // log.info("[Handler::startRtpEndpoint] Limit output bandwidth: 1024 kbps");
	    // rtpEp.setMaxVideoRecvBandwidth(1024); // In kbps (1000 bps)

	    String sdpComediaAttr = "";
	    if (useComedia) {
	      // Use Discard port (9)
	      senderRtpPortA = 9;
	      senderRtpPortV = 9;

	      // Inspired by RFC 4145 Draft 05 ("COMEDIA")
	      sdpComediaAttr = "a=direction:active\r\n";
	    }

	    Boolean useAudio = true;
	    String senderProtocol = "RTP/AVPF";
	    String sdpCryptoAttr = "";


	    String rtpSdpOffer =
	        "v=0\r\n"
	        + "o=- 0 0 IN IP4 " + senderIp + "\r\n"
	        + "s=Kurento Tutorial - RTP Receiver\r\n"
	        + "c=IN IP4 " + senderIp + "\r\n"
	        + "t=0 0\r\n";

	    if (useAudio) {
	      rtpSdpOffer +=
	          "m=audio " + senderRtpPortA + " RTP/AVPF 96\r\n"
	          + "a=rtpmap:96 opus/48000/2\r\n"
	          + "a=sendonly\r\n"
	          + sdpComediaAttr
	          + "a=ssrc:" + senderSsrcA + " cname:" + senderCname + "\r\n";
	    }

	    rtpSdpOffer +=
	        "m=video " + senderRtpPortV + " " + senderProtocol + " 103\r\n"
	        + sdpCryptoAttr
	        + "a=rtpmap:103 " + senderCodecV + "/90000\r\n"
	        + "a=rtcp-fb:103 goog-remb\r\n"
	        + "a=sendonly\r\n"
	        + sdpComediaAttr
	        + "a=ssrc:" + senderSsrcV + " cname:" + senderCname + "\r\n"
	        + "";

	    // Send the SDP Offer to KMS, and get its negotiated SDP Answer
	    String rtpSdpAnswer = rtpEp.processOffer(rtpSdpOffer);

	    log.info("[Handler::startRtpEndpoint] Fake SDP Offer from App to KMS:\n{}",
	        rtpSdpOffer);
	    log.info("[Handler::startRtpEndpoint] SDP Answer from KMS to App:\n{}",
	        rtpSdpAnswer);

	    // Parse SDP Answer
	    // NOTE: No error checking; this code assumes that the SDP Answer from KMS
	    // is always well formed.
	    Pattern p; Matcher m;

	    int kmsRtpPortA = 0;
	    int senderRtcpPortA = 0;
	    if (useAudio) {
	      p = Pattern.compile("m=audio (\\d+) RTP");
	      m = p.matcher(rtpSdpAnswer);
	      m.find();
	      kmsRtpPortA = Integer.parseInt(m.group(1));
	      senderRtcpPortA = senderRtpPortA + 1;
	    }

	    p = Pattern.compile("m=video (\\d+) RTP");
	    m = p.matcher(rtpSdpAnswer);
	    m.find();
	    int kmsRtpPortV = Integer.parseInt(m.group(1));
	    int senderRtcpPortV = senderRtpPortV + 1;

	    p = Pattern.compile("a=ssrc:(\\d+)");
	    m = p.matcher(rtpSdpAnswer);
	    m.find();

	    p = Pattern.compile("c=IN IP4 (([0-9]{1,3}\\.){3}[0-9]{1,3})");
	    m = p.matcher(rtpSdpAnswer);
	    m.find();
	    String kmsIp = m.group(1);

	    // Check if KMS accepted the use of "direction" attribute
	    useComedia = rtpSdpAnswer.contains("a=direction:passive");

	    String msgConnInfo = "SDP negotiation finished\n";
	    if (useAudio) {
	      msgConnInfo += String.format(
	          "* KMS listens for Audio RTP at port: %d\n", kmsRtpPortA);
	    }
	    msgConnInfo += String.format(
	        "* KMS listens for Video RTP at port: %d\n", kmsRtpPortV);
	    if (useAudio) {
	      msgConnInfo += String.format(
	          "* KMS expects Audio SSRC from sender: %d\n", senderSsrcA);
	    }
	    msgConnInfo += String.format(
	        "* KMS expects Video SSRC from sender: %d\n", senderSsrcV);
	    msgConnInfo += String.format("* KMS local IP address: %s\n", kmsIp);
	    if (useComedia) {
	      msgConnInfo += "* KMS will discover remote IP and port to send RTCP\n";
	    } else {
	      if (useAudio) {
	        msgConnInfo += String.format(
	            "* KMS sends Audio RTCP to: %s:%d\n", senderIp, senderRtcpPortA);
	      }
	      msgConnInfo += String.format(
	          "* KMS sends Video RTCP to: %s:%d\n", senderIp, senderRtcpPortV);
	    }
	    
	    
	    Room room = manageRooms.getRoom(userParticipate.getBySession(session).getRoomName());
	    room.setYoutubePortA(kmsRtpPortA);
	    room.setYoutubePortV(kmsRtpPortV);

	    log.info("[Handler::startRtpEndpoint] " + msgConnInfo);

	  }

	  private void handleProcessSdpOffer(final WebSocketSession session,
	      JsonObject jsonMessage)
	  {
	    // ---- Session handling

	    String sessionId = session.getId();

	    log.info("[Handler::handleStart] New user: {}", sessionId);

	    log.info("[Handler::handleStart] Create Media Pipeline");
	    final MediaPipeline pipeline = kurento.createMediaPipeline();
	    final WebRtcEndpoint webRtcEp =
	    		new WebRtcEndpoint.Builder(pipeline).build();
	    final RtpEndpoint rtpEp = makeRtpEndpoint(pipeline);
	    
	    final UserSession media = new UserSession("youtube", userParticipate.getBySession(session).getRoomName(),
	    		session, pipeline, webRtcEp);
	    media.setRtpEp(rtpEp);
	    
	    
	    Room room = manageRooms.getRoom(userParticipate.getBySession(session).getRoomName());
		
		room.setYoutube(media);
		
	    // ---- Endpoint configuration

	    rtpEp.connect(webRtcEp);

	    String sdpOffer = jsonMessage.get("sdpOffer").getAsString();
	    initWebRtcEndpoint(session, webRtcEp, sdpOffer);
	    startWebRtcEndpoint(webRtcEp);

	    Boolean useComedia = jsonMessage.get("useComedia").getAsBoolean();
	    startRtpEndpoint(session, rtpEp, useComedia);


	    // ---- Debug
	     String pipelineDot = pipeline.getGstreamerDot();
	     try (PrintWriter out = new PrintWriter("pipeline.dot")) {
	       out.println(pipelineDot);
	     } catch (IOException ex) {
	       log.error("[Handler::start] Exception: {}", ex.getMessage());
	     }
	  }

	  // ADD_ICE_CANDIDATE ---------------------------------------------------------

	  private void handleAddIceCandidate(final WebSocketSession session,
	      JsonObject jsonMessage)
	  {
		  
	    UserSession user = userParticipate.getBySession(session);
	    
	    if (user != null) {
	      JsonObject jsonCandidate = jsonMessage.get("candidate").getAsJsonObject();
	      IceCandidate candidate =
	          new IceCandidate(jsonCandidate.get("candidate").getAsString(),
	          jsonCandidate.get("sdpMid").getAsString(),
	          jsonCandidate.get("sdpMLineIndex").getAsInt());
	      
	      log.info("Candidate Info: " + candidate.getCandidate());
	      
	      WebRtcEndpoint webRtcEp = user.getOutgoingMedia();
	      if(webRtcEp != null) {
	    	  webRtcEp.addIceCandidate(candidate);
	      }
	    }
	  }
	  
	  // YOTUBE_SDP_OFFER ----------------------------------------------------------
	  
	  public void handleYoutubeSdp(final WebSocketSession session, final String sdpOffer) {
			UserSession youtubeSession = manageRooms.getRoom(userParticipate.getBySession(session).getRoomName()).getYoutube();
			  
			UserSession currSession = userParticipate.getBySession(session);
			log.debug("Participant {}: YOUTUBE: makeWebRtcEndPoint from {}", currSession.getName(), youtubeSession.getName());
			WebRtcEndpoint incoming = currSession.getIncomingMedia().get(youtubeSession.getName());
			
			if(incoming == null) { 
				log.debug("Participant {}: YOUTUBE: create new endpoint for {}", currSession.getName(), youtubeSession.getName());
				incoming = new WebRtcEndpoint.Builder(youtubeSession.getPipeline()).build();
				currSession.getIncomingMedia().put(youtubeSession.getName(), incoming);
			}
			log.debug("Participant {}: YOUTUBE: obtained endpoint for {}", currSession.getName(), youtubeSession.getName());
			initWebRtcEndpoint(session, incoming, sdpOffer);
			startWebRtcEndpoint(incoming);
			
			log.info("Set SebRTCENDPOINT ENDED");
			youtubeSession.getRtpEp().connect(incoming);
	  }

	  // STOP ----------------------------------------------------------------------

	  public void sendPlayEnd(final WebSocketSession session)
	  {
	    if (userParticipate.getBySession(session) != null) {
	      JsonObject message = new JsonObject();
	      message.addProperty("id", "END_PLAYBACK");
	      sendMessage(session, message.toString());
	    }
	  }

	  private void stop(final WebSocketSession session)
	  {
	    log.info("[Handler::stop]");

	    // Update the UI
	    sendPlayEnd(session);

	    // Remove the user session and release all resources
//	    String sessionId = session.getId();
	    
	    UserSession youtube = manageRooms.getRoom(userParticipate.getBySession(session).getRoomName()).getYoutube();
	    
	    if (youtube != null) {
	    	MediaPipeline mediaPipeline = youtube.getPipeline();
	    	if (mediaPipeline != null) {
	    		log.info("[Handler::stop] Release the Media Pipeline");
	    		mediaPipeline.release();
	    	}
	    	Room room = manageRooms.getRoom(youtube.getRoomName());
    		room.setYoutube(null);
    		log.info("###### Youtube Deleted from Room: " + room.getName() + "#######");
	    }
	  }

	  private void handleStop(final WebSocketSession session,
	      JsonObject jsonMessage)
	  {
	    stop(session);
	  }

	  // ---------------------------------------------------------------------------
	  
	  
	  // MUSIC START ---------------------------------------------------------------
	  
	  public void handleMusicStart (final WebSocketSession session, final JsonObject jsonMessage) throws Exception {
		  String videoId = jsonMessage.get("videoId").getAsString();
		  String roomId = userParticipate.getBySession(session).getRoomName();
		  Room room = manageRooms.getRoom(roomId);
		  shell.shella(room.getYoutubePortA(), room.getYoutubePortV(), videoId, roomId);
	  }
	  
	  // MUSIC STOP ----------------------------------------------------------------
	  
	  public void handleMusicStop (final WebSocketSession session) throws Exception{
		  shell.kill(userParticipate.getBySession(session).getRoomName());
	  }
	  
	  // ---------------------------------------------------------------------------
	  
	  private void sendError(final WebSocketSession session, String errMsg)
	  {
	    if (manageRooms.getRoom(userParticipate.getBySession(session).getRoomName()).getYoutube() != null) {
	      JsonObject message = new JsonObject();
	      message.addProperty("id", "ERROR");
	      message.addProperty("message", errMsg);
	      sendMessage(session, message.toString());
	    }
	  }

	  private synchronized void sendMessage(final WebSocketSession session,
	      String message)
	  {
	    if (!session.isOpen()) {
	      log.error("[Handler::sendMessage] WebSocket session is closed");
	      return;
	    }

	    try {
	      session.sendMessage(new TextMessage(message));
	    } catch (IOException ex) {
	      log.error("[Handler::sendMessage] Exception: {}", ex.getMessage());
	    }
	  }
}
