package com.coffee_shop.coffeeshop.service.redisq;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisQService {

	private static final String QUEUE_NAME = "coupon-queue";

	private final RedisTemplate<String, Object> redisTemplate;

	public void enqueue(String data) {
		redisTemplate.opsForList().rightPush(QUEUE_NAME, data);
	}

	public String dequeue() {
		return (String)redisTemplate.opsForList().leftPop(QUEUE_NAME);
	}
}
