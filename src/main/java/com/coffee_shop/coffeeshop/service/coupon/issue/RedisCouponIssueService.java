package com.coffee_shop.coffeeshop.service.coupon.issue;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coffee_shop.coffeeshop.common.exception.BusinessException;
import com.coffee_shop.coffeeshop.domain.coupon.Coupon;
import com.coffee_shop.coffeeshop.domain.coupon.CouponTransactionHistory;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponIssueCountRepository;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponRepository;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponTransactionHistoryRepository;
import com.coffee_shop.coffeeshop.domain.user.User;
import com.coffee_shop.coffeeshop.domain.user.UserRepository;
import com.coffee_shop.coffeeshop.exception.ErrorCode;
import com.coffee_shop.coffeeshop.service.coupon.dto.request.CouponApplication;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisCouponIssueService {
	private final UserRepository userRepository;
	private final CouponRepository couponRepository;
	private final CouponTransactionHistoryRepository couponTransactionHistoryRepository;
	private final CouponIssueCountRepository couponIssueCountRepository;

	@Transactional
	public Long issueCoupon(CouponApplication couponApplication) {
		Coupon coupon = findCoupon(couponApplication.getCouponId());
		User user = findUser(couponApplication.getUserId());

		Long issueCount = couponIssueCountRepository.getIssueCount(couponApplication.getCouponId());
		isCouponLimitExceeded(coupon, issueCount);

		couponTransactionHistoryRepository.save(
			CouponTransactionHistory.issueCoupon(user, coupon, LocalDateTime.now()));

		return couponIssueCountRepository.increment(coupon.getId());
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

	private void isCouponLimitExceeded(Coupon coupon, Long issueCount) {
		if (issueCount + 1 > coupon.getMaxIssueCount()) {
			throw new BusinessException(ErrorCode.COUPON_LIMIT_REACHED);
		}
	}
}
