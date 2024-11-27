package com.coffee_shop.coffeeshop.service.coupon;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.coffee_shop.coffeeshop.common.exception.BusinessException;
import com.coffee_shop.coffeeshop.domain.coupon.Coupon;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponRepository;
import com.coffee_shop.coffeeshop.exception.ErrorCode;
import com.coffee_shop.coffeeshop.service.coupon.dto.request.CouponSaveServiceRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void syncCouponIssuedCount(Long couponId, Long increment) {
		Coupon coupon = findCoupon(couponId);
		coupon.updateIssuedCount(increment);
	}

	private Coupon findCoupon(Long couponId) {
		return couponRepository.findById(couponId)
			.orElseThrow(
				() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "Coupon Not Found, 쿠폰 ID : " + couponId));
	}
}
