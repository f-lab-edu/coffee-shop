package com.coffee_shop.coffeeshop.service.coupon.issue.fail;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coffee_shop.coffeeshop.common.exception.BusinessException;
import com.coffee_shop.coffeeshop.domain.coupon.Coupon;
import com.coffee_shop.coffeeshop.domain.coupon.CouponIssueFailHistory;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponIssueFailHistoryRepository;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponRepository;
import com.coffee_shop.coffeeshop.domain.user.User;
import com.coffee_shop.coffeeshop.domain.user.UserRepository;
import com.coffee_shop.coffeeshop.exception.ErrorCode;
import com.coffee_shop.coffeeshop.service.coupon.dto.request.CouponApplication;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CouponIssueFailHistoryService {
	private final UserRepository userRepository;
	private final CouponRepository couponRepository;
	private final CouponIssueFailHistoryRepository couponIssueFailHistoryRepository;

	@Transactional
	public void saveCouponFailHistory(CouponApplication couponApplication) {
		Coupon coupon = findCoupon(couponApplication.getCouponId());
		User user = findUser(couponApplication.getUserId());

		CouponIssueFailHistory history = CouponIssueFailHistory.of(user, coupon,
			LocalDateTime.now());
		couponIssueFailHistoryRepository.save(history);
	}

	private Coupon findCoupon(Long couponId) {
		return couponRepository.findById(couponId)
			.orElseThrow(
				() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "Coupon Not Found, 쿠폰 ID : " + couponId));
	}

	private User findUser(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "User Not Found, 사용자 ID : " + userId));
	}
}
