package com.coffee_shop.coffeeshop.service.coupon;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.coffee_shop.coffeeshop.common.exception.BusinessException;
import com.coffee_shop.coffeeshop.domain.coupon.Coupon;
import com.coffee_shop.coffeeshop.domain.coupon.CouponRepository;
import com.coffee_shop.coffeeshop.domain.coupon.CouponTransactionHistory;
import com.coffee_shop.coffeeshop.domain.coupon.CouponTransactionHistoryRepository;
import com.coffee_shop.coffeeshop.domain.user.User;
import com.coffee_shop.coffeeshop.domain.user.UserRepository;
import com.coffee_shop.coffeeshop.exception.ErrorCode;
import com.coffee_shop.coffeeshop.service.coupon.dto.request.CouponApplication;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class CouponIssueService {
	private final UserRepository userRepository;
	private final CouponRepository couponRepository;
	private final CouponTransactionHistoryRepository couponTransactionHistoryRepository;

	@Transactional
	public void issueCoupon(CouponApplication couponApplication) {
		Coupon coupon = findCoupon(couponApplication.getCouponId());
		User user = findUser(couponApplication.getUserId());

		//발급할 수 있는 쿠폰개수를 확인한다.
		if (!coupon.canIssueCoupon()) {
			throw new BusinessException(ErrorCode.COUPON_LIMIT_REACHED);
		}

		//이미 발급된 쿠폰인지 확인
		checkDuplicateIssuedCoupon(coupon, user);

		//발급 count +1
		coupon.issueCoupon();

		//이력 저장
		couponTransactionHistoryRepository.save(
			CouponTransactionHistory.issueCoupon(user, coupon, couponApplication.getIssueDateTime()));
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

	private void checkDuplicateIssuedCoupon(Coupon coupon, User user) {
		couponTransactionHistoryRepository.findByCouponAndUser(coupon, user)
			.ifPresent(couponTransactionHistory -> {
				throw new BusinessException(ErrorCode.COUPON_DUPLICATE_ISSUE,
					ErrorCode.COUPON_DUPLICATE_ISSUE.getMessage() + " 사용자 ID, 이름 : " + user.getId() + ", "
						+ user.getName());
			});
	}
}
