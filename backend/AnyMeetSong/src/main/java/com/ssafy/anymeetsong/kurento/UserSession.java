package com.ssafy.anymeetsong.kurento;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.kurento.client.Continuation;
import org.kurento.client.EventListener;
import org.kurento.client.IceCandidate;
import org.kurento.client.IceCandidateFoundEvent;
import org.kurento.client.MediaPipeline;
import org.kurento.client.RtpEndpoint;
import org.kurento.client.WebRtcEndpoint;
import org.kurento.jsonrpc.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.gson.JsonObject;

public class UserSession implements Closeable{
	public static final Logger log = LoggerFactory.getLogger(UserSession.class);
	
	private final String name;
	private final String roomName;
	
	private final WebSocketSession wss;
	
	private MediaPipeline pipeline;
	

	private RtpEndpoint rtpEp;
	private WebRtcEndpoint outgoingMedia;
	public final ConcurrentMap<String, WebRtcEndpoint> incomingMedia = new ConcurrentHashMap<>();
	
	public UserSession(final String name, String roomName, final WebSocketSession wss, MediaPipeline pipeline, WebRtcEndpoint outgoingMedia) {
		this.name = name;
		this.roomName = roomName;
		this.wss = wss;
		this.pipeline = pipeline;
		this.outgoingMedia = outgoingMedia;
	}
	
	//init
	public UserSession(final String name, String roomName, final WebSocketSession wss, MediaPipeline pipeline) {
		this.name = name;
		this.roomName = roomName;
		this.wss = wss;
		this.pipeline = pipeline;
		this.outgoingMedia = new WebRtcEndpoint.Builder(pipeline).build();
		
		//Interactive Connectivity Establishment : 두 단말이 서로 통신 가능한 최적 경로 찾게 도와주는 프레임워크
		this.outgoingMedia.addIceCandidateFoundListener(new EventListener<IceCandidateFoundEvent>() {
			@Override
			public void onEvent(IceCandidateFoundEvent event) {
				JsonObject response = new JsonObject();
		    	response.addProperty("id", "iceCandidate");
		    	response.addProperty("name", name);
		    	response.add("candidate", JsonUtils.toJsonObject(event.getCandidate()));
		        
		    	try {
		    		synchronized (wss) {
		    			wss.sendMessage(new TextMessage(response.toString()));
		    		}
		    	} catch (IOException e) {
		    		log.debug(e.getMessage());
		    	}
			}
		});
	}
	
	public String getName() {
		return name;
	}
	
	public String getRoomName() {
		return roomName;
	}
	
	public WebSocketSession getSession() {
		return wss;
	}
	
	public WebRtcEndpoint getOutgoingWebRtcPeer() {
		return outgoingMedia;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj == null || !(obj instanceof UserSession)) {
			return false;
		}
		
		UserSession other = (UserSession) obj;
		boolean eq = name.equals(other.name);
		eq &= roomName.equals(other.roomName);
		
