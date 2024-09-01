package com.coffee_shop.coffeeshop.domain.coupon;

import java.util.Arrays;

import com.coffee_shop.coffeeshop.common.exception.InvalidValueException;
import com.coffee_shop.coffeeshop.exception.ErrorCode;

import lombok.Getter;

@Getter
public enum CouponType {
	PERCENTAGE("비율 할인"),
	AMOUNT("금액 할인");

	private final String name;

	CouponType(String name) {
		this.name = name;
	}

	public static CouponType of(String type) {
		return Arrays.stream(CouponType.values())
			.filter(couponType -> couponType.getName().equals(type))
			.findFirst()
			.orElseThrow(() -> new InvalidValueException(ErrorCode.COUPON_TYPE_NOT_FOUND));
	}
}
