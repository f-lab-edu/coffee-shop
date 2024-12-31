package com.coffee_shop.coffeeshop.domain.coupon.repository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Repository
public class CouponIssuanceRateRepository {
	private static final int DEFAULT_RANGE_COUNT = 10;
	private static final String COUPON_ISSUANCE_RATE_KEY = "coupon_issuance_rate";

	private final RedisTemplate<String, Integer> redisTemplateInteger;

	public int getIssuanceRate() {
		Integer rate = redisTemplateInteger.opsForValue().get(COUPON_ISSUANCE_RATE_KEY);
		if (rate == null) {
			return DEFAULT_RANGE_COUNT;
		}

		return rate;
	}

	public void changeIssuanceRate(int issuanceRate) {
		redisTemplateInteger.opsForValue().set(COUPON_ISSUANCE_RATE_KEY, issuanceRate);
	}
}
