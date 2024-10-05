package com.coffee_shop.coffeeshop.service.coupon.dto.response;

import java.time.LocalDateTime;

import com.coffee_shop.coffeeshop.domain.coupon.CouponIssueStatus;
import com.coffee_shop.coffeeshop.domain.coupon.CouponTransactionHistory;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;
import lombok.Getter;

@Getter
public class IssuedCouponResponse {
	private CouponIssueStatus result;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
	private LocalDateTime issuedDateTime;

	@Builder
	private IssuedCouponResponse(CouponIssueStatus result, LocalDateTime issuedDateTime) {
		this.result = result;
		this.issuedDateTime = issuedDateTime;
	}

	public static IssuedCouponResponse createSuccessResponse(CouponTransactionHistory history) {
		return IssuedCouponResponse.builder()
			.result(CouponIssueStatus.SUCCESS)
			.issuedDateTime(history.getIssueDateTime())
			.build();
	}

	public static IssuedCouponResponse createFailResponse() {
		return IssuedCouponResponse.builder()
			.result(CouponIssueStatus.FAIL)
			.build();
	}
}
