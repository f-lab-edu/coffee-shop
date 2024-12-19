package com.coffee_shop.coffeeshop.service.coupon.issue.fail;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.coffee_shop.coffeeshop.common.exception.BusinessException;
import com.coffee_shop.coffeeshop.domain.coupon.Coupon;
import com.coffee_shop.coffeeshop.domain.coupon.CouponIssueFailHistory;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponFailCountRepository;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponIssueFailHistoryRepository;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponIssueRepository;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponRepository;
import com.coffee_shop.coffeeshop.domain.user.User;
import com.coffee_shop.coffeeshop.domain.user.UserRepository;
import com.coffee_shop.coffeeshop.exception.ErrorCode;
import com.coffee_shop.coffeeshop.service.coupon.dto.request.CouponApplication;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class RedisCouponIssueFailHandler {
	private static final Logger log = LoggerFactory.getLogger(RedisCouponIssueFailHandler.class);
	private static final int MAX_FAIL_COUNT = 3;
	private final CouponIssueRepository couponIssueRepository;
	private final CouponFailCountRepository couponFailCountRepository;
	private final UserRepository userRepository;
	private final CouponRepository couponRepository;
	private final CouponIssueFailHistoryRepository couponIssueFailHistoryRepository;

	public void handleFail(CouponApplication couponApplication, Exception exception) {
		Long failCount = couponFailCountRepository.increment(couponApplication.getUserId());

		if (failCount >= MAX_FAIL_COUNT) {
			log.warn("최대 실패 횟수 {}회를 초과하였습니다. 실패 횟수 : {}", MAX_FAIL_COUNT, failCount, exception);

			Coupon coupon = findCoupon(couponApplication.getCouponId());
			User user = findUser(couponApplication.getUserId());

			CouponIssueFailHistory history = CouponIssueFailHistory.of(user, coupon,
				LocalDateTime.now());
			couponIssueFailHistoryRepository.save(history);
			return;
		}

		log.warn("쿠폰 발급 실패 > 재시도 횟수 : {}, 사용자 ID : {}, 쿠폰 ID : {}", failCount, couponApplication.getUserId(),
			couponApplication.getCouponId(), exception);
		couponIssueRepository.add(couponApplication, System.currentTimeMillis());
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
