package com.ssafy.anymeetsong.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.ssafy.anymeetsong.model.UserDto;
import com.ssafy.anymeetsong.model.service.JwtService;
import com.ssafy.anymeetsong.model.service.RoomService;
import com.ssafy.anymeetsong.model.service.EmailService;
import com.ssafy.anymeetsong.model.service.UserService;
import com.ssafy.anymeetsong.response.EmailVerificationRes;
import com.ssafy.anymeetsong.response.QuestionListRes;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "유저 컨트롤러 API v1", tags = { "User" })
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "http://i6a505.p.ssafy.com")
public class UserController {
	public static final Logger logger = LoggerFactory.getLogger(UserController.class);

	@Autowired
	private UserService userService;

	@Autowired
	private RoomService roomService;
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private JwtService jwtService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@PostMapping("/regist")
	@ApiOperation(value = "회원 가입", notes = "회원 정보를 DB에 입력한다.")
	@ApiResponses({ 
		@ApiResponse(code = 201, message = "회원가입 완료"), 
		@ApiResponse(code = 461, message = "이메일 부적합"),
		@ApiResponse(code = 462, message = "비밀번호 오류"), 
		@ApiResponse(code = 463, message = "이메일 인증을 미진행"),
		@ApiResponse(code = 464, message = "질문 미입력"),
		@ApiResponse(code = 465, message = "답변 미입력"),
		@ApiResponse(code = 466, message = "회원 가입 실패") 
	})
	public ResponseEntity<String> registUser(
			@RequestBody @ApiParam(value = "회원가입 정보", required = true) UserDto userDto) {
//		logger.info("registUser - 호출");
//		logger.info(userDto.getId());
//		logger.info(userDto.getPassword());
//		logger.info(userDto.getNickname());

		// 이메일 validation 체크
		if (!userDto.getId().matches(".+@.+")) {
			return ResponseEntity.status(461).body("이메일 부적합");
		}

		// 비밀번호 오류 체크: 글자 제한(8~20자), 영어 혹은 숫자 혹은 특수문자만 사용가능
		if (userDto.getPassword() == null
				|| (!userDto.getPassword().matches("^(?=.*[A-Za-z\\d])[A-Za-z\\d~!@#$%^&*()+|=]{8,20}$")))
			return ResponseEntity.status(462).body("비밀번호 오류");
		else {
			String pw = userDto.getPassword();
			userDto.setPassword(passwordEncoder.encode(pw)); // 암호화
		}

		// 질문 미입력 체크: 미 체크 시, default 값은 0이다.
		if (userDto.getQuestionsId() == 0)
			return ResponseEntity.status(464).body("질문 미입력");

		// 답변 미입력 체크
		if (userDto.getAnswer() == null)
			return ResponseEntity.status(465).body("답변 미입력");

		// 회원 가입 정보 DB에 입력
		try {
			if (userService.regist(userDto)) {
				return ResponseEntity.status(201).body("회원가입 완료"); // 성공 시
			}
		} catch (Exception e) {
			return ResponseEntity.status(466).body("회원 가입 실패"); // 내부 쿼리 작동 실패 시
		}
		return ResponseEntity.status(500).body("내부 서버 에러"); // 실패 시
	}

	@PostMapping("/regist/email")
	@ApiOperation(value = "회원 가입시 이메일로 인증 코드 보내기", notes = "회원 가입 이메일로 랜덤한 인증 코드 송신하기")
	@ApiResponses({ 
		@ApiResponse(code = 201, message = "요청 성공"), 
		@ApiResponse(code = 500, message = "요청 실패") 
	})
	public ResponseEntity<String> sendVerificationEmail(
			@RequestBody @ApiParam(value = "이메일정보 정보", required = true) Map<String, String> map) throws Exception {

//		logger.info("sendVerificationEmail - 호출");
		String verificationCode = null;
		try {
			verificationCode = emailService.sendSimpleMessage(map.get("id"), 1);
		} catch (Exception e) {
			return ResponseEntity.status(500).body("요청 실패");
		}
		
		// Verification Code 생성 및 저장.
		emailService.saveIdAndCodeToDB(map.get("id"), verificationCode);

		return ResponseEntity.status(201).body("요청 성공");
	}

	@GetMapping("/regist/{id}/{code}")
	@ApiOperation(value = "인증 코드 확인", notes = "인증코드 확인")
	@ApiResponses({ 
		@ApiResponse(code = 200, message = "인증 완료"), 
		@ApiResponse(code = 400, message = "인증 코드 불일치"),
		@ApiResponse(code = 401, message = "이메일 불일치"), 
		@ApiResponse(code = 402, message = "시간 초과"), 
	})
	public ResponseEntity<EmailVerificationRes> codeVerify(
			@PathVariable(value = "id") @ApiParam(value = "사용자 이메일", required = true) String id,
			@PathVariable(value = "code") @ApiParam(value = "사용자가 받은 코드", required = true) String code)
			throws Exception {

		logger.info("codeViery - 호출");
		logger.info(id);
		logger.info(code);
		
		int statuscode = emailService.verifyCode(id, code);
		
		if(statuscode == 200) {
			return ResponseEntity.status(statuscode).body(EmailVerificationRes.of(true));
		} else {
			return ResponseEntity.status(statuscode).body(EmailVerificationRes.of(false));
		}
	}

	@GetMapping("/questions")
	@ApiOperation(value = "질문 목록 조회", notes = "DB에 저장된 질문 목록들 가져오기")
	@ApiResponses({ 
		@ApiResponse(code = 200, message = "조회 성공"), 
		@ApiResponse(code = 500, message = "내부 서버 오류"), 
	})
	public ResponseEntity<QuestionListRes> getQuestionList() throws Exception {
//		logger.info("getQuestionList - 호출");
		return ResponseEntity.status(200).body(QuestionListRes.of(userService.getQuestionList()));
	}

	@PostMapping("/login")
	@ApiOperation(value = "로그인", notes = "입력받은 회원 정보로 사용자를 찾고 access token을 발급한다.")
	@ApiResponses({ 
		@ApiResponse(code = 200, message = "로그인 완료"), 
		@ApiResponse(code = 404, message = "로그인 실패"),
		@ApiResponse(code = 500, message = "내부 서버 오류") 
	})
	public ResponseEntity<Map<String,Object>> login(
			@ApiParam(value = "로그인 정보 id password", required = true) @RequestBody Map<String, String> map) {
		int status = 0;
		String password = map.get("password");
		Map<String,Object> resultMap = new HashMap<>();

		try {
			Map<String,Object> checkValid = userService.checkValid(map);

			if (checkValid != null && password != null
					&& passwordEncoder.matches(password, (String) checkValid.get("password"))) {
				checkValid.remove("password");

				if ((int) checkValid.get("is_blocked") != 1) {
					status = 200;

					resultMap.put("id", checkValid.get("id"));
					resultMap.put("nickname", checkValid.get("nickname"));
					resultMap.put("is_admin", checkValid.get("is_admin"));

					String token = jwtService.create("info", checkValid, "access-token");
					resultMap.put("access_token", token);
				} else
					status = 403;
			} else
				status = 404;
		} catch (Exception e) {
			// e.printStackTrace();
			status = 500;
		}

		return ResponseEntity.status(status).body(resultMap);
	}

	@PostMapping("/logout")
	@ApiResponses({ 
		@ApiResponse(code = 200, message = "로그아웃 완료"), 
		@ApiResponse(code = 404, message = "로그아웃 실패"),
		@ApiResponse(code = 500, message = "내부 서버 오류") 
	})
	public ResponseEntity<String> logout(@RequestBody Map<String, Object> map) {
		int status = 200;
		String message = "";

		Map<String, Object> jwt = jwtService.getJwt();
		
		if(jwt != null) {			
			Map<String,Object> jwtInfo = (Map<String,Object>)jwt.get("info");
			String userId = (String)jwtInfo.get("id");
			int roomId = (int)map.get("roomId");
			
			//db에도 로그아웃 확인
			if(userId.equals(map.get("id"))) {
					if(roomId != -1) {
						try {
							String isHost = roomService.findHost(String.valueOf(roomId));
							
							if(isHost.equals(userId)) {
								String hostRoom = roomService.isHostThereS(isHost);
								boolean res1 = roomService.deleteRoomParticipants(hostRoom);
								boolean res2 = roomService.deleteRoom(hostRoom);
								
								if(res1 && res2) {
									message = "로그아웃 완료";
								}
								else {
									status = 404;
									message = "로그아웃 실패";
									if(!res1) {
										message += ": 방 강퇴 실패";
									}
									if(!res2) {
										message += ": 방 삭제 실패";
									}
									if(!res1 && !res2) {
										message = "로그아웃 실패: 방 강퇴/삭제 실패";
									}
								}
							}
							else {
								Map<String,Object> inputMap = new HashMap<>();
								inputMap.put("id", roomId);
								inputMap.put("user_id", userId);
								
								boolean res3 = roomService.leave(inputMap);
								
								if(res3) {
									message = "로그아웃 완료";								
								}
								else {
									status = 404;
									message = "로그아웃 실패: 방 퇴장 실패";
								}
							}	
						} catch(Exception e) {
							//e.printStackTrace();
							status = 500;
							message = "내부 서버 에러";
						}
					}
					else {
						message = "로그아웃 완료";
					}				
			}
			else {
				status = 404;
				message = "로그아웃 실패";
			}
		} else {
			status = 500;
			message = "내부 서버 오류";
		}

		return ResponseEntity.status(status).body(message);
	}

