package com.ssafy.anymeetsong.model.mapper;

import java.sql.SQLException;
import java.util.Map;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.ssafy.anymeetsong.model.QuestionsDto;
import com.ssafy.anymeetsong.model.UserDto;

@Mapper
public interface UserMapper {

	public int regist(UserDto userDto) throws SQLException;
	public List<QuestionsDto> getQuestionList() throws SQLException;

	public Map<String,Object> checkValid(Map<String,String> map) throws SQLException;
	public Map<String,String> findByInfo(Map<String,Object> map) throws SQLException;
	public Map<String,Object> findById(Map<String,Object> map) throws SQLException;
	public int changePassword(Map<String,Object> map) throws SQLException;
	
	public Map<String,Object> getUser(String id) throws SQLException;
	public int updateUser(Map<String,Object> map) throws SQLException;
	public int updatePassword(Map<String,Object> map) throws SQLException;
	public int deleteUser(String id) throws SQLException;
	public String checkPw(String id) throws SQLException;

}
