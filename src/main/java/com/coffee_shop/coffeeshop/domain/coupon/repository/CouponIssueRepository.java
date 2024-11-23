package com.coffee_shop.coffeeshop.domain.coupon.repository;

import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import com.coffee_shop.coffeeshop.service.coupon.dto.request.CouponApplication;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class CouponIssueRepository {
	private static final String COUPON_QUEUE_KEY_PREFIX = "coupon_issue_queue";
	private final RedisTemplate<String, Object> redisTemplate;

	public void add(CouponApplication application, long timestamp) {
		redisTemplate
			.opsForZSet()
			.add(COUPON_QUEUE_KEY_PREFIX, application, timestamp);
	}

	public Long count() {
		return redisTemplate
			.opsForZSet()
			.zCard(COUPON_QUEUE_KEY_PREFIX);
	}

	public boolean isEmpty() {
		return count() == 0L;
	}

	public Set<ZSetOperations.TypedTuple<Object>> popMin(long count) {
		return redisTemplate
			.opsForZSet()
			.popMin(COUPON_QUEUE_KEY_PREFIX, count);
	}
}
