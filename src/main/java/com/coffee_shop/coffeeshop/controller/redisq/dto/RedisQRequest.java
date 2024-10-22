package com.coffee_shop.coffeeshop.controller.redisq.dto;

import jakarta.validation.constraints.NotNull;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RedisQRequest {

	@NotNull(message = "사용자 ID는 필수입니다.")
	private String userId;

	@Builder
	public RedisQRequest(String userId) {
		this.userId = userId;
	}
}
