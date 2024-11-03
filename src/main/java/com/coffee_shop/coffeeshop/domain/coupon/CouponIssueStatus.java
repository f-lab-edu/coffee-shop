package com.coffee_shop.coffeeshop.domain.coupon;

public enum CouponIssueStatus {
	SUCCESS("SUCCESS", "발급 성공"),
	IN_PROGRESS("IN_PROGRESS", "발급중"),
	FAILURE("FAILURE", "발급 실패");
	private final String code;
	private final String name;

	CouponIssueStatus(String code, String name) {
		this.code = code;
		this.name = name;
	}
}
