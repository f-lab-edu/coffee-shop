package com.coffee_shop.coffeeshop.domain.coupon;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.coffee_shop.coffeeshop.domain.cash.Cash;

class CouponTest {
	@DisplayName("쿠폰 타입이 비율 할인인 경우 쿠폰 생성시 소수점으로 변환한다.")
	@Test
	void convertDiscountAmount() {
		//when
		Coupon coupon = Coupon.builder()
			.name("오픈할인 쿠폰")
			.type(CouponType.PERCENTAGE)
			.discountAmount(10)
			.minOrderAmount(1000)
			.build();

		//then
		assertThat(coupon.getDiscountAmount()).isEqualTo(Cash.of("0.1"));
	}

	@DisplayName("발급된 쿠폰 개수가 최대 발급수보다 작을 경우 발급이 가능하다.")
	@Test
	void canIssueCoupon() {
		//given
		Coupon coupon = createCoupon(10, 9);

		//when, then
		assertTrue(coupon.canIssueCoupon());
	}

	@DisplayName("발급된 쿠폰 개수가 최대 발급수보다 클 경우 발급이 불가능하다.")
	@Test
	void soldOutCoupon() {
		//given
		Coupon coupon = createCoupon(10, 10);

		//when, then
		assertFalse(coupon.canIssueCoupon());
	}

	@DisplayName("쿠폰이 발급되면 발급 개수를 +1 한다.")
	@Test
	void issueCoupon() {
		//given
		Coupon coupon = createCoupon(10, 1);

		//when
		coupon.issueCoupon();

		//then
		assertThat(coupon.getIssuedCount()).isEqualTo(2);
	}

	private Coupon createCoupon(int maxIssueCount, int issuedCount) {
		return Coupon.builder()
			.name("오픈할인 쿠폰")
			.type(CouponType.AMOUNT)
			.discountAmount(100)
			.minOrderAmount(1000)
			.maxIssueCount(maxIssueCount)
			.issuedCount(issuedCount)
			.build();
	}

}
