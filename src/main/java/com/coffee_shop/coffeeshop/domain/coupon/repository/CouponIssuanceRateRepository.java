package com.coffee_shop.coffeeshop.domain.coupon.repository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Repository
public class CouponIssuanceRateRepository {
	private static final long MAX_RANGE_COUNT = 50L;
	private static final long DEFAULT_RANGE_COUNT = 10L;
	private static final String COUPON_ISSUANCE_RATE_KEY_PREFIX = "coupon_issuance_rate";

	private final RedisTemplate<String, String> redisTemplateString;

	public long getRangeCount() {
		long rangeCount = DEFAULT_RANGE_COUNT;
		String issuanceRate = null;

		try {
			issuanceRate = getIssuanceRate();
			rangeCount = Long.parseLong(issuanceRate);
			if (rangeCount > MAX_RANGE_COUNT) {
				rangeCount = MAX_RANGE_COUNT;
				log.warn(
					"Coupon issuance rate exceeded. Current rate: {}, Max rate: {}. Action required to adjust configuration or throttle requests.",
					rangeCount, MAX_RANGE_COUNT);
			}
		} catch (NumberFormatException e) {
			log.warn(
				"Invalid number format for conversion to long. Provided input: '{}'.", issuanceRate);
		}

		return rangeCount;
	}

	private String getIssuanceRate() {
		return redisTemplateString.opsForValue().get(COUPON_ISSUANCE_RATE_KEY_PREFIX);
	}
}
