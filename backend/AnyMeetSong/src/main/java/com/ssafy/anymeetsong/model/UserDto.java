package com.ssafy.anymeetsong.model;

import lombok.Data;

@Data
public class UserDto {
	private String id;
	private int questionsId;
	private String question;
	private String password;
	private String nickname;
	private String phone;
	private String answer;
	private boolean isBlocked;
	private boolean isAdmin;
	private int listenCount;
	private int singingCount;
}
