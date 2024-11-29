package com.coffee_shop.coffeeshop.domain.coupon.producer;

import com.coffee_shop.coffeeshop.domain.coupon.Coupon;
import com.coffee_shop.coffeeshop.domain.user.User;

public interface CouponProducer {
	void applyCoupon(User user, Coupon coupon);

	int getPosition(User user, Coupon coupon);
}
