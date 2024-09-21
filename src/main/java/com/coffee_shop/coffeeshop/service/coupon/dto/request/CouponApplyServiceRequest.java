package com.coffee_shop.coffeeshop.service.coupon.dto.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CouponApplyServiceRequest {

	private Long userId;

	private Long couponId;

	@Builder
	private CouponApplyServiceRequest(Long userId, Long couponId) {
		this.userId = userId;
		this.couponId = couponId;
	}
}
