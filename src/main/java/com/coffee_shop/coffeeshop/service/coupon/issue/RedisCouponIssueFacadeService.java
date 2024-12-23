package com.coffee_shop.coffeeshop.service.coupon.issue;

import org.springframework.stereotype.Service;

import com.coffee_shop.coffeeshop.common.exception.BusinessException;
import com.coffee_shop.coffeeshop.service.coupon.CouponService;
import com.coffee_shop.coffeeshop.service.coupon.dto.request.CouponApplication;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisCouponIssueFacadeService {
	private final RedisCouponIssueService redisCouponIssueService;
	private final CouponService couponService;

	public void issueCoupon(CouponApplication couponApplication) {
		Long couponId = couponApplication.getCouponId();

		Long increment = redisCouponIssueService.issueCoupon(couponApplication);
		try {
			couponService.syncCouponIssuedCount(couponId, increment);
		} catch (BusinessException e) {
			log.warn(
				"Business logic failed during coupon issuance count sync for couponId {}. The Redis increment value was {}. Details: {}",
				couponId, increment, e.getMessage());
		} catch (Exception e) {
			log.warn(
				"Unexpected error occurred while synchronizing coupon issuance count to the database for couponId {}. "
					+ "The Redis increment value was {}. Details: {}",
				couponId, increment, e.getMessage(), e);
		}
	}
}
