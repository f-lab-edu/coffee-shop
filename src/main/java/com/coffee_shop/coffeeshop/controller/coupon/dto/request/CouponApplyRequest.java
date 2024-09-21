package com.coffee_shop.coffeeshop.controller.coupon.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import com.coffee_shop.coffeeshop.service.coupon.dto.request.CouponApplyServiceRequest;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CouponApplyRequest {

	@NotNull(message = "사용자 ID는 필수입니다.")
	@Positive(message = "사용자 ID는 양수입니다.")
	private Long userId;

	@NotNull(message = "쿠폰 ID는 필수입니다.")
	@Positive(message = "쿠폰 ID는 양수입니다.")
	private Long couponId;

	@Builder
	private CouponApplyRequest(Long userId, Long couponId) {
		this.userId = userId;
		this.couponId = couponId;
	}

	public CouponApplyServiceRequest toServiceRequest() {
		return CouponApplyServiceRequest.builder()
			.userId(userId)
			.couponId(couponId)
			.build();
	}
}
