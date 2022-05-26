package com.ssafy.anymeetsong.model.service;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ssafy.anymeetsong.model.mapper.AdminMapper;

@Service
public class AdminServiceImpl implements AdminService{
	
	@Autowired
	private SqlSession sqlSession;
	
	@Override
	public List<Map<String,Object>> getUserList() throws Exception {
		return sqlSession.getMapper(AdminMapper.class).getUserList();
	}
	
	@Override
	public int updateNickname(Map<String,String> map) throws Exception {
		if(map.get("id") == null || map.get("nickname") == null) return 0;
		return sqlSession.getMapper(AdminMapper.class).updateNickname(map);
	}
	
	@Override
	public int resetPassword(Map<String,String> map) throws Exception {
		if(map.get("id") == null) return 0;
		return sqlSession.getMapper(AdminMapper.class).resetPassword(map);
	}
	
	@Override
	public int updateBan(Map<String,Object> map) throws Exception {
		if(map.get("id") == null || map.get("isBlocked") == null) return 0;
		return sqlSession.getMapper(AdminMapper.class).updateBan(map);
	}
	
	@Override
	public int deleteUser(Map<String,String> map) throws Exception {
		if(map.get("id") == null) return 0;
		return sqlSession.getMapper(AdminMapper.class).deleteUser(map);
	}
	
	@Override
	public List<Map<String,Object>> getRoomList() throws Exception {
		return sqlSession.getMapper(AdminMapper.class).getRoomList();
	}
	
	@Override
	public int removeParticipants(Map<String,Integer> map) throws Exception {
		if(map.get("id") == null) return 0;
		return sqlSession.getMapper(AdminMapper.class).removeParticipants(map);
	}
	
	@Override
	public int deleteRoom(Map<String,Integer> map) throws Exception {
		if(map.get("id") == null) return 0;
		return sqlSession.getMapper(AdminMapper.class).deleteRoom(map);
	}
}
