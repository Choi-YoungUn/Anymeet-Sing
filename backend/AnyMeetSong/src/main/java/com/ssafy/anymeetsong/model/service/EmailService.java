package com.ssafy.anymeetsong.model.service;

public interface EmailService {
	String sendSimpleMessage(String to, int mode) throws Exception;
	Boolean saveIdAndCodeToDB(String id, String code) throws Exception;
	int verifyCode(String id, String code) throws Exception;
}
