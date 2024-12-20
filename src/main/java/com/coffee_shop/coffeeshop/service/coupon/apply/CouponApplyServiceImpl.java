package com.coffee_shop.coffeeshop.service.coupon.apply;

import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coffee_shop.coffeeshop.common.exception.BusinessException;
import com.coffee_shop.coffeeshop.domain.coupon.Coupon;
import com.coffee_shop.coffeeshop.domain.coupon.CouponIssueStatus;
import com.coffee_shop.coffeeshop.domain.coupon.CouponTransactionHistory;
import com.coffee_shop.coffeeshop.domain.coupon.producer.CouponMessageQProducer;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponRepository;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponTransactionHistoryRepository;
import com.coffee_shop.coffeeshop.domain.user.User;
import com.coffee_shop.coffeeshop.domain.user.UserRepository;
import com.coffee_shop.coffeeshop.exception.ErrorCode;
import com.coffee_shop.coffeeshop.service.coupon.dto.request.CouponApplyServiceRequest;
import com.coffee_shop.coffeeshop.service.coupon.dto.response.CouponApplyResponse;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
@ConditionalOnProperty(name = "schedule.active", havingValue = "messageQ")
public class CouponApplyServiceImpl implements CouponApplyService {
	private final UserRepository userRepository;
	private final CouponRepository couponRepository;
	private final CouponMessageQProducer couponMessageQProducer;
	private final CouponTransactionHistoryRepository couponTransactionHistoryRepository;

	public CouponApplyResponse isCouponIssued(Long userId, Long couponId) {
		User user = findUser(userId);
		Coupon coupon = findCoupon(couponId);

		Optional<CouponTransactionHistory> history = couponTransactionHistoryRepository.findByCouponAndUser(
			coupon, user);

		if (history.isPresent()) {
			return CouponApplyResponse.of(CouponIssueStatus.SUCCESS);
		}

		try {
			int position = couponMessageQProducer.getPosition(user, coupon);
			return CouponApplyResponse.of(CouponIssueStatus.IN_PROGRESS, position);
		} catch (BusinessException e) {
			return CouponApplyResponse.of(CouponIssueStatus.FAILURE);
		}
	}

	public void applyCoupon(CouponApplyServiceRequest request) {
		User user = findUser(request.getUserId());
		Coupon coupon = findCoupon(request.getCouponId());

		if (!coupon.isCouponLimitExceeded()) {
			throw new BusinessException(ErrorCode.COUPON_LIMIT_REACHED);
		}

		checkDuplicateIssuedCoupon(coupon, user);

		couponMessageQProducer.applyCoupon(user, coupon);
	}

	private void checkDuplicateIssuedCoupon(Coupon coupon, User user) {
		couponTransactionHistoryRepository.findByCouponAndUser(coupon, user)
			.ifPresent(couponTransactionHistory -> {
				throw new BusinessException(ErrorCode.COUPON_DUPLICATE_ISSUE,
					"사용자 ID = " + user.getId() + ", 사용자 이름 = " + user.getName());
			});
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
