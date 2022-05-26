package com.ssafy.anymeetsong.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.anymeetsong.model.service.AdminService;
import com.ssafy.anymeetsong.model.service.EmailService;
import com.ssafy.anymeetsong.model.service.JwtService;
import com.ssafy.anymeetsong.model.service.UserService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "관리자 컨트롤러 API v1", tags = {"Admin"})
@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "http://i6a505.p.ssafy.com")
public class AdminController {
	
	@Autowired
	private AdminService adminService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private JwtService jwtService;

	@GetMapping("/user")
	@ApiResponses({ 
		@ApiResponse(code = 200, message = "성공"),
		@ApiResponse(code = 401, message = "권한이 없습니다"),
		@ApiResponse(code = 403, message = "로그인이 되어있지 않습니다"),
		@ApiResponse(code = 404, message = "회원이 존재하지 않습니다"),
		@ApiResponse(code = 500, message = "내부 서버 오류")
	})
	public ResponseEntity<Map<String,Object>> userList(){
		int status = 200;
		Map<String,Object> resultMap = new HashMap<>();
		
		try {
			Map<String,Object> jwt = (Map<String,Object>)jwtService.getJwt().get("info");
			Map<String,String> inputValid = new HashMap<>();
			inputValid.put("id", (String)jwt.get("id"));

			if((int)jwt.get("is_admin") == (int)userService.checkValid(inputValid).get("is_admin")) {
				List<Map<String,Object>> resultList = adminService.getUserList();
				
				if(resultList != null) {
					status = 200;
					resultMap.put("users", resultList);
				}
				else status = 404;
			}
			else status = 401;
			
		} catch(NullPointerException e) {
			status = 403;
		} catch(Exception e){
			//e.printStackTrace();
			status = 500;
		}
		
		return ResponseEntity.status(status).body(resultMap);
	}

	@PutMapping("/user/nickname")
	@ApiResponses({ 
		@ApiResponse(code = 200, message = "닉네임 수정 성공"),
		@ApiResponse(code = 401, message = "권한이 없습니다"),
		@ApiResponse(code = 403, message = "로그인이 되어있지 않습니다"),
		@ApiResponse(code = 404, message = "닉네임 수정 실패"),
		@ApiResponse(code = 500, message = "내부 서버 오류")
	})
	public ResponseEntity<String> nicknameChange(
			@ApiParam(value = "id, nickname", required = true) @RequestBody Map<String,String> map){
		int status = 200;
		String message = "";
		
		try {
			Map<String,Object> jwt = (Map<String,Object>)jwtService.getJwt().get("info");
			Map<String,String> inputValid = new HashMap<>();
			inputValid.put("id", (String)jwt.get("id"));

			if((int)jwt.get("is_admin") == (int)userService.checkValid(inputValid).get("is_admin")) {
				int res = adminService.updateNickname(map);
				
				if(res != 0) {
					status = 200;
					message = "닉네임 수정 성공";
				}
				else {
					status = 404;
					message = "닉네임 수정 실패";
				}
			}
			else {
				status = 401;
				message = "권한이 없습니다";
			}
		} catch(NullPointerException e) {
			status = 403;
			message = "로그인이 되어있지 않습니다";
		} catch(Exception e){
			//e.printStackTrace();
			status = 500;
			message = "내부 서버 오류";
		}
		
		return ResponseEntity.status(status).body(message);
	}
	
