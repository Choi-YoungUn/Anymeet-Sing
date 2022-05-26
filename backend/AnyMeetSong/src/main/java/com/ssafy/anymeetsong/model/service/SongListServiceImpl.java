package com.ssafy.anymeetsong.model.service;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ssafy.anymeetsong.model.SongInfoDto;
import com.ssafy.anymeetsong.model.mapper.SongListMapper;

@Service
public class SongListServiceImpl implements SongListService {

	@Autowired
	private SqlSession sqlSession;
	
	@Override
	public List<SongInfoDto> getSongList(String roomId) throws Exception {
		return sqlSession.getMapper(SongListMapper.class).getSongList(roomId);
	}

	@Override
	public int addSongToList(Map<String,String> map) throws Exception {
		return sqlSession.getMapper(SongListMapper.class).addSong(map);
	}

	@Override
	public int deleteSongFromList(String roomId, String songId) throws Exception {
		return sqlSession.getMapper(SongListMapper.class).deleteSong(roomId, songId);
	}

}
