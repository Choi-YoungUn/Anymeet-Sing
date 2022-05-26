package com.ssafy.anymeetsong.kurento;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.kurento.client.KurentoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.WebSocketSession;

public class ManageRooms {
	private final Logger log = LoggerFactory.getLogger(ManageRooms.class);
	
	@Autowired
	private KurentoClient kurento;
	
	private final ConcurrentMap<String,Room> rooms = new ConcurrentHashMap<>();
	
	public Room getRoom(String name) {
		log.debug("Searching for room {}", name);
		Room room = rooms.get(name);
		
		if(room == null) {
			log.debug("Room {} not found. Create new room", name);
			room = new Room(name, kurento.createMediaPipeline());
			rooms.put(name, room);
		}
		
		log.debug("Room {} found", name);
		
		return room;
	}
	
	// make Media Session 
	public Room getRoom(String name, WebSocketSession wss) {
		log.debug("Searching for room {}", name);
		Room room = rooms.get(name);
		
		if(room == null) {
			log.debug("Room {} not found. Create new room", name);
			room = new Room(name, kurento.createMediaPipeline());
			rooms.put(name, room);
		}
		
		log.debug("Room {} found", name);
		
		return room;
	}
	
	public void removeRoom(Room room) {
		this.rooms.remove(room.getName());
		room.close();
		
		log.info("Room {} removed and closed", room.getName());
	}
}
