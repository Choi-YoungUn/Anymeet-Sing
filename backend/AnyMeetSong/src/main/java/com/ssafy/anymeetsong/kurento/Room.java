package com.ssafy.anymeetsong.kurento;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.PreDestroy;

import org.kurento.client.Continuation;
import org.kurento.client.MediaPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.WebSocketSession;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class Room implements Closeable {
	private final Logger log = LoggerFactory.getLogger(Room.class);
	
	private final String name;
	
	private final MediaPipeline pipeline;
	
	private final ConcurrentMap<String, UserSession> participants = new ConcurrentHashMap<>();
	private UserSession youtube;
	private int youtubePortA;
	private int youtubePortV;
	
	public Room(String name, MediaPipeline pipeline) {
		this.name = name;
		this.pipeline = pipeline;
		
		log.info("Room {} has been created", name);
	}
	
	public String getName() {
		return name;
	}
	
	@PreDestroy
	private void shutdown() {
		this.close();
	}
	
	public Collection<UserSession> getParticipants(){
		return participants.values();
	}
	
	public UserSession getParticipant(String userName) {
		return participants.get(userName);
	}
	
	private Collection<String> joinRoom(UserSession newParticipant) throws IOException {
		final JsonObject newParticipantMsg = new JsonObject();
		
		newParticipantMsg.addProperty("id", "newParticipantArrived");
		newParticipantMsg.addProperty("name", newParticipant.getName());
		
		final List<String> participantsList = new ArrayList<>(participants.values().size());
		log.debug("Room {}: notifying other participants of new participant {}",
				name, newParticipant.getName());
		
		for(final UserSession participant : participants.values()) {
			try {
				participant.sendMessage(newParticipantMsg);
			} catch (IOException e) {
				log.debug("Room {}: participant {} could not be notified", name, participant.getName(), e);
			}
			
			participantsList.add(participant.getName());
		}
		
		return participantsList;
	}
	
	private void removeParticipant(String name) throws IOException {
		participants.remove(name);
		
		log.debug("Room {}: notifying other participants that {} is leaving the room", this.name, name);
		
		final List<String> unnotifiedParticipants = new ArrayList<>();
		final JsonObject participantLeftJson = new JsonObject();
		
		participantLeftJson.addProperty("id", "participantLeft");
		participantLeftJson.addProperty("name", name);
		
		for(final UserSession participant : participants.values()) {
			try {
				participant.cancelVideoFrom(name);
				participant.sendMessage(participantLeftJson);
			} catch (IOException e) {
				unnotifiedParticipants.add(participant.getName());
			}
		}
		
		if(!unnotifiedParticipants.isEmpty()) {
			log.debug("Room {}: The users {} could not be notified that {} left the room",
					this.name, unnotifiedParticipants, name);
		}
	}
	
	public void sendParticipantNames(UserSession user) throws IOException {
		final JsonArray participantsArray = new JsonArray();
		
		for(final UserSession participant : this.getParticipants()) {
			if(!participant.equals(user)) {
				final JsonElement participantName = new JsonPrimitive(participant.getName());
				participantsArray.add(participantName);
			}
		}
		
		final JsonObject existingParticipantsMsg = new JsonObject();
		
		existingParticipantsMsg.addProperty("id", "existingParticipants");
		existingParticipantsMsg.add("data", participantsArray);
		
		log.debug("Participant {}: sending a list of {} participants",
				 user.getName(), participantsArray.size());
		
		user.sendMessage(existingParticipantsMsg);
	}
	
	public UserSession join(String userName, WebSocketSession wss) throws IOException {
		log.info("Room {}: adding participant {}", this.name, userName);
		
		final UserSession participant = new UserSession(userName, this.name, wss, this.pipeline);
		joinRoom(participant);
		
		participants.put(participant.getName(), participant);
		sendParticipantNames(participant);
		
		return participant;
	}
	
	public void leave(UserSession user) throws IOException {
		log.debug("Participant {}: leaving Room {}", user.getName(), this.name);
		this.removeParticipant(user.getName());
		
		user.close();
	}
	
	@Override
	public void close() {
		for(final UserSession user : participants.values()) {
			try {
				user.close();
			} catch (IOException e) {
				log.debug("Room {}: could not invoke close on participant {}", this.name, user.getName(), e);
			}
		}
		
		participants.clear();
		
		pipeline.release(new Continuation<Void>() {
			@Override
			public void onSuccess(Void result) throws Exception {
				log.trace("Room {}: released pipeline", Room.this.name);
			}
			
			@Override
			public void onError(Throwable cause) throws Exception {
				log.warn("Room {}: could not release pipeline", Room.this.name);
			}
		});
		
		log.debug("Room {} closed", this.name);
	}

	public UserSession getYoutube() {
		return youtube;
	}

	public void setYoutube(UserSession youtube) {
		this.youtube = youtube;
	}

	public int getYoutubePortA() {
		return youtubePortA;
	}

	public void setYoutubePortA(int youtubePortA) {
		this.youtubePortA = youtubePortA;
	}

	public int getYoutubePortV() {
		return youtubePortV;
	}

	public void setYoutubePortV(int youtubePortV) {
		this.youtubePortV = youtubePortV;
	}
	
}
