package com.coffee_shop.coffeeshop.service.coupon.issue;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponIssuanceRateRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class CouponIssuanceRateService {
	private static final int MAX_RANGE_COUNT = 50;
	private static final int DEFAULT_RANGE_COUNT = 10;
	private final CouponIssuanceRateRepository couponIssuanceRateRepository;

	@CachePut(value = "couponIssuanceRate", key = "'rate'")
	public int changeIssuanceRate(int rate) {
		int issuanceRate = adjustIssuanceRate(rate);
		couponIssuanceRateRepository.changeIssuanceRate(issuanceRate);
		return issuanceRate;
	}

	@Cacheable(value = "couponIssuanceRate", key = "'rate'")
	public int getIssuanceRate() {
		return couponIssuanceRateRepository.getIssuanceRate();
	}

	private int adjustIssuanceRate(int rate) {
		if (rate == 0) {
			rate = DEFAULT_RANGE_COUNT;
			return rate;
		}

		if (rate > MAX_RANGE_COUNT) {
			rate = MAX_RANGE_COUNT;
			return rate;
		}

		return rate;
	}
}
