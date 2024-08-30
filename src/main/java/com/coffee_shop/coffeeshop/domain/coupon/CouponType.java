package com.coffee_shop.coffeeshop.domain.coupon;

import lombok.Getter;

@Getter
public enum CouponType {
	PERCENTAGE("비율 할인"),
	AMOUNT("금액 할인");

	private final String name;

	CouponType(String name) {
		this.name = name;
	}
}
