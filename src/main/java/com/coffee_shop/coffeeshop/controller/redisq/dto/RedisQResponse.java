package com.coffee_shop.coffeeshop.controller.redisq.dto;

import lombok.Getter;

@Getter
public class RedisQResponse {
	private Long userId;

	public RedisQResponse(Long userId) {
		this.userId = userId;
	}
}
