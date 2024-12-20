package com.coffee_shop.coffeeshop.domain.coupon.repository;

import org.springframework.stereotype.Repository;

import com.coffee_shop.coffeeshop.common.domain.RedisRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Repository
public class CouponFailCountRepository {
	private static final String COUPON_FAIL_COUNT_KEY_PREFIX = "coupon_fail_count:";
	private final RedisRepository redisRepository;

	public Long increment(Long userId) {
		return redisRepository.increment(getKey(userId));
	}

	public Long getIssueCount(Long userId) {
		return redisRepository.getIssueCount(getKey(userId));
	}

	private String getKey(Long userId) {
		return COUPON_FAIL_COUNT_KEY_PREFIX + userId;
	}
}
