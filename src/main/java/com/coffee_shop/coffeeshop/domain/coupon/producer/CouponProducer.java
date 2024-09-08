package com.coffee_shop.coffeeshop.domain.coupon.producer;

import java.time.LocalDateTime;

import com.coffee_shop.coffeeshop.domain.coupon.Coupon;
import com.coffee_shop.coffeeshop.domain.user.User;

public interface CouponProducer {
	void applyCoupon(User user, Coupon coupon, LocalDateTime issueDateTime);
}
