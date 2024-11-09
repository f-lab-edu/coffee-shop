package com.coffee_shop.coffeeshop.service.redisq;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisQService {

	private static final String QUEUE_NAME = "stress-test-coupon-queue";

	private final RedisTemplate<String, Long> redisTemplate;

	public void enqueue(Long userId) {
		redisTemplate.opsForList().rightPush(QUEUE_NAME, userId);
	}

	public Long dequeue() {
		return redisTemplate.opsForList().leftPop(QUEUE_NAME);
	}
}
