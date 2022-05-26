package com.ssafy.anymeetsong.model.service;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ssafy.anymeetsong.model.mapper.RoomMapper;

@Service
public class RoomServiceImpl implements RoomService {
	
	@Autowired
	private SqlSession sqlSession;
	
	@Override
	public List<Map<String,Object>> getRoomList() throws Exception {
		return sqlSession.getMapper(RoomMapper.class).getRoomList();
	}

	@Override
	public List<Map<String,Object>> getRoomAll(Map<String,Object> map) throws Exception {
		return sqlSession.getMapper(RoomMapper.class).getRoomAll(map);
	}

	@Override
	public List<Map<String,Object>> getRoomWithPassword(Map<String,Object> map) throws Exception {
		return sqlSession.getMapper(RoomMapper.class).getRoomWithPassword(map);
	}

	@Override
	public List<Map<String,Object>> getRoomWithoutPassword(Map<String,Object> map) throws Exception {
		return sqlSession.getMapper(RoomMapper.class).getRoomWithoutPassword(map);
	}
	public Map<String,Object> getRoomWithId(String id) throws Exception {
		return sqlSession.getMapper(RoomMapper.class).getRoomWithId(id);
	}

	@Override
	public List<Map<String,Object>> isHostThere(String id) throws Exception {
		return sqlSession.getMapper(RoomMapper.class).isHostThere(id);
	}
	
	@Override
	public String isHostThereS(String id) throws Exception {
		return sqlSession.getMapper(RoomMapper.class).isHostThereS(id);
	}
	
	@Override
	public boolean createRoom(Map<String,Object> map) throws Exception {
		return sqlSession.getMapper(RoomMapper.class).createRoom(map) == 1;
	}

	@Override
	public boolean joinRoom(Map<String, Object> inputMap) throws Exception {
		return sqlSession.getMapper(RoomMapper.class).joinRoom(inputMap) == 1;		
	}
	
	@Override
	public boolean deleteRoom(String id) throws Exception {
		return sqlSession.getMapper(RoomMapper.class).deleteRoom(id) == 1;
	}
	
	@Override
	public boolean deleteRoomParticipants(String id) throws Exception {
		return sqlSession.getMapper(RoomMapper.class).deleteRoomParticipants(id) == 1;
	}
	
	@Override
	public String findHost(String id) throws Exception {
		return sqlSession.getMapper(RoomMapper.class).findHost(id);
	}
	
	@Override
	public boolean leave(Map<String,Object> map) throws Exception {
		return sqlSession.getMapper(RoomMapper.class).leave(map) == 1;
	}

	@Override
	public boolean leaveAll(Map<String,Object> map) throws Exception {
		return sqlSession.getMapper(RoomMapper.class).leaveAll(map) == 1;
	}

	
	@Override
	public boolean updateRoom(Map<String,Object> map) throws Exception {
		return sqlSession.getMapper(RoomMapper.class).updateRoom(map) == 1;
	}

	@Override
	public Map<String,Object> update(Map<String,Object> map) throws Exception {
		return sqlSession.getMapper(RoomMapper.class).update(map);
	}
}
