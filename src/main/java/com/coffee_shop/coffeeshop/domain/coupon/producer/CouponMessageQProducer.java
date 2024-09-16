package com.coffee_shop.coffeeshop.domain.coupon.producer;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.coffee_shop.coffeeshop.domain.coupon.Coupon;
import com.coffee_shop.coffeeshop.domain.coupon.MessageQ;
import com.coffee_shop.coffeeshop.domain.user.User;
import com.coffee_shop.coffeeshop.service.coupon.dto.request.CouponApplication;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class CouponMessageQProducer implements CouponProducer {

	private final MessageQ messageQ;

	@Override
	public void applyCoupon(User user, Coupon coupon, LocalDateTime issueDateTime) {
		CouponApplication couponApplication = CouponApplication.createCouponApplication(user, coupon, issueDateTime);
		messageQ.addMessage(couponApplication);
	}
}
