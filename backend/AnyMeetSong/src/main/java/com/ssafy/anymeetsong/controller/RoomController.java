package com.ssafy.anymeetsong.controller;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.anymeetsong.model.service.JwtService;
import com.ssafy.anymeetsong.model.service.RoomService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "노래방 컨트롤러 API v1", tags = {"Room"})
@RestController
@RequestMapping("/room")
@CrossOrigin(origins = "http://i6a505.p.ssafy.com")
public class RoomController {
	public static final Logger logger = LoggerFactory.getLogger(RoomController.class);

	@Autowired
	private RoomService roomService;
	
	@Autowired
	private JwtService jwtService;
	
	@PostMapping("/list")
	@ApiOperation(value = "노래방 검색", notes = "노래방 전체 조회 (공개 비공개 유무 추후 구현시 명세서 추가해서 만듬.)")
	@ApiResponses({ 
		@ApiResponse(code = 201, message = "방 검색 완료"),
		@ApiResponse(code = 401, message = "노래방이 없습니다."),
		@ApiResponse(code = 402, message = "로그인 되어 있지 않습니다."),
		@ApiResponse(code = 500, message = "내부 서버 오류")
	})
	public ResponseEntity<List<Map<String, Object>>> list(
			@ApiParam(value = "방 생성 정보", required = true) @RequestBody Map<String,Object> map) throws Exception {
		Map<String, Object> jwt = jwtService.getJwt();		
		List<Map<String, Object>> resultList = new LinkedList<Map<String,Object>>();		
		
		
		if(jwt == null)
			return ResponseEntity.status(402).body(resultList);
		
		

		int filter = Integer.parseInt((String) map.get("filter"));
		try {			
			if(filter == 0) {
				resultList = roomService.getRoomAll(map);
			} else if(filter == 1) {
				resultList = roomService.getRoomWithPassword(map);

			} else if(filter == 2) {
				resultList = roomService.getRoomWithoutPassword(map);

			}
			if(resultList != null) 
				return ResponseEntity.status(200).body(resultList);				
			else
				return ResponseEntity.status(401).body(resultList);	

		} catch (Exception e) {
			return ResponseEntity.status(500).body(resultList);
		}
	}
	
	@PostMapping("/create")
	@ApiOperation(value = "노래방 생성", notes = "입력받은 방 정보로 노래방을 생성한다.")
	@ApiResponses({ 
		@ApiResponse(code = 201, message = "방 생성 완료"),
		@ApiResponse(code = 400, message = "잘못된 요청입니다"),
		@ApiResponse(code = 401, message = "로그인이 되어 있지 않습니다"),
		@ApiResponse(code = 461, message = "방은 한개만 생성이 됩니다"),
		@ApiResponse(code = 500, message = "내부 서버 오류")
	})
	public ResponseEntity<Map<String, Object>> create(
			@ApiParam(value = "방 생성 정보", required = true) @RequestBody Map<String,Object> map) throws Exception {
		Map<String, Object> jwt = jwtService.getJwt();		
		Map<String,Object> result = new HashMap<String, Object>();

		if(jwt == null)
			return ResponseEntity.status(201).body(result);
	
		Map<String,Object> jwtInfo = (Map<String,Object>)jwt.get("info");
		map.put("hostId", jwtInfo.get("id"));	
		

		try {			
			List<Map<String, Object>> resultList = new LinkedList<Map<String,Object>>();
			resultList = roomService.isHostThere(String.valueOf(jwtInfo.get("id")));

			if(resultList.size() == 0) {				
				if(roomService.createRoom(map)) {
					String getNum = roomService.isHostThereS(String.valueOf(jwtInfo.get("id")));
					result.put("id", (Object)getNum);
					return ResponseEntity.status(201).body(result);
				} else {
					return ResponseEntity.status(400).body(result);
				}
			} else {
				return ResponseEntity.status(461).body(result);
			}
		} catch (Exception e) {
			return ResponseEntity.status(500).body(result);
		}			
	}
	
