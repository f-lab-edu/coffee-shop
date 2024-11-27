package com.coffee_shop.coffeeshop.domain.coupon.repository;

import org.springframework.stereotype.Repository;

import com.coffee_shop.coffeeshop.common.domain.RedisIncrRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Repository
public class CouponIssueCountRepository {
	private static final String COUPON_COUNT_KEY_PREFIX = "coupon_issue_count:";
	private final RedisIncrRepository redisIncrRepository;

	public Long increment(Long couponId) {
		return redisIncrRepository.increment(getKey(couponId));
	}

	public Long getIssueCount(Long couponId) {
		return redisIncrRepository.getIssueCount(getKey(couponId));
	}

	private String getKey(Long couponId) {
		return COUPON_COUNT_KEY_PREFIX + couponId;
	}
}
