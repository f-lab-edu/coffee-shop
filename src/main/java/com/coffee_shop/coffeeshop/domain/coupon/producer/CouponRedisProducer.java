package com.coffee_shop.coffeeshop.domain.coupon.producer;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.coffee_shop.coffeeshop.domain.coupon.Coupon;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponIssueRepository;
import com.coffee_shop.coffeeshop.domain.user.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class CouponRedisProducer implements CouponProducer {
	private static final int POSITION_NOT_FOUND = -1;
	private final CouponIssueRepository couponIssueRepository;

	@Override
	public void applyCoupon(User user, Coupon coupon, LocalDateTime issueDateTime) {
		long timestamp = System.currentTimeMillis();
		couponIssueRepository.add(coupon, user, timestamp);
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
