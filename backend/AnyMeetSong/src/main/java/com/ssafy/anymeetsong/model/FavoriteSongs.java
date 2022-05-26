package com.ssafy.anymeetsong.model;

import lombok.Data;

@Data
public class FavoriteSongs {
	private int id;
	private String userId;
	private String songName;
	private String songUrl;
}
