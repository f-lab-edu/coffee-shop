package com.coffee_shop.coffeeshop.service.coupon;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coffee_shop.coffeeshop.domain.coupon.Coupon;
import com.coffee_shop.coffeeshop.domain.coupon.CouponRepository;
import com.coffee_shop.coffeeshop.service.coupon.dto.request.CouponSaveServiceRequest;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class CouponService {
	private final CouponRepository couponRepository;

	@Transactional
	public Long createCoupon(CouponSaveServiceRequest request) {
		Coupon savedCoupon = couponRepository.save(Coupon.of(request));
		return savedCoupon.getId();
	}
}
