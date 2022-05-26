package com.ssafy.anymeetsong.model.mapper;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.anymeetsong.model.SongInfoDto;

@Mapper
public interface SongListMapper {
	public List<SongInfoDto> getSongList(String roomId) throws SQLException;
	public int addSong(Map<String,String> map) throws SQLException;
	public int deleteSong(
		@Param("roomId") String roomId, 
		@Param("songId") String songId
	) throws SQLException;
}