	@PutMapping("/user/password")
	@ApiResponses({ 
		@ApiResponse(code = 200, message = "비밀번호 초기화 성공"),
		@ApiResponse(code = 401, message = "권한이 없습니다"),
		@ApiResponse(code = 403, message = "로그인이 되어있지 않습니다"),
		@ApiResponse(code = 404, message = "비밀번호 초기화 실패"),
		@ApiResponse(code = 500, message = "내부 서버 오류")
	})
	public ResponseEntity<String> passwordReset(
			@ApiParam(value = "id", required = true) @RequestBody Map<String,Object> map){
		int status = 200;
		String message = "";
		
		try {
			Map<String,Object> jwt = (Map<String,Object>)jwtService.getJwt().get("info");
			Map<String,String> inputValid = new HashMap<>();
			inputValid.put("id", (String)jwt.get("id"));

			if((int)jwt.get("is_admin") == (int)userService.checkValid(inputValid).get("is_admin")) {
				Map<String,String> inputMap = new HashMap<>();
				
				String tempPw = emailService.sendSimpleMessage((String)map.get("id"), 2);
				
				inputMap.put("id",(String)map.get("id"));
				inputMap.put("password", passwordEncoder.encode(tempPw));
				
				int res = adminService.resetPassword(inputMap);
				
				if(res != 0) {
					status = 200;
					message = "비밀번호 초기화 성공";
				}
				else {
					status = 404;
					message = "비밀번호 초기화 실패";
				}
			}
			else {
				status = 401;
				message = "권한이 없습니다";
			}
		} catch(NullPointerException e) {
			status = 403;
			message = "로그인이 되어있지 않습니다";
		} catch(Exception e){
			e.printStackTrace();
			status = 500;
			message = "내부 서버 오류";
		}
		
		return ResponseEntity.status(status).body(message);
	}
	
	@PutMapping("/user/ban")
	@ApiResponses({ 
		@ApiResponse(code = 200, message = "밴 설정 성공"),
		@ApiResponse(code = 401, message = "권한이 없습니다"),
		@ApiResponse(code = 403, message = "로그인이 되어있지 않습니다"),
		@ApiResponse(code = 404, message = "밴 설정 실패"),
		@ApiResponse(code = 500, message = "내부 서버 오류")
	})
	public ResponseEntity<String> banSetting(
			@ApiParam(value = "id, isBlocked", required = true) @RequestBody Map<String,Object> map){
		int status = 200;
		String message = "";
		
		try {
			Map<String,Object> jwt = (Map<String,Object>)jwtService.getJwt().get("info");
			Map<String,String> inputValid = new HashMap<>();
			inputValid.put("id", (String)jwt.get("id"));

			if((int)jwt.get("is_admin") == (int)userService.checkValid(inputValid).get("is_admin")) {
				if((int)jwt.get("is_admin") == 1) {
					Map<String,Object> inputMap = new HashMap<>();
					inputMap.put("id",(String)map.get("id"));
					inputMap.put("isBlocked", (int)map.get("isBlocked"));
					
					int res = adminService.updateBan(inputMap);
					
					if(res != 0) {
						status = 200;
						message = "밴 설정 성공";
					}
					else {
						status = 404;
						message = "밴 설정 실패";
					}
				}
			}
			else {
				status = 401;
				message = "권한이 없습니다";
			}
		} catch(NullPointerException e) {
			status = 403;
			message = "로그인이 되어있지 않습니다";
		} catch(Exception e){
			//e.printStackTrace();
			status = 500;
			message = "내부 서버 오류";
		}
		
		return ResponseEntity.status(status).body(message);
	}

	@PostMapping("/user/delete")
	@ApiResponses({ 
		@ApiResponse(code = 200, message = "회원 삭제 성공"),
		@ApiResponse(code = 401, message = "권한이 없습니다"),
		@ApiResponse(code = 403, message = "로그인이 되어있지 않습니다"),
		@ApiResponse(code = 404, message = "회원 삭제 실패"),
		@ApiResponse(code = 500, message = "내부 서버 오류")
	})
	public ResponseEntity<String> userDelete(
			@ApiParam(value = "id", required = true) @RequestBody Map<String,Object> map){
		int status = 200;
		String message = "";
		
		try {
			Map<String,Object> jwt = (Map<String,Object>)jwtService.getJwt().get("info");
			Map<String,String> inputValid = new HashMap<>();
			inputValid.put("id", (String)jwt.get("id"));

			if((int)jwt.get("is_admin") == (int)userService.checkValid(inputValid).get("is_admin")) {
				Map<String,String> inputMap = new HashMap<>();
				inputMap.put("id",(String)map.get("id"));
				
				int res = adminService.deleteUser(inputMap);
				
				if(res != 0) {
					status = 200;
					message = "회원 삭제 성공";
				}
				else {
					status = 404;
					message = "회원 삭제 실패";
				}
			}
			else {
				status = 401;
				message = "권한이 없습니다";
			}
		} catch(NullPointerException e) {
			status = 403;
			message = "로그인이 되어있지 않습니다";
		} catch(Exception e){
			e.printStackTrace();
			status = 500;
			message = "내부 서버 오류";
		}
		
		return ResponseEntity.status(status).body(message);
	}
	
