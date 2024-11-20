package com.coffee_shop.coffeeshop.service.coupon.issue;

import com.coffee_shop.coffeeshop.service.coupon.dto.request.CouponApplication;

public interface CouponIssueService {
	void issueCoupon(CouponApplication couponApplication);
}