	@PostMapping("/join/{id}")
	@ApiOperation(value = "노래방 참가", notes = "노래방에 입장한다.")
	@ApiResponses({
		@ApiResponse(code = 201, message = "방 참가 성공"),
		@ApiResponse(code = 400, message = "잘못된 요청입니다"),
		@ApiResponse(code = 401, message = "로그인되어 있지 않습니다"),
		@ApiResponse(code = 404, message = "방을 찾을 수 없습니다"),
		@ApiResponse(code = 500, message = "내부 서버 오류")
	})
	public ResponseEntity<String> join(
			@PathVariable(value = "id") @ApiParam(value="방 번호", required = true) String id) throws Exception {

//			@ApiParam(value = "방 생성 정보", required = true) @RequestBody Map<String,Object> map) throws Exception {

		Map<String, Object> jwt = jwtService.getJwt();		
		int status = -1;
		StringBuilder sb = new StringBuilder();
		Map<String, Object> inputMap = new HashMap<>();
		if(jwt == null)
			return ResponseEntity.status(401).body("https://room/");	 
		Map<String,Object> jwtInfo = (Map<String,Object>)jwt.get("info");
		inputMap.put("hostId", jwtInfo.get("id"));
//		inputMap.put("id", map.get("id"));
		inputMap.put("id", id);

		System.out.println("oh");
		try {			
			status = 200;						
			if(roomService.joinRoom(inputMap)) {
				sb.append("url: http://room/----");
			} else {
				status = 400;
				sb.append("url creation failed");
			}
		} catch (Exception e) {
			status = 500;
		}	
		return ResponseEntity.status(status).body("https://room/");
	}
	
	
	@PutMapping("/update")
	@ApiOperation(value = "노래방 정보 수정", notes = "노래방 타이틀 혹은 비밀번호 수정한다.")
	@ApiResponses({
		@ApiResponse(code = 201, message = "방 수정 성공"),
		@ApiResponse(code = 401, message = "로그인되어 있지 않습니다"),
		@ApiResponse(code = 404, message = "방을 찾을 수 없습니다"),
		@ApiResponse(code = 461, message = "방 호스트가 아니라 정보 수정이 불가합니다"),
		@ApiResponse(code = 500, message = "내부 서버 오류")
	})
	public ResponseEntity<String> update(
			@ApiParam(value = "방 정보 수정", required = true) @RequestBody Map<String,Object> map) throws Exception {
		Map<String, Object> jwt = jwtService.getJwt();		
		if(jwt == null)
			return ResponseEntity.status(401).body("로그인되어 있지 않습니다");
		Map<String,Object> jwtInfo = (Map<String,Object>)jwt.get("info");
		Map<String, Object> resultMap = new HashMap<>();
		try {
			String boss = roomService.findHost(String.valueOf(map.get("id")));
			if(boss == null)
				return ResponseEntity.status(404).body("방을 찾을 수 없습니다");				
			if(boss.equals(jwtInfo.get("id"))) {
				resultMap = roomService.update(map);				
				return ResponseEntity.status(200).body("성공");												
			} else 
				return ResponseEntity.status(461).body("방 호스트가 아니라 정보 수정이 불가합니다");								
			
			
			
		} catch (Exception e) {
			return ResponseEntity.status(500).body("내부 서버 오류");				
		}	
	}
	
	
	@DeleteMapping("/leave/{id}")
	@ApiOperation(value = "노래방 퇴실", notes = "사용자가 노래방을 나갑니다.")
	@ApiResponses({
		@ApiResponse(code = 204, message = "퇴실 성공"),
		@ApiResponse(code = 205, message = "방장 퇴실 및 방장 방 삭제 성공"),
		@ApiResponse(code = 401, message = "잘못된 요청입니다"),
		@ApiResponse(code = 461, message = "참가자 퇴실이 완료되지 않았습니다."),
		@ApiResponse(code = 462, message = "방 삭제가 되지 않았습니다"),
		@ApiResponse(code = 500, message = "내부 서버 오류")
	})
	public ResponseEntity<String> leave(
			@PathVariable(value = "id") @ApiParam(value="방 번호", required = true) String id) throws Exception {
		Map<String, Object> jwt = jwtService.getJwt();		
		if(jwt == null)
			return ResponseEntity.status(401).body("로그인되어 있지 않습니다");
		Map<String,Object> jwtInfo = (Map<String,Object>)jwt.get("info");
		Map<String,Object> input = (Map<String,Object>)jwt.get("info");
		
		String room_id = id;
		String user_id = String.valueOf(jwtInfo.get("id"));		
		input.put("id", room_id);
		String boss = roomService.findHost(room_id);
		Object o = user_id;
		input.put("user_id", o);		
		try {			
			if(roomService.leave(input)) {
				if(boss.equals(user_id)) {
					if(!roomService.leaveAll(input)) {
						if(roomService.deleteRoom(room_id)) {
							return ResponseEntity.status(205).body("방장 퇴실 및 방장 방 삭제 성공");
						} else {
							return ResponseEntity.status(462).body("방 삭제가 되지 않았습니다");
						}
					} else {
						return ResponseEntity.status(461).body("참가자 퇴실이 완료되지 않았습니다.");
					}
				}
			}
			return ResponseEntity.status(204).body("퇴실 성공");
		} catch (Exception e) {
			return ResponseEntity.status(500).body("내부 서버 오류");
		}	
		
	}
	
