package com.coffee_shop.coffeeshop.service.coupon.issue.fail;

import com.coffee_shop.coffeeshop.service.coupon.dto.request.CouponApplication;

public interface CouponIssueFailHandler {
	void handleFail(CouponApplication couponApplication, Exception exception);
}
