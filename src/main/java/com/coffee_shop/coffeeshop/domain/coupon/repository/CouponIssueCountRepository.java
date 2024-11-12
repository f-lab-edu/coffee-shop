package com.coffee_shop.coffeeshop.domain.coupon.repository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class CouponIssueCountRepository {
	private static final String COUPON_COUNT_KEY_PREFIX = "coupon_issue_count:";
	private final RedisTemplate<String, Long> redisTemplate;

	public Long increment(Long couponId) {
		return redisTemplate
			.opsForValue()
			.increment(getKey(couponId));
	}

	private String getKey(Long couponId) {
		return COUPON_COUNT_KEY_PREFIX + couponId;
	}
}