	@DeleteMapping("/delete/{id}/{name}")
	@ApiOperation(value = "노래방 퇴실", notes = "노래방을 삭제합니다.")
	@ApiResponses({
		@ApiResponse(code = 204, message = "삭제 성공"),
		@ApiResponse(code = 401, message = "로그인 되어 있지 않습니다"),
		@ApiResponse(code = 400, message = "잘못된 요청입니다."),
		@ApiResponse(code = 461, message = "호스트가 아니라서 삭제할 수 없습니다"),
		@ApiResponse(code = 500, message = "내부 서버 오류")
	})
	public ResponseEntity<String> delete(
			@PathVariable(value = "id") @ApiParam(value="방 번호", required = true) String id,
			@PathVariable(value = "name") @ApiParam(value="사용자 이름", required = true) String name) throws Exception {
		Map<String, Object> jwt = jwtService.getJwt();		
		Map<String, Object> inputMap = new HashMap<>();
		if(jwt == null)
			return ResponseEntity.status(401).body("로그인되어 있지 않습니다");
		Map<String,Object> jwtInfo = (Map<String,Object>)jwt.get("info");
		inputMap.put("hostId", jwtInfo.get("id"));
		inputMap.put("id", id);		
		try {						
			if(name.equals(jwtInfo.get("id"))) {
				if(roomService.deleteRoomParticipants(id) && roomService.deleteRoom(id)) 
					return ResponseEntity.status(204).body("삭제 성공");
			} else 
				return ResponseEntity.status(461).body("호스트가 아니라서 삭제할 수 없습니다");
			return ResponseEntity.status(400).body("잘못된 요청입니다");
		} catch (Exception e) {
			return ResponseEntity.status(500).body("내부 서버 오류");
		}	
	}
	
	
	@GetMapping("/listRoom/{id}")
	@ApiOperation(value = "노래방 참가", notes = "노래방에 입장한다.")
	@ApiResponses({
		@ApiResponse(code = 201, message = "방 검색 완료"),
		@ApiResponse(code = 401, message = "노래방이 없습니다."),
		@ApiResponse(code = 402, message = "로그인 되어 있지 않습니다."),
		@ApiResponse(code = 500, message = "내부 서버 오류")
	})
	public ResponseEntity<Map<String, Object>> listRoom(
			@PathVariable(value = "id") @ApiParam(value="방 번호", required = true) String id) throws Exception {
		Map<String, Object> jwt = jwtService.getJwt();				
		Map<String, Object> resultList = new HashMap<String, Object>();
		
		if(jwt == null)
			return ResponseEntity.status(402).body(resultList);
		
		try {			
			resultList = roomService.getRoomWithId(id);
			if(resultList != null) 
				return ResponseEntity.status(201).body(resultList);				
			else
				return ResponseEntity.status(401).body(resultList);	
		} catch (Exception e) {
			return ResponseEntity.status(500).body(resultList);
		}
	}
	
}