	@GetMapping("/{phone}/{qId}/{ans}")
	@ApiResponses({ 
		@ApiResponse(code = 200, message = "성공"), 
		@ApiResponse(code = 404, message = "해당 정보를 찾을 수 없습니다"),
		@ApiResponse(code = 500, message = "내부 서버 오류") 
	})
	public ResponseEntity<Map<String, String>> findId(
			@ApiParam(value = "전화번호", required = true) @PathVariable("phone") String phone,
			@ApiParam(value = "질문 번호", required = true) @PathVariable("qId") String questionsId,
			@ApiParam(value = "답변", required = true) @PathVariable("ans") String answer) {
		int status = 0;
		Map<String, Object> inputMap = new HashMap<>();
		inputMap.put("phone", phone);
		inputMap.put("questionsId", questionsId);
		
		if (answer != null)
			inputMap.put("answer", answer.replace("%20", " "));
		else
			inputMap.put("answer", null);

		Map<String, String> resultMap = new HashMap<>();

		try {
			Map<String, String> checkId = userService.findByInfo(inputMap);

			if (checkId != null) {
				status = 200;
				resultMap.put("id", checkId.get("id"));
			} else
				status = 404;
		} catch (Exception e) {
			// e.printStackTrace();
			status = 500;
		}

		return ResponseEntity.status(status).body(resultMap);
	}

	@PostMapping("/password")
	@ApiResponses({ 
		@ApiResponse(code = 200, message = "성공"), 
		@ApiResponse(code = 404, message = "해당 정보를 찾을 수 없습니다"),
		@ApiResponse(code = 500, message = "내부 서버 오류") 
	})
	public ResponseEntity<String> findPassword(@RequestBody Map<String, Object> map) {
		int status = 0;
		String message = "";

		try {
			Map<String, Object> checkId = userService.findById(map);

			if (checkId != null && checkId.get("questionsId") == Long.valueOf(((int) map.get("questionsId")))
					&& checkId.get("answer").equals(map.get("answer"))) {

				// 이메일 전송 임시 비밀번호로 변경
				String tempPw = emailService.sendSimpleMessage((String) map.get("id"), 2);

				map.put("password", passwordEncoder.encode(tempPw));

				int res = userService.changePassword(map);

				if (res != 0) {
					status = 200;
					message = "성공";
				} else {
					status = 500;
					message = "내부 서버 오류";
				}
			} else {
				status = 404;
				message = "해당 정보를 찾을 수 없습니다";
			}
		} catch (Exception e) {
			// e.printStackTrace();
			status = 500;
			message = "내부 서버 오류";
		}

		return ResponseEntity.status(status).body(message);
	}
	
	@GetMapping("/list")
	@ApiOperation(value = "회원정보조회", notes = "입력받은 회원 정보로 사용자를 찾고 회원 정보를 제공한다.")
	@ApiResponses({ 
		@ApiResponse(code = 200, message = "조회 완료"),
		@ApiResponse(code = 401, message = "로그인 되어있지 않습니다"),
		@ApiResponse(code = 404, message = "해당 유저가 존재하지 않습니다"),
		@ApiResponse(code = 500, message = "내부 서버 오류")
	})
	public ResponseEntity<Map<String,Object>> list(){
		int status = -1;
		Map<String, Object> resultMap = new HashMap<>();
		Map<String, Object> jwt = jwtService.getJwt();		

		if(jwt != null) {			
			Map<String,Object> jwtInfo = (Map<String,Object>)jwt.get("info");			
			try {			
				resultMap = userService.getUser(String.valueOf(jwtInfo.get("id")));

				if(resultMap.get("id") != null) status = 200;
				else status = 404;
			} catch (Exception e) {
				status = 500;
			}
		}
		else {
			status = 401;
		}
		
		return ResponseEntity.status(status).body(resultMap);
	}
	
