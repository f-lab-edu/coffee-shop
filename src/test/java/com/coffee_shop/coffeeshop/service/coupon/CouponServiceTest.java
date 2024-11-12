package com.coffee_shop.coffeeshop.service.coupon;

import static com.coffee_shop.coffeeshop.domain.coupon.CouponType.*;
import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.coffee_shop.coffeeshop.domain.cash.Cash;
import com.coffee_shop.coffeeshop.domain.coupon.Coupon;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponRepository;
import com.coffee_shop.coffeeshop.service.IntegrationTestSupport;
import com.coffee_shop.coffeeshop.service.coupon.dto.request.CouponSaveServiceRequest;

class CouponServiceTest extends IntegrationTestSupport {

	@Autowired
	private CouponService couponService;

	@Autowired
	private CouponRepository couponRepository;

	@AfterEach
	void tearDown() {
		couponRepository.deleteAllInBatch();
	}

	@DisplayName("금액 할인 쿠폰을 생성한다.")
	@Test
	void createAmountTypeCoupon() {
		//given
		CouponSaveServiceRequest request = CouponSaveServiceRequest.builder()
			.name("오픈기념 선착순 할인 쿠폰")
			.type(AMOUNT)
			.discountAmount(1000)
			.minOrderAmount(4000)
			.maxIssueCount(1000)
			.build();

		//when
		Long couponId = couponService.createCoupon(request);

		//then
		assertThat(couponId).isNotNull();

		List<Coupon> coupons = couponRepository.findAll();
		assertThat(coupons).hasSize(1)
			.extracting("id", "type", "discountAmount", "minOrderAmount", "maxIssueCount", "issuedCount")
			.containsExactlyInAnyOrder(tuple(couponId, AMOUNT, Cash.of(1000), 4000, 1000, 0));
	}

	@DisplayName("비율 할인 쿠폰을 생성한다.")
	@Test
	void createPercentageTypeCoupon() {
		//given
		CouponSaveServiceRequest request = CouponSaveServiceRequest.builder()
			.name("오픈기념 선착순 할인 쿠폰")
			.type(PERCENTAGE)
			.discountAmount(50)
			.minOrderAmount(4000)
			.maxIssueCount(1000)
			.build();

		//when
		Long couponId = couponService.createCoupon(request);

		//then
		assertThat(couponId).isNotNull();

		List<Coupon> coupons = couponRepository.findAll();
		assertThat(coupons).hasSize(1)
			.extracting("id", "type", "discountAmount", "minOrderAmount", "maxIssueCount", "issuedCount")
			.containsExactlyInAnyOrder(tuple(couponId, PERCENTAGE, Cash.of("0.5"), 4000, 1000, 0));
	}
}
