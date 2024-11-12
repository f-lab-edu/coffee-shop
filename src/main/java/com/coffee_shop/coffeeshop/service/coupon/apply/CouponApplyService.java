package com.coffee_shop.coffeeshop.service.coupon.apply;

import java.time.LocalDateTime;

import com.coffee_shop.coffeeshop.service.coupon.dto.request.CouponApplyServiceRequest;
import com.coffee_shop.coffeeshop.service.coupon.dto.response.CouponApplyResponse;

public interface CouponApplyService {
	CouponApplyResponse isCouponIssued(Long userId, Long couponId);

	void applyCoupon(CouponApplyServiceRequest request, LocalDateTime issueDateTime);

}
