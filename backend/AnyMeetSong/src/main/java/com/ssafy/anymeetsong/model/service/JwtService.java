package com.ssafy.anymeetsong.model.service;

import java.util.Map;

public interface JwtService {
	public <T> String create(String key, T data, String subject);
	public Map<String,Object> getJwt();
	public boolean isValid(String jwt);
}
