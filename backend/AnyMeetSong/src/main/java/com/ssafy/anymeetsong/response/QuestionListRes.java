package com.ssafy.anymeetsong.response;

import java.util.ArrayList;
import java.util.List;

import com.ssafy.anymeetsong.model.QuestionsDto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("QuestionListResponse")
public class QuestionListRes {
	@ApiModelProperty(name="Return List of Questions")
	private List<String> questions;
	
	public static QuestionListRes of(List<QuestionsDto> list) {
		QuestionListRes res = new  QuestionListRes();
		
		res.setQuestions(new ArrayList<>());
		
		for(int i = 0, size = list.size(); i < size; i++) {
			res.getQuestions().add(list.get(i).getQuestion());
		}

		return res;
	}
}
