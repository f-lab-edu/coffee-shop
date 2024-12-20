package com.coffee_shop.coffeeshop.domain.coupon.producer;

import org.springframework.stereotype.Component;

import com.coffee_shop.coffeeshop.common.exception.BusinessException;
import com.coffee_shop.coffeeshop.domain.coupon.Coupon;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponIssueRepository;
import com.coffee_shop.coffeeshop.domain.user.User;
import com.coffee_shop.coffeeshop.exception.ErrorCode;
import com.coffee_shop.coffeeshop.service.coupon.dto.request.CouponApplication;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class RedisCouponProducer {
	private final CouponIssueRepository couponIssueRepository;

	public void applyCoupon(CouponApplication couponApplication) {
		long timestamp = System.currentTimeMillis();
		couponIssueRepository.add(couponApplication, timestamp);
	}

	public int getPosition(User user, Coupon coupon) {
		Long position = couponIssueRepository.findPosition(CouponApplication.of(user, coupon));
		if (position == null) {
			throw new BusinessException(ErrorCode.POSITION_NOT_FOUND);
		}

		return position.intValue() + 1;
	}
}
