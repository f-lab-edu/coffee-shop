package com.coffee_shop.coffeeshop.domain.coupon.repository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.coffee_shop.coffeeshop.domain.coupon.Coupon;
import com.coffee_shop.coffeeshop.domain.user.User;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class CouponIssueRepository {
	private static final String COUPON_QUEUE_KEY_PREFIX = "coupon_issue_queue:";
	private final RedisTemplate<String, Long> redisTemplate;

	public void add(Coupon coupon, User user, long timestamp) {
		redisTemplate
			.opsForZSet()
			.add(getKey(coupon), user.getId(), timestamp);
	}

	public Long count(Coupon coupon) {
		return redisTemplate
			.opsForZSet()
			.zCard(getKey(coupon));
	}

	private String getKey(Coupon coupon) {
		return COUPON_QUEUE_KEY_PREFIX + coupon.getId();
	}
}