	@PutMapping("/update")
	@ApiOperation(value = "회원정보수정", notes = "입력받은 회원 정보로 사용자의 회원 정보를 수정한다.")
	@ApiResponses({ 
		@ApiResponse(code = 201, message = "수정 완료"),
		@ApiResponse(code = 462, message = "비밀번호가 틀렸습니다"),
		@ApiResponse(code = 400, message = "잘못된 요청입니다"),
		@ApiResponse(code = 500, message = "내부 서버 오류")
	})
	public ResponseEntity<String> update(
			@ApiParam(value = "회원가입 정보", required = true) @RequestBody Map<String,Object> map) throws Exception{
		Map<String, Object> jwt = jwtService.getJwt();

		if(jwt == null) return ResponseEntity.status(400).body("잘못된 요청입니다");
		Map<String,Object> jwtInfo = (Map<String,Object>)jwt.get("info");
		map.put("id", jwtInfo.get("id"));

		try {
//			String pw = (String)map.get("password");
//			map.put("password", passwordEncoder.encode(pw)); //암호화
			int res = userService.updateUser(map);
			
			if(res != 0) return ResponseEntity.status(201).body("수정 완료");
			else return ResponseEntity.status(400).body("잘못된 요청입니다");
			

		} catch (Exception e) {
			return ResponseEntity.status(500).body("내부 서버 에러");
		}		
	}
	
	@PutMapping("/changepw")
	@ApiOperation(value = "비밀번호 수정", notes = "입력받은 비밀번호가 기존 회원 비밀번호와 일치하면 새 비밀번호로 갱신한다.")
	@ApiResponses({ 
		@ApiResponse(code = 201, message = "수정 완료"),
		@ApiResponse(code = 400, message = "잘못된 요청입니다"),
		@ApiResponse(code = 402, message = "새 비밀번호가 유효하지 않습니다"),
		@ApiResponse(code = 403, message = "현재 비밀번호가 틀렸습니다"),
		@ApiResponse(code = 500, message = "내부 서버 오류")
	})
	public ResponseEntity<String> changepw(
			@ApiParam(value = "회원가입 정보", required = true) @RequestBody Map<String,Object> map) throws Exception{
		Map<String, Object> jwt = jwtService.getJwt();
		Map<String, Object> input = new HashMap<String, Object>();
				
		if(jwt == null) return ResponseEntity.status(400).body("잘못된 요청입니다");
		Map<String,Object> jwtInfo = (Map<String,Object>)jwt.get("info");
		String updatePw = String.valueOf(map.get("changepw"));
		input.put("id", String.valueOf(jwtInfo.get("id")));
		input.put("password", passwordEncoder.encode(updatePw));
		
		String pw = (String)map.get("password");

		try {

			System.out.println(jwtInfo.get("id"));
			String userTypePw = userService.checkPw(String.valueOf(jwtInfo.get("id")));
			int res = 0;
			
			if(passwordEncoder.matches(pw, userTypePw)) {
				res = userService.updatePassword(input);
			}
			else {
				return ResponseEntity.status(403).body("현재 비밀번호가 틀립니다");
			}
			
			if(res != 0) {
				return ResponseEntity.status(201).body("수정 완료");
			} 
			else {
				return ResponseEntity.status(402).body("새 비밀번호가 유효하지 않습니다");
			}
			
		} catch (Exception e) {
			return ResponseEntity.status(500).body("내부 서버 에러");
		}		
	}
	
	@DeleteMapping("/delete")
	@ApiOperation(value = "회원탈퇴", notes = "입력받은 회원아이디와 일차하는 아이디를 탈퇴한다.")
	@ApiResponses({ 
		@ApiResponse(code = 204, message = "탈퇴 완료"),
		@ApiResponse(code = 400, message = "잘못된 요청입니다"),
		@ApiResponse(code = 401, message = "로그인 되어 있지 않습니다"),
		@ApiResponse(code = 500, message = "내부 서버 오류")
	})
	public ResponseEntity<String> delete() throws Exception{
		Map<String, Object> jwt = jwtService.getJwt();
		
		if(jwt == null) {			
			return ResponseEntity.status(401).body("로그인 되어 있지 않습니다");
		}
		Map<String,Object> jwtInfo = (Map<String,Object>)jwt.get("info");

		if(userService.deleteUser(String.valueOf(jwtInfo.get("id")))) {
			return ResponseEntity.status(201).body("탈퇴 완료");	
		}
		
		return ResponseEntity.status(500).body("내부 서버 오류");
	}

}