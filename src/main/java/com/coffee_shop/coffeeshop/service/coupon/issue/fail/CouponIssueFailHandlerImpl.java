package com.coffee_shop.coffeeshop.service.coupon.issue.fail;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coffee_shop.coffeeshop.common.exception.BusinessException;
import com.coffee_shop.coffeeshop.domain.coupon.Coupon;
import com.coffee_shop.coffeeshop.domain.coupon.CouponIssueFailHistory;
import com.coffee_shop.coffeeshop.domain.coupon.MessageQ;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponIssueFailHistoryRepository;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponRepository;
import com.coffee_shop.coffeeshop.domain.user.User;
import com.coffee_shop.coffeeshop.domain.user.UserRepository;
import com.coffee_shop.coffeeshop.exception.ErrorCode;
import com.coffee_shop.coffeeshop.service.coupon.dto.request.CouponApplication;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class CouponIssueFailHandlerImpl {
	private static final int MAX_FAIL_COUNT = 3;
	private final MessageQ messageQ;
	private final UserRepository userRepository;
	private final CouponRepository couponRepository;
	private final CouponIssueFailHistoryRepository couponIssueFailHistoryRepository;

	@Transactional
	public void handleFail(CouponApplication couponApplication, Exception exception) {
		couponApplication.addException(exception);
		couponApplication.increaseFailCount();

		if (couponApplication.getFailCount() >= MAX_FAIL_COUNT) {
			handleTooManyFails(couponApplication);

			Coupon coupon = findCoupon(couponApplication.getCouponId());
			User user = findUser(couponApplication.getUserId());

			CouponIssueFailHistory history = CouponIssueFailHistory.of(user, coupon,
				LocalDateTime.now());
			couponIssueFailHistoryRepository.save(history);
			return;
		}

		messageQ.addFirst(couponApplication);
	}

	private void handleTooManyFails(CouponApplication couponApplication) {
		log.info("최대 실패 횟수 {}회를 초과하였습니다. 실패 횟수 : {}", MAX_FAIL_COUNT, couponApplication.getFailCount());
		log.info("---------------------- 예외 리스트 START ----------------------");
		List<Exception> exceptionList = couponApplication.getExceptionList();
		log.info("실패한 메시지 : {}", couponApplication);
		for (int i = 0; i < couponApplication.getExceptionList().size(); i++) {
			Exception e = exceptionList.get(i);
			e.printStackTrace();
			log.info("----------------------------------");
		}
		log.info("---------------------- 예외 리스트 END ----------------------");
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
