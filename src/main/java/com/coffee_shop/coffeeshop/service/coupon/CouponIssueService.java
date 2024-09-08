package com.coffee_shop.coffeeshop.service.coupon;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coffee_shop.coffeeshop.common.exception.BusinessException;
import com.coffee_shop.coffeeshop.domain.coupon.Coupon;
import com.coffee_shop.coffeeshop.domain.coupon.CouponRepository;
import com.coffee_shop.coffeeshop.domain.coupon.CouponTransactionHistoryRepository;
import com.coffee_shop.coffeeshop.domain.coupon.producer.CouponProducer;
import com.coffee_shop.coffeeshop.domain.user.User;
import com.coffee_shop.coffeeshop.domain.user.UserRepository;
import com.coffee_shop.coffeeshop.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Transactional
@RequiredArgsConstructor
@Service
public class CouponIssueService {
	private final UserRepository userRepository;
	private final CouponRepository couponRepository;
	private final CouponProducer couponMessageQProducer;
	private final CouponTransactionHistoryRepository couponTransactionHistoryRepository;

	public void applyCoupon(Long userId, Long couponId, LocalDateTime issueDateTime) {
		User user = findUser(userId);
		Coupon coupon = findCoupon(couponId);

		//발급할 수 있는 쿠폰개수를 확인한다.
		if (!coupon.canIssueCoupon()) {
			throw new BusinessException(ErrorCode.COUPON_LIMIT_REACHED);
		}

		//이미 발급된 쿠폰인지 확인
		checkDuplicateIssuedCoupon(coupon, user);

		//발급 신청 -> 큐에 넣기
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
