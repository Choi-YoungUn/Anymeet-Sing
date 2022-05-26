package com.ssafy.anymeetsong.model.service;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ssafy.anymeetsong.model.QuestionsDto;
import com.ssafy.anymeetsong.model.UserDto;
import com.ssafy.anymeetsong.model.mapper.UserMapper;

@Service
public class UserServiceImpl implements UserService {
	
	@Autowired
	private SqlSession sqlSession;
	
	@Override
	public boolean regist(UserDto userDto) throws Exception {
		return sqlSession.getMapper(UserMapper.class).regist(userDto) == 1;
	}

	@Override
	public List<QuestionsDto> getQuestionList() throws Exception {
		return sqlSession.getMapper(UserMapper.class).getQuestionList();
	}

	@Override
	public Map<String,Object> checkValid(Map<String,String> map) throws Exception {
		if(map.get("id") == null) return null;
		return sqlSession.getMapper(UserMapper.class).checkValid(map);
	}
	
	@Override
	public Map<String,String> findByInfo(Map<String,Object> map) throws Exception {
		if(map.get("phone") == null || map.get("questionsId") == null || map.get("answer") == null) return null;
		return sqlSession.getMapper(UserMapper.class).findByInfo(map);
	}
	
	@Override
	public Map<String,Object> findById(Map<String,Object> map) throws Exception {
		if(map.get("id") == null || map.get("questionsId") == null || map.get("answer") == null) return null;
		return sqlSession.getMapper(UserMapper.class).findById(map);
	}
	
	@Override
	public int changePassword(Map<String,Object> map) throws Exception {
		if(map.get("id") == null || map.get("questionsId") == null || map.get("answer") == null) return 0;
		return sqlSession.getMapper(UserMapper.class).changePassword(map);
	}
	
	@Override
	public Map<String,Object> getUser(String id) throws Exception {
		return sqlSession.getMapper(UserMapper.class).getUser(id);
	}
	
	@Override
	public int updateUser(Map<String,Object> map) throws Exception {
		return sqlSession.getMapper(UserMapper.class).updateUser(map);
	}

	@Override
	public int updatePassword(Map<String,Object> map) throws Exception {
		return sqlSession.getMapper(UserMapper.class).updatePassword(map);
	}
	
	@Override
	public boolean deleteUser(String id) throws Exception {
		return sqlSession.getMapper(UserMapper.class).deleteUser(id) == 1;
	}
	
	public String checkPw(String id) throws Exception {
		return sqlSession.getMapper(UserMapper.class).checkPw(id);
	}
}
