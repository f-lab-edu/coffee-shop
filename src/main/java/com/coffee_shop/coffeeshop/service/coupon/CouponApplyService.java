package com.coffee_shop.coffeeshop.service.coupon;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coffee_shop.coffeeshop.common.exception.BusinessException;
import com.coffee_shop.coffeeshop.domain.coupon.Coupon;
import com.coffee_shop.coffeeshop.domain.coupon.CouponRepository;
import com.coffee_shop.coffeeshop.domain.coupon.CouponTransactionHistory;
import com.coffee_shop.coffeeshop.domain.coupon.CouponTransactionHistoryRepository;
import com.coffee_shop.coffeeshop.domain.coupon.producer.CouponProducer;
import com.coffee_shop.coffeeshop.domain.user.User;
import com.coffee_shop.coffeeshop.domain.user.UserRepository;
import com.coffee_shop.coffeeshop.exception.ErrorCode;
import com.coffee_shop.coffeeshop.service.coupon.dto.request.CouponApplyServiceRequest;
import com.coffee_shop.coffeeshop.service.coupon.dto.response.IssuedCouponResponse;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class CouponApplyService {
	private final UserRepository userRepository;
	private final CouponRepository couponRepository;
	private final CouponProducer couponMessageQProducer;
	private final CouponTransactionHistoryRepository couponTransactionHistoryRepository;

	public IssuedCouponResponse isCouponIssued(Long userId, Long couponId) {
		User user = findUser(userId);
		Coupon coupon = findCoupon(couponId);
		Optional<CouponTransactionHistory> history = couponTransactionHistoryRepository.findByCouponAndUser(
			coupon, user);
		
		if (history.isPresent()) {
			return IssuedCouponResponse.createSuccessResponse(history.get());
		}

		return IssuedCouponResponse.createFailResponse();
	}

	@Transactional
	public void applyCoupon(CouponApplyServiceRequest request, LocalDateTime issueDateTime) {
		User user = findUser(request.getUserId());
		Coupon coupon = findCoupon(request.getCouponId());

		if (!coupon.canIssueCoupon()) {
			throw new BusinessException(ErrorCode.COUPON_LIMIT_REACHED);
		}

		checkDuplicateIssuedCoupon(coupon, user);

		couponMessageQProducer.applyCoupon(user, coupon, issueDateTime);
	}

	private void checkDuplicateIssuedCoupon(Coupon coupon, User user) {
		couponTransactionHistoryRepository.findByCouponAndUser(coupon, user)
			.ifPresent(couponTransactionHistory -> {
				throw new BusinessException(ErrorCode.COUPON_DUPLICATE_ISSUE,
					ErrorCode.COUPON_DUPLICATE_ISSUE.getMessage() + " 사용자 ID, 이름 : " + user.getId() + ", "
						+ user.getName());
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
