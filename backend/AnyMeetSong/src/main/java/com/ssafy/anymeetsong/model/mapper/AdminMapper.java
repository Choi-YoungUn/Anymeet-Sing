package com.ssafy.anymeetsong.model.mapper;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminMapper {
	public List<Map<String,Object>> getUserList() throws SQLException;
	public int updateNickname(Map<String,String> map) throws SQLException;
	public int resetPassword(Map<String,String> map) throws SQLException;
	public int updateBan(Map<String,Object> map) throws SQLException;
	public int deleteUser(Map<String,String> map) throws SQLException;
	public List<Map<String,Object>> getRoomList() throws SQLException;
	public int removeParticipants(Map<String,Integer> map) throws SQLException;
	public int deleteRoom(Map<String,Integer> map) throws SQLException;
}
