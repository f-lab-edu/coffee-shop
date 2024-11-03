package com.coffee_shop.coffeeshop.service.coupon.dto.response;

import com.coffee_shop.coffeeshop.domain.coupon.CouponIssueStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CouponApplyResponse {
	private CouponIssueStatus couponIssueStatus;
	private int position;

	@Builder
	private CouponApplyResponse(CouponIssueStatus couponIssueStatus, int position) {
		this.couponIssueStatus = couponIssueStatus;
		this.position = position;
	}

	public static CouponApplyResponse of(CouponIssueStatus couponIssueStatus, int position) {
		return CouponApplyResponse.builder()
			.couponIssueStatus(couponIssueStatus)
			.position(position)
			.build();
	}

	public static CouponApplyResponse of(CouponIssueStatus couponIssueStatus) {
		return of(couponIssueStatus, -1);
	}
}
