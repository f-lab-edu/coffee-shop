package com.coffee_shop.coffeeshop.service.coupon.issue;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
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
import com.coffee_shop.coffeeshop.service.coupon.CouponService;
import com.coffee_shop.coffeeshop.service.coupon.dto.request.CouponApplication;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class RedisCouponIssueService {
	private static final int SYNC_COUNT = 10;
	private final CouponService couponService;
	private final UserRepository userRepository;
	private final CouponRepository couponRepository;
	private final CouponTransactionHistoryRepository couponTransactionHistoryRepository;
	private final CouponIssueCountRepository couponIssueCountRepository;

	@Transactional
	public void issueCoupon(CouponApplication couponApplication) {
		Coupon coupon = findCoupon(couponApplication.getCouponId());
		User user = findUser(couponApplication.getUserId());

		Long issueCount = couponIssueCountRepository.getIssueCount(couponApplication.getCouponId());
		isCouponLimitExceeded(coupon, issueCount);

		couponTransactionHistoryRepository.save(
			CouponTransactionHistory.issueCoupon(user, coupon, LocalDateTime.now()));

		Long increment = couponIssueCountRepository.increment(coupon.getId());
		if (isDbSyncRequired(increment, coupon)) {
			try {
				couponService.syncCouponIssuedCount(coupon.getId(), increment);
			} catch (BusinessException e) {
				log.warn("Failed to synchronize coupon issuance count with the database. {}", e.getMessage());
			} catch (Exception e) {
				log.error("Failed to synchronize coupon issuance count with the database.", e);
			}
		}
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

	private static boolean isDbSyncRequired(Long increment, Coupon coupon) {
		if (increment == null || increment <= 0L) {
			log.warn("Coupon could not be updated because issuedCount is null or zero");
			return false;
		}

		return increment % SYNC_COUNT == 0 || increment == coupon.getMaxIssueCount();
	}
}
