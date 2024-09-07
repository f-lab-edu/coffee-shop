package com.coffee_shop.coffeeshop.domain.coupon;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Getter;

@Getter
public enum CouponType {
	PERCENTAGE("PERCENTAGE", "비율 할인"),
	AMOUNT("AMOUNT", "금액 할인");

	private final String code;
	private final String name;

	CouponType(String code, String name) {
		this.code = code;
		this.name = name;
	}

	@JsonCreator
	public static CouponType of(String type) {
		return Arrays.stream(CouponType.values())
			.filter(couponType -> couponType.code.equals(type))
			.findFirst()
			.orElse(null);
	}
}
