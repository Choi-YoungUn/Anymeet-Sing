package com.ssafy.anymeetsong.model.service;

import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class JwtServiceImpl implements JwtService{
	private static final String SALT = "AnyMeetSing";
	private static final int EXPIRE_MIN = 600;
	private static final String HEADER = "access-token";
	
	@Override
	public <T> String create(String key, T data, String subject) {
		String jwt = Jwts.builder()
							.setHeaderParam("typ","JWT")
							.setHeaderParam("regDate",System.currentTimeMillis())
							.setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * EXPIRE_MIN))
							.setSubject(subject)
							.claim(key,data)
							.signWith(SignatureAlgorithm.HS256, this.generateKey())
							.compact();
		return jwt;
	}
	
	private byte[] generateKey() {
		byte[] key = null;
		try {
			key = SALT.getBytes("UTF-8");
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
		
		return key;
	}
	
	@Override
	public Map<String,Object> getJwt(){
		Map<String,Object> value = null;
		
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
										.getRequest();
		String jwt = request.getHeader(HEADER);
		
		try {
			Jws<Claims> claims = Jwts.parser()
										.setSigningKey(this.generateKey())
										.parseClaimsJws(jwt);
			value = claims.getBody();
		} catch(Exception e) {
			//e.printStackTrace();
			System.out.println(e.getMessage());
		}
		
		return value;
	}
	
	@Override
	public boolean isValid(String jwt) {
		if(jwt == null) return false;
		else return true;
	}
}
