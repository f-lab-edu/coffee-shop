package com.coffee_shop.coffeeshop.common.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommonResponse {
	private String code;
	private String message;

	public CommonResponse(String code, String message) {
		this.code = code;
		this.message = message;
	}
}
