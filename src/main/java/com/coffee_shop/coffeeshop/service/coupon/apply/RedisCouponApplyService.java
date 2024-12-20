package com.coffee_shop.coffeeshop.service.coupon.apply;

import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coffee_shop.coffeeshop.common.exception.BusinessException;
import com.coffee_shop.coffeeshop.domain.coupon.Coupon;
import com.coffee_shop.coffeeshop.domain.coupon.CouponIssueFailHistory;
import com.coffee_shop.coffeeshop.domain.coupon.CouponIssueStatus;
import com.coffee_shop.coffeeshop.domain.coupon.CouponTransactionHistory;
import com.coffee_shop.coffeeshop.domain.coupon.producer.RedisCouponProducer;
import com.coffee_shop.coffeeshop.domain.coupon.repository.AppliedUserRepository;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponIssueCountRepository;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponIssueFailHistoryRepository;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponRepository;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponTransactionHistoryRepository;
import com.coffee_shop.coffeeshop.domain.user.User;
import com.coffee_shop.coffeeshop.domain.user.UserRepository;
import com.coffee_shop.coffeeshop.exception.ErrorCode;
import com.coffee_shop.coffeeshop.service.coupon.CouponService;
import com.coffee_shop.coffeeshop.service.coupon.dto.request.CouponApplication;
import com.coffee_shop.coffeeshop.service.coupon.dto.request.CouponApplyServiceRequest;
import com.coffee_shop.coffeeshop.service.coupon.dto.response.CouponApplyResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
@ConditionalOnProperty(name = "schedule.active", havingValue = "redis")
public class RedisCouponApplyService implements CouponApplyService {
	private final UserRepository userRepository;
	private final CouponRepository couponRepository;
	private final RedisCouponProducer redisCouponProducer;
	private final CouponTransactionHistoryRepository couponTransactionHistoryRepository;
	private final CouponIssueFailHistoryRepository couponIssueFailHistoryRepository;
	private final CouponIssueCountRepository couponIssueCountRepository;
	private final AppliedUserRepository appliedUserRepository;
	private final CouponService couponService;
	private final CouponFindService couponFindService;

	@Transactional(readOnly = true)
	public CouponApplyResponse isCouponIssued(Long userId, Long couponId) {
		User user = findUser(userId);
		Coupon coupon = findCoupon(couponId);

		Optional<CouponTransactionHistory> transactionHistory = couponTransactionHistoryRepository.findByCouponAndUser(
			coupon, user);

		if (transactionHistory.isPresent()) {
			return CouponApplyResponse.of(CouponIssueStatus.SUCCESS);
		}

		Optional<CouponIssueFailHistory> failHistory = couponIssueFailHistoryRepository.findByCouponAndUser(coupon,
			user);

		if (failHistory.isPresent()) {
			return CouponApplyResponse.of(CouponIssueStatus.FAILURE);
		}

		int position = 0;
		try {
			position = redisCouponProducer.getPosition(user, coupon);
		} catch (BusinessException e) {
			log.warn("Position not found for userId: {}, couponId: {} in the coupon application queue.", user.getId(),
				coupon.getId());
		}

		return CouponApplyResponse.of(CouponIssueStatus.IN_PROGRESS, position);
	}

	public void applyCoupon(CouponApplyServiceRequest request) {
		Coupon coupon = couponFindService.findCoupon(request.getCouponId());

		checkDuplicateIssuedCoupon(request.getUserId());

		Long issueCount = couponIssueCountRepository.getIssueCount(coupon.getId());
		isCouponLimitExceeded(coupon, issueCount);

		redisCouponProducer.applyCoupon(CouponApplication.of(request.getUserId(), request.getCouponId()));
	}

	private void isCouponLimitExceeded(Coupon coupon, Long issueCount) {
		if (issueCount + 1 > coupon.getMaxIssueCount()) {
			throw new BusinessException(ErrorCode.COUPON_LIMIT_REACHED);
		}
	}

	private void checkDuplicateIssuedCoupon(Long userId) {
		Boolean isIssuedCoupon = appliedUserRepository.isMember(userId);

		if (isIssuedCoupon != null && isIssuedCoupon) {
			throw new BusinessException(ErrorCode.COUPON_DUPLICATE_ISSUE,
				"사용자 ID = " + userId);
		}
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
