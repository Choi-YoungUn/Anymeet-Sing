package com.ssafy.anymeetsong.kurento;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.socket.WebSocketSession;

public class UserParticipate {
	private final ConcurrentHashMap<String,UserSession> usersByName = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String,UserSession> usersBySessionId = new ConcurrentHashMap<>();
	
	public UserSession getByName(String name) {
		return usersByName.get(name);
	}
	
	public UserSession getBySession(WebSocketSession wss) {
		return usersBySessionId.get(wss.getId());
	}
	
	public boolean exists(String name) {
		return usersByName.keySet().contains(name);
	}
	
	public void participate(UserSession user) {
		usersByName.put(user.getName(), user);
		usersBySessionId.put(user.getSession().getId(), user);
	}
	
	public UserSession removeBySession(WebSocketSession wss) {
		final UserSession user = getBySession(wss);
		usersByName.remove(user.getName());
		usersBySessionId.remove(wss.getId());
		
		return user;
	}
}