		return eq;
	}
	
	@Override
	public int hashCode() {
		int res = 1;
		res = 31 * res + name.hashCode();
		res = 31 * res + roomName.hashCode();
		
		return res;
	}
	
	public void sendMessage(JsonObject msg) throws IOException {
		log.debug("User {}: Send msg {}", name, msg);
		
		synchronized(wss) {
			wss.sendMessage(new TextMessage(msg.toString()));
		}
	}
	
	public void addCandidate(IceCandidate candidate, String name) {
		if(this.name.compareTo(name) == 0) {
			outgoingMedia.addIceCandidate(candidate);
		}
		else {
			WebRtcEndpoint webRtc = incomingMedia.get(name);
			
			if(webRtc != null) {
				webRtc.addIceCandidate(candidate);
			}
		}
	}
	
	public WebRtcEndpoint getEndpointForUser(final UserSession sender) {
		if(sender.getName().equals(name)) {
			log.debug("Participant {}: configuring loopback", this.name);
			
			return outgoingMedia;
		}
		
		log.debug("Participant {}: receiving video from {}", this.name, sender.getName());
		WebRtcEndpoint incoming = incomingMedia.get(sender.getName());
		
		if(incoming == null) { 
			log.debug("Participant {}: create new endpoint for {}", this.name, sender.getName());
			incoming = new WebRtcEndpoint.Builder(pipeline).build();
			
			incoming.addIceCandidateFoundListener(new EventListener<IceCandidateFoundEvent>() {
				@Override
				public void onEvent(IceCandidateFoundEvent event) {
					JsonObject response = new JsonObject();
			    	response.addProperty("id", "iceCandidate");
			    	response.addProperty("name", sender.getName());
			    	response.add("candidate", JsonUtils.toJsonObject(event.getCandidate()));
			        
			    	try {
			    		synchronized (wss) {
			    			wss.sendMessage(new TextMessage(response.toString()));
			    		}
			    	} catch (IOException e) {
			    		log.debug(e.getMessage());
			    	}
				}
			});
			
			incomingMedia.put(sender.getName(), incoming);
		}
		log.debug("Participant {}: obtained endpoint for {}", this.name, sender.getName());
		sender.getOutgoingWebRtcPeer().connect(incoming);
		
		return incoming;
	}
	
	//SDP : Session Description Protocol 스트리밍 미디어의 초기화 인수 기술
	public void receiveVideoFrom(UserSession sender, String sdpOffer) throws IOException {
		log.info("User {}: connect with {}, in room {}", this.name, sender.getName(), this.roomName);
		log.trace("User {}: SdpOffer for {} is {}", this.name, sender.getName(), sdpOffer);
		
		final String ipSdpAnswer = this.getEndpointForUser(sender).processOffer(sdpOffer);
		final JsonObject scParams = new JsonObject();
		
		scParams.addProperty("id", "receiveVideoAnswer");
		scParams.addProperty("name", sender.getName());
		scParams.addProperty("sdpAnswer", ipSdpAnswer);
		
		log.trace("User {}: SdpAnswer for {} is {}", this.name, sender.getName(), ipSdpAnswer);
		this.sendMessage(scParams);
		
		log.debug("gather candidates");
		this.getEndpointForUser(sender).gatherCandidates();
	}
		
	public ConcurrentMap<String, WebRtcEndpoint> getIncomingMedia() {
		return incomingMedia;
	}

	public void cancelVideoFrom(final String senderName) {
		log.debug("Participant {}: canceling video reception from {}", this.name, senderName);
		final WebRtcEndpoint incoming = incomingMedia.remove(senderName);
		
		log.debug("Participant {}: removing endpoint for {}", this.name, senderName);
		incoming.release(new Continuation<Void>(){
			@Override
			public void onSuccess(Void result) throws Exception {
				log.trace("Participant {}: released successfully incoming Endpoint for {}", 
						UserSession.this.name, senderName);
			}
			
			@Override
			public void onError(Throwable cause) throws Exception {
				log.warn("Participant {}: could not release incoming Endpoint for {}",
						UserSession.this.name, senderName);
			}
		});
	}
	
	public void cancelVideoFrom(final UserSession sender) {
		this.cancelVideoFrom(sender.getName());
	}
	
	@Override
	public void close() throws IOException {
		log.debug("Participant {}: releasing resources", this.name);
		
		for(final String remoteParticipantName : incomingMedia.keySet()) {
			log.trace("Participant {}: released incoming endpoint for {}",
					this.name, remoteParticipantName);
			final WebRtcEndpoint ep = this.incomingMedia.get(remoteParticipantName);
			
			ep.release(new Continuation<Void>() {
				@Override
				public void onSuccess(Void result) throws Exception {
					log.trace("Participant {}: released successfully incoming Endpoint for {}", 
							UserSession.this.name, remoteParticipantName);
				}
				
				@Override
				public void onError(Throwable cause) throws Exception {
					log.warn("Participant {}: could not release incoming Endpoint for {}",
							UserSession.this.name, remoteParticipantName);
				}
			});
		}
		
		outgoingMedia.release(new Continuation<Void>() {
			@Override
			public void onSuccess(Void result) throws Exception {
				log.trace("Participant {}: released successfully outgoing Endpoint", 
						UserSession.this.name);
			}
			
			@Override
			public void onError(Throwable cause) throws Exception {
				log.warn("Participant {}: could not release outgoing Endpoint",
						UserSession.this.name);
			}
		});
	}

	public RtpEndpoint getRtpEp() {
		return rtpEp;
	}

	public void setRtpEp(RtpEndpoint rtpEp) {
		this.rtpEp = rtpEp;
	}
	
	public MediaPipeline getPipeline() {
		return pipeline;
	}
	
	public void setPipeline(MediaPipeline pipeline) {
		this.pipeline = pipeline;
	}
	
	public WebRtcEndpoint getOutgoingMedia() {
		return outgoingMedia;
	}

	public void setOutgoingMedia(WebRtcEndpoint outgoingMedia) {
		this.outgoingMedia = outgoingMedia;
	}
}
