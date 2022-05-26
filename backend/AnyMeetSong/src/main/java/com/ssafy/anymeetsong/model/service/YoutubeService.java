package com.ssafy.anymeetsong.model.service;

import java.io.IOException;
import java.util.List;

import com.ssafy.anymeetsong.model.SongInfoDto;

public interface YoutubeService {
	public List<SongInfoDto> searchByTitleFromYoutube(String token) throws IOException;
}
