package com.coffee_shop.coffeeshop.service.coupon.apply;

import org.springframework.stereotype.Service;

import com.coffee_shop.coffeeshop.common.exception.BusinessException;
import com.coffee_shop.coffeeshop.domain.coupon.Coupon;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponRepository;
import com.coffee_shop.coffeeshop.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class CouponFindService {
	public final CouponRepository couponRepository;

	public Coupon findCoupon(Long couponId) {
		return couponRepository.findById(couponId)
			.orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
	}
}