	@GetMapping("/room")
	@ApiResponses({ 
		@ApiResponse(code = 200, message = "성공"),
		@ApiResponse(code = 401, message = "권한이 없습니다"),
		@ApiResponse(code = 403, message = "로그인이 되어있지 않습니다"),
		@ApiResponse(code = 404, message = "방이 존재하지 않습니다"),
		@ApiResponse(code = 500, message = "내부 서버 오류")
	})
	public ResponseEntity<Map<String,Object>> roomList(){
		int status = 200;
		Map<String, Object> resultMap = new HashMap<>();
		
		try {
			Map<String,Object> jwt = (Map<String,Object>)jwtService.getJwt().get("info");
			Map<String,String> inputValid = new HashMap<>();
			inputValid.put("id", (String)jwt.get("id"));

			if((int)jwt.get("is_admin") == (int)userService.checkValid(inputValid).get("is_admin")) {
				List<Map<String,Object>> resultList = adminService.getRoomList();
				
				if(resultList != null) {
					status = 200;
					resultMap.put("rooms", resultList);
				}
				else {
					status = 404;
					resultMap.put("rooms", null);
				}
			}
			else {
				status = 401;
			}
		} catch(NullPointerException e) {
			status = 403;
		} catch(Exception e){
			e.printStackTrace();
			status = 500;
		}
		
		return ResponseEntity.status(status).body(resultMap);
	}
	
	@DeleteMapping("/room/{rId}")
	@ApiResponses({ 
		@ApiResponse(code = 200, message = "방 폐쇠 성공"),
		@ApiResponse(code = 401, message = "권한이 없습니다"),
		@ApiResponse(code = 403, message = "로그인이 되어있지 않습니다"),
		@ApiResponse(code = 404, message = "방 폐쇠/방 강퇴 실패"),
		@ApiResponse(code = 500, message = "내부 서버 오류")
	})
	public ResponseEntity<String> roomDelete(
			@ApiParam(value = "방 번호", required = true) @PathVariable("rId") int roomId){
		int status = 200;
		String message = "";
		
		try {
			Map<String,Object> jwt = (Map<String,Object>)jwtService.getJwt().get("info");
			Map<String,String> inputValid = new HashMap<>();
			inputValid.put("id", (String)jwt.get("id"));

			if((int)jwt.get("is_admin") == (int)userService.checkValid(inputValid).get("is_admin")) {
				Map<String,Integer> inputMap = new HashMap<>();
				inputMap.put("id", roomId);
				
				int res1 = adminService.removeParticipants(inputMap);
				int res2 = adminService.deleteRoom(inputMap);
				
				if(res1 != 0 && res2 != 0) {
					status = 200;
					message =  "방 폐쇠 성공";
				}
				else {
					message = "방 ";
					if(res1 == 0) {
						status = 200;
						message += "강퇴 실패(빈 방 or 실패)";
					}
					if(res2 == 0) {
						status = 200;
						message += "폐쇠 실패(없는 방 or 사람 있음)";
					}
					if(res1 == 0 && res2 == 0) {
						status = 404;
						message = "방 강퇴 & 폐쇠 실패";
					}
				}
			}
			else {
				status = 401;
				message = "권한이 없습니다";
			}
		} catch(NullPointerException e) {
			status = 403;
			message = "로그인이 되어있지 않습니다";
		} catch(Exception e){
			e.printStackTrace();
			status = 500;
			message = "내부 서버 오류";
		}
		
		return ResponseEntity.status(status).body(message);
	}
}
