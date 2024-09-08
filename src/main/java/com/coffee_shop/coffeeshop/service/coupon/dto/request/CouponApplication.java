package com.coffee_shop.coffeeshop.service.coupon.dto.request;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CouponApplication {
	private final Long userId;
	private final Long couponId;
	private final LocalDateTime issueDateTime;
}
