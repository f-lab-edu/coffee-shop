package com.coffee_shop.coffeeshop.controller.coupon.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import com.coffee_shop.coffeeshop.common.exception.InvalidValueException;
import com.coffee_shop.coffeeshop.domain.coupon.CouponType;
import com.coffee_shop.coffeeshop.exception.ErrorCode;
import com.coffee_shop.coffeeshop.service.coupon.dto.request.CouponSaveServiceRequest;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CouponSaveRequest {
	@NotBlank(message = "쿠폰명은 필수입니다.")
	@Size(max = 30, message = "쿠폰명은 최대 30자까지 입력가능합니다.")
	private String name;

	@NotNull(message = "유효하지 않는 쿠폰타입입니다.")
	private CouponType type;

	@Positive(message = "할인금액은 양수입니다.")
	private int discountAmount;

	@PositiveOrZero(message = "주문 최소 금액은 0이상입니다.")
	private int minOrderAmount;

	@PositiveOrZero(message = "최대 발급개수는 0이상입니다. 0일경우 무제한으로 발급가능합니다.")
	private int maxIssueCount;

	@Builder
	private CouponSaveRequest(String name, CouponType type, int discountAmount, int minOrderAmount,
		int maxIssueCount) {
		this.name = name;
		this.type = type;
		this.discountAmount = discountAmount;
		this.minOrderAmount = minOrderAmount;
		this.maxIssueCount = maxIssueCount;
	}

	public CouponSaveServiceRequest toServiceRequest() {
		validateDiscountAmount(type);

		return CouponSaveServiceRequest.builder()
			.name(name)
			.type(type)
			.discountAmount(discountAmount)
			.minOrderAmount(minOrderAmount)
			.maxIssueCount(maxIssueCount)
			.build();
	}

	private void validateDiscountAmount(CouponType couponType) {
		if (couponType == CouponType.PERCENTAGE && (discountAmount < 1 || discountAmount > 100)) {
			throw new InvalidValueException(ErrorCode.INVALID_DISCOUNT_PERCENTAGE);
		}
	}
}
