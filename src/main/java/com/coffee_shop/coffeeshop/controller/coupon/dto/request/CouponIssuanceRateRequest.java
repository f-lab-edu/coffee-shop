package com.coffee_shop.coffeeshop.controller.coupon.dto.request;

import jakarta.validation.constraints.Positive;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CouponIssuanceRateRequest {
	@Positive(message = "발급처리량은 양수입니다.")
	private int issuanceRate;
}
