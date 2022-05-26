package com.ssafy.anymeetsong.model.mapper;

import java.sql.SQLException;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EmailMapper {

	public String findId(String id) throws SQLException;
	public int deleteIdAndCode(String id) throws SQLException;
	public int insertIdAndCode(
		@Param("id") String id, 
		@Param("code") String code
	) throws SQLException;
	public String findCodeById(String id) throws SQLException;
	public int getSecondTillCreated(String id) throws SQLException;

}
