package com.ssafy.anymeetsong.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("EmailVerificationResponse")
public class EmailVerificationRes {
	@ApiModelProperty(name="Verification Check code coincident")
	private boolean checkCode;
	
	public static EmailVerificationRes of(boolean isCoincident) {
		EmailVerificationRes res = new  EmailVerificationRes();
		res.setCheckCode(isCoincident);
		return res;
	}
}
