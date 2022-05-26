package com.ssafy.anymeetsong.model;

import java.util.List;

import lombok.Data;

@Data
public class RoomDto {
	private int id;
	private String title;
	private String password;
	private String hostId;
	private List<RoomParticipantDto> participants;
}
