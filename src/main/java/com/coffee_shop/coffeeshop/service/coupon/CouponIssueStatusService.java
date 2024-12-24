package com.coffee_shop.coffeeshop.service.coupon;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.coffee_shop.coffeeshop.common.exception.BusinessException;
import com.coffee_shop.coffeeshop.domain.coupon.Coupon;
import com.coffee_shop.coffeeshop.domain.coupon.CouponIssueFailHistory;
import com.coffee_shop.coffeeshop.domain.coupon.CouponIssueStatus;
import com.coffee_shop.coffeeshop.domain.coupon.CouponTransactionHistory;
import com.coffee_shop.coffeeshop.domain.coupon.producer.CouponProducer;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponIssueFailHistoryRepository;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponRepository;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponTransactionHistoryRepository;
import com.coffee_shop.coffeeshop.domain.user.User;
import com.coffee_shop.coffeeshop.domain.user.UserRepository;
import com.coffee_shop.coffeeshop.exception.ErrorCode;
import com.coffee_shop.coffeeshop.service.coupon.dto.response.CouponApplyResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class CouponIssueStatusService {
	private final UserRepository userRepository;
	private final CouponRepository couponRepository;
	private final CouponProducer couponProducer;
	private final CouponTransactionHistoryRepository couponTransactionHistoryRepository;
	private final CouponIssueFailHistoryRepository couponIssueFailHistoryRepository;

	public CouponApplyResponse isCouponIssued(Long userId, Long couponId) {
		User user = findUser(userId);
		Coupon coupon = findCoupon(couponId);

		Optional<CouponTransactionHistory> transactionHistory = findCouponTransactionHistory(coupon, user);
		if (transactionHistory.isPresent()) {
			return CouponApplyResponse.of(CouponIssueStatus.SUCCESS);
		}

		Optional<CouponIssueFailHistory> failHistory = findCouponIssueFailHistory(coupon, user);
		if (failHistory.isPresent()) {
			return CouponApplyResponse.of(CouponIssueStatus.FAILURE);
		}

		int position = 0;
		try {
			position = couponProducer.getPosition(user, coupon);
		} catch (BusinessException e) {
			log.warn("Position not found for userId: {}, couponId: {} in the coupon application queue.", user.getId(),
				coupon.getId());
		}

		return CouponApplyResponse.of(CouponIssueStatus.IN_PROGRESS, position);
	}

	private Optional<CouponIssueFailHistory> findCouponIssueFailHistory(Coupon coupon, User user) {
		return couponIssueFailHistoryRepository.findByCouponAndUser(coupon,
			user);
	}

	private Optional<CouponTransactionHistory> findCouponTransactionHistory(Coupon coupon, User user) {
		return couponTransactionHistoryRepository.findByCouponAndUser(coupon, user);
	}

	private Coupon findCoupon(Long couponId) {
		return couponRepository.findById(couponId)
			.orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
	}

	private User findUser(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
	}
}
