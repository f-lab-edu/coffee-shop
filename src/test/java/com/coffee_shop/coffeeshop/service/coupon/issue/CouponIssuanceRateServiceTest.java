package com.coffee_shop.coffeeshop.service.coupon.issue;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import com.coffee_shop.coffeeshop.service.IntegrationTestSupport;

class CouponIssuanceRateServiceTest extends IntegrationTestSupport {
	@Autowired
	private CouponIssuanceRateService couponIssuanceRateService;

	@Autowired
	private CacheManager cacheManager;

	@DisplayName("쿠폰 발급 처리량을 변경해서 캐시에 저장한다.")
	@Test
	void changeCouponIssuanceRate() {
		//when
		int issuanceRate = couponIssuanceRateService.changeIssuanceRate(30);

		//then
		assertThat(issuanceRate).isEqualTo(30);

		Cache cache = cacheManager.getCache("couponIssuanceRate");
		assertThat(cache).isNotNull();
		assertThat(cache.get("rate", Integer.class)).isEqualTo(30);
	}

	@DisplayName("쿠폰 발급 처리량을 캐시에서 가져온다.")
	@Test
	void getCouponIssuanceRate() {
		//when
		couponIssuanceRateService.changeIssuanceRate(30);

	}
}