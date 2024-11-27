package com.coffee_shop.coffeeshop.domain.coupon.repository;

import static com.coffee_shop.coffeeshop.domain.coupon.CouponType.*;
import static org.assertj.core.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.coffee_shop.coffeeshop.domain.coupon.Coupon;
import com.coffee_shop.coffeeshop.service.IntegrationTestSupport;

class CouponIssueCountRepositoryTest extends IntegrationTestSupport {
	@Autowired
	private CouponRepository couponRepository;

	@Autowired
	private CouponIssueCountRepository couponIssueCountRepository;

	@Autowired
	private RedisTemplate<String, Long> redisTemplate;

	@AfterEach
	void tearDown() {
		clearAll();
	}

	@DisplayName("쿠폰 발급 수를 증가시킨다.")
	@Test
	void increment() {
		//given
		Coupon coupon = createCoupon();

		//when
		Long increment = couponIssueCountRepository.increment(coupon.getId());

		//then
		assertThat(increment).isEqualTo(1L);
	}

	@DisplayName("쿠폰 발급 수를 조회할 때 키가 없을 경우 0을 반환한다.")
	@Test
	void getIssueCountWhenKeyDoesNotExist() {
		//given
		Coupon coupon = createCoupon();

		//when
		Long increment = couponIssueCountRepository.getIssueCount(coupon.getId());

		//then
		assertThat(increment).isEqualTo(0L);
	}

	private Coupon createCoupon() {
		Coupon coupon = Coupon.builder()
			.name("오픈기념 선착순 할인 쿠폰")
			.type(AMOUNT)
			.discountAmount(1000)
			.minOrderAmount(4000)
			.maxIssueCount(10)
			.issuedCount(0)
			.build();
		return couponRepository.save(coupon);
	}

	private void clearAll() {
		Set<String> keys = redisTemplate.keys("*");
		if (keys != null && !keys.isEmpty()) {
			redisTemplate.delete(keys);
		}
	}
}