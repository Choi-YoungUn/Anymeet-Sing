package com.ssafy.anymeetsong.model.service;

import java.util.List;
import java.util.Map;

public interface AdminService {
	public List<Map<String,Object>> getUserList() throws Exception;
	public int updateNickname(Map<String,String> map) throws Exception;
	public int resetPassword(Map<String,String> map) throws Exception;
	public int updateBan(Map<String,Object> map) throws Exception;
	public int deleteUser(Map<String,String> map) throws Exception;
	public List<Map<String,Object>> getRoomList() throws Exception;
	public int removeParticipants(Map<String,Integer> map) throws Exception;
	public int deleteRoom(Map<String,Integer> map) throws Exception;
}
