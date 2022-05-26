package com.ssafy.anymeetsong.model.service;

import java.util.List;
import java.util.Map;

import com.ssafy.anymeetsong.model.SongInfoDto;

public interface SongListService {
	public List<SongInfoDto> getSongList(String roomId) throws Exception;
	public int addSongToList(Map<String,String> map) throws Exception;
	public int deleteSongFromList(String roomId, String songId) throws Exception;
}
