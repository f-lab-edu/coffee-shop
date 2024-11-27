package com.coffee_shop.coffeeshop.domain.coupon.repository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

	public Long getIssueCount(Long couponId) {
		Long issueCount = redisTemplate
			.opsForValue()
			.get(getKey(couponId));

		if (issueCount == null) {
			log.warn("Redis key '{}' does not exist or used in transaction/pipeline",
				COUPON_COUNT_KEY_PREFIX + couponId);
			return 0L;
		}

		return issueCount;
	}

	private String getKey(Long couponId) {
		return COUPON_COUNT_KEY_PREFIX + couponId;
	}
}
