package com.ssafy.anymeetsong.controller;

import java.util.HashMap;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.anymeetsong.model.SongInfoDto;
import com.ssafy.anymeetsong.model.service.JwtService;
import com.ssafy.anymeetsong.model.service.SongListService;
import com.ssafy.anymeetsong.model.service.YoutubeService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "노래방 메인 기능 컨트롤러 API v1", tags = {"Sing"})
@RestController
@RequestMapping("/sing")
@CrossOrigin(origins = "http://i6a505.p.ssafy.com")
public class SingController {
	public static final Logger logger = LoggerFactory.getLogger(SingController.class);

	@Autowired
	private YoutubeService youtubeService;
	
	@Autowired
	private SongListService songListService;
	
	@Autowired
	private JwtService jwtService;

	@PostMapping("/search/{keyword}")
	@ApiOperation(value = "Youtube 검색", notes = "입력 받은 keyword로 유튜브 API를 사용해 검색: 썸네일, 제목, videoId를 반환받는다.")
	@ApiResponses({ 
		@ApiResponse(code = 200, message = "성공"),
		@ApiResponse(code = 401, message = "로그인 되어있지 않습니다."),
		@ApiResponse(code = 466, message = "노래를 불러오지 못했습니다.")
	})
	public ResponseEntity<Map<String,List<SongInfoDto>>> searchSongsByKeywordFromYoutube(
			@ApiParam(value = "검색 키워드", required = true) @PathVariable("keyword") String keyword){
		logger.info("searchSongsByKeywordFromYoutube - 호출");
		
		int status = 0;
		Map<String, Object> jwt = jwtService.getJwt();
		List<SongInfoDto> list = null;
		Map<String,List<SongInfoDto>> resultMap = new HashMap<>();
		
		if(jwt != null) {
			try {
				list = youtubeService.searchByTitleFromYoutube(keyword);
				
				if(list == null) status = 466;
				else status = 200;
			}
			catch (Exception e) {
				status = 466;
			}
		} else status = 401;
		resultMap.put("list", list);
		return ResponseEntity.status(status).body(resultMap);
	}
	
	@GetMapping("/list/{roomId}")
	@ApiOperation(value = "예약 노래 리스트 얻기", notes = "입력 받은 roomId로 해당 방의 노래 예약 리스트를 가져온다.")
	@ApiResponses({ 
		@ApiResponse(code = 200, message = "성공"),
		@ApiResponse(code = 401, message = "로그인 되어있지 않습니다."),
		@ApiResponse(code = 466, message = "노래 리스트를 불러오지 못했습니다.")
	})
	public ResponseEntity<Map<String,List<SongInfoDto>>> getSongListByRoomId(
			@ApiParam(value = "리스트를 가져올 방 Id", required = true) @PathVariable("roomId") String roomId){
		logger.info("getSongListByRoomId - 호출");
		
		int status = 0;
		Map<String, Object> jwt = jwtService.getJwt();
		List<SongInfoDto> list = null;
		Map<String,List<SongInfoDto>> resultMap = new HashMap<>();
		
		if(jwt != null) {
			try {
				list = songListService.getSongList(roomId);
				
				if(list == null) status = 466;
				else status = 200;
			}
			catch (Exception e) {
				status = 466;
			}
		} else status = 401;
		resultMap.put("list", list);
		return ResponseEntity.status(status).body(resultMap);
	}
	
	@PostMapping("/list/{roomId}")
	@ApiOperation(value = "노래 리스트에 노래 추가", notes = "입력 받은 roomId로 해당 방의 노래 예약 리스트에 노래를 추가한다.")
	@ApiResponses({ 
		@ApiResponse(code = 200, message = "성공"),
		@ApiResponse(code = 401, message = "로그인 되어있지 않습니다."),
		@ApiResponse(code = 466, message = "노래 추가 실패")
	})
	public ResponseEntity<String> addSongToList(
			@ApiParam(value = "노래를 추가할 방 ID", required = true) @PathVariable("roomId") String roomId,
			@ApiParam(value = "추가할 노래의 정보", required=true) @RequestBody SongInfoDto songInfoDto){
		logger.info("addSongToList - 호출");
		
		int status = 0, result = 0;
		Map<String, String> inputMap = new HashMap<String, String>();
		
		inputMap.put("roomId", roomId);
		inputMap.put("title", songInfoDto.getTitle());
		inputMap.put("videoId", songInfoDto.getVideoId());
		inputMap.put("thumbnail", songInfoDto.getThumbnail());
		inputMap.put("id", songInfoDto.getId());
		inputMap.put("nickname", songInfoDto.getNickname());
		Map<String, Object> jwt = jwtService.getJwt();
		
		if(jwt != null) {
			try {
				result = songListService.addSongToList(inputMap);
				
				if(result == 0) status = 466;
				else status = 200;
			}
			catch (Exception e) {
				status = 466;
			}
		} else status = 401;
		return ResponseEntity.status(status).body("Done.");
	}
	
	@DeleteMapping("/list/{roomId}/{songId}")
	@ApiOperation(value = "리스트에서 노래 삭제", notes = "입력 받은 roomId로 해당 방의 노래 예약 리스트에서 songId에 해당하는 노래를 삭제한다.")
	@ApiResponses({ 
		@ApiResponse(code = 200, message = "성공"),
		@ApiResponse(code = 401, message = "로그인 되어있지 않습니다."),
		@ApiResponse(code = 466, message = "노래 삭제 실패")
	})
	public ResponseEntity<String> deleteSongFromList(
			@ApiParam(value = "노래를 삭제할 방 Id", required = true) @PathVariable("roomId") String roomId,
			@ApiParam(value = "노래 ID", required = true) @PathVariable("songId") String songId){
		logger.info("deleteSongFromList - 호출");
		
		int status = 0, result = 0;
		Map<String, Object> jwt = jwtService.getJwt();
		
		if(jwt != null) {
			try {
				result = songListService.deleteSongFromList(roomId, songId);
				
				if(result == 0) status = 466;
				else status = 200;
			}
			catch (Exception e) {
				status = 466;
			}
		} else status = 401;
		return ResponseEntity.status(status).body("Done.");
	}

}