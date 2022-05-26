package com.ssafy.anymeetsong.model.service;

import java.util.List;
import java.util.Map;

public interface RoomService {
	public boolean createRoom(Map<String,Object> map) throws Exception;
	public boolean joinRoom(Map<String, Object> inputMap) throws Exception;
	public boolean deleteRoom(String id) throws Exception;		
	public boolean deleteRoomParticipants(String id) throws Exception;		
	public String findHost(String id) throws Exception;		
	public boolean leave(Map<String,Object> map) throws Exception;
	public boolean leaveAll(Map<String,Object> map) throws Exception;

	public boolean updateRoom(Map<String,Object> map) throws Exception;	

	public Map<String,Object> update(Map<String,Object> map) throws Exception;	
	public List<Map<String,Object>> getRoomList() throws Exception;
	public List<Map<String,Object>> getRoomAll(Map<String,Object> map) throws Exception;
	public List<Map<String,Object>> getRoomWithPassword(Map<String,Object> map) throws Exception;
	public List<Map<String,Object>> getRoomWithoutPassword(Map<String,Object> map) throws Exception;
	public Map<String,Object> getRoomWithId(String id) throws Exception;

	
	
	
	public List<Map<String,Object>> isHostThere(String id) throws Exception;
	public String isHostThereS(String id) throws Exception;

	
}
