package com.ssafy.anymeetsong.model.service;

import java.util.List;
import java.util.Map;

import com.ssafy.anymeetsong.model.QuestionsDto;
import com.ssafy.anymeetsong.model.UserDto;

public interface UserService {
	
	public boolean regist(UserDto userDto) throws Exception;
	public List<QuestionsDto> getQuestionList() throws Exception;
	
	public Map<String,Object> checkValid(Map<String,String> map) throws Exception;
	public Map<String,String> findByInfo(Map<String,Object> map) throws Exception;
	public Map<String,Object> findById(Map<String,Object> map) throws Exception;
	public int changePassword(Map<String,Object> map) throws Exception;
	
	public Map<String,Object> getUser(String id) throws Exception;
	public int updateUser(Map<String,Object> map) throws Exception;	
	public boolean deleteUser(String id) throws Exception;
	public String checkPw(String id) throws Exception;
	public int updatePassword(Map<String,Object> map) throws Exception;	
	
}
