package com.coffee_shop.coffeeshop.service.coupon.dto.request;

import com.coffee_shop.coffeeshop.domain.cash.Cash;
import com.coffee_shop.coffeeshop.domain.coupon.Coupon;
import com.coffee_shop.coffeeshop.domain.coupon.CouponType;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CouponSaveServiceRequest {
	private String name;
	private CouponType type;
	private int discountAmount;
	private int minOrderAmount;
	private int maxIssueCount;

	@Builder
	private CouponSaveServiceRequest(String name, CouponType type, int discountAmount, int minOrderAmount,
		int maxIssueCount) {
		this.name = name;
		this.type = type;
		this.discountAmount = discountAmount;
		this.minOrderAmount = minOrderAmount;
		this.maxIssueCount = maxIssueCount;
	}

	public Coupon toEntity() {
		return Coupon.builder()
			.name(name)
			.type(type)
			.discountAmount(convertDiscountAmount())
			.minOrderAmount(minOrderAmount)
			.maxIssueCount(maxIssueCount)
			.build();
	}

	private Cash convertDiscountAmount() {
		if (type == CouponType.PERCENTAGE) {
			return Cash.of(discountAmount).divide(100);
		}

		return Cash.of(discountAmount);
	}
}
