package com.coffee_shop.coffeeshop.domain.coupon.producer;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.coffee_shop.coffeeshop.domain.coupon.Coupon;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponIssueRepository;
import com.coffee_shop.coffeeshop.domain.user.User;
import com.coffee_shop.coffeeshop.service.coupon.dto.request.CouponApplication;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Primary
@Slf4j
@RequiredArgsConstructor
@Component
public class RedisCouponProducer implements CouponProducer {
	private static final int POSITION_NOT_FOUND = -1;
	private final CouponIssueRepository couponIssueRepository;

	@Override
	public void applyCoupon(User user, Coupon coupon) {
		long timestamp = System.currentTimeMillis();
		couponIssueRepository.add(CouponApplication.of(user, coupon), timestamp);
	}

	@Override
	public int getPosition(Long userId, Long couponId) {
		return POSITION_NOT_FOUND;
	}

	@Override
	public boolean isPositionNotFound(int position) {
		return position == POSITION_NOT_FOUND;
	}
}
