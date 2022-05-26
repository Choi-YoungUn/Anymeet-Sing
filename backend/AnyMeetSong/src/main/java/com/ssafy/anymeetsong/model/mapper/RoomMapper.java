package com.ssafy.anymeetsong.model.mapper;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RoomMapper {
	public List<Map<String,Object>> getRoomList() throws SQLException;
	public List<Map<String,Object>> getRoomAll(Map<String,Object> map) throws SQLException;
	public List<Map<String,Object>> getRoomWithPassword(Map<String,Object> map) throws SQLException;
	public List<Map<String,Object>> getRoomWithoutPassword(Map<String,Object> map) throws SQLException;
	public Map<String,Object> getRoomWithId(String id) throws SQLException;

	
	public List<Map<String,Object>> isHostThere(String id) throws SQLException;
	public String isHostThereS(String id) throws SQLException;

	
	public int createRoom(Map<String,Object> map) throws SQLException;
	public int joinRoom(Map<String, Object> inputMap) throws SQLException;
	public int deleteRoom(String id) throws SQLException;
	public int deleteRoomParticipants(String id) throws SQLException;
	public String findHost(String id) throws SQLException;
	public int leave(Map<String, Object> inputMap) throws SQLException;
	public int leaveAll(Map<String, Object> inputMap) throws SQLException;

	public int updateRoom(Map<String,Object> map) throws SQLException;

	public Map<String,Object> update(Map<String,Object> map) throws SQLException;

}
