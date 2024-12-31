package com.coffee_shop.coffeeshop.service.coupon.dto.response;

import java.io.Serializable;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CouponIssuanceRateResponse implements Serializable {
	private int issuanceRate;

	@Builder
	private CouponIssuanceRateResponse(int issuanceRate) {
		this.issuanceRate = issuanceRate;
	}

	public static CouponIssuanceRateResponse of(int issuanceRate) {
		return CouponIssuanceRateResponse.builder()
			.issuanceRate(issuanceRate)
			.build();
	}
}
