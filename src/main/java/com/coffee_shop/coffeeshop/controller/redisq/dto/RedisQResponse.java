package com.coffee_shop.coffeeshop.controller.redisq.dto;

import lombok.Getter;

@Getter
public class RedisQResponse {
	private String userId;

	public RedisQResponse(String userId) {
		this.userId = userId;
	}
}
