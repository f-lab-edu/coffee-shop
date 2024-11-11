package com.coffee_shop.coffeeshop.domain.coupon.repository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class AppliedUserRepository {
	private static final String COUPON_APPLIED_USER_KEY_PREFIX = "coupon_applied_user:";

	private final RedisTemplate<String, Long> redisTemplate;

	public void add(Long userId) {
		redisTemplate
			.opsForSet()
			.add(getKey(userId), userId);
	}

	public Boolean isMember(Long userId) {
		return redisTemplate
			.opsForSet()
			.isMember(getKey(userId), userId);
	}

	private String getKey(Long userId) {
		return COUPON_APPLIED_USER_KEY_PREFIX + userId;
	}
}
