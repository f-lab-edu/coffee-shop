package com.coffee_shop.coffeeshop.service.coupon.issue.fail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponFailCountRepository;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponIssueRepository;
import com.coffee_shop.coffeeshop.service.coupon.dto.request.CouponApplication;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class RedisCouponIssueFailHandler {
	private static final Logger log = LoggerFactory.getLogger(RedisCouponIssueFailHandler.class);
	private static final int MAX_FAIL_COUNT = 3;
	private final CouponIssueFailHistoryService couponIssueFailHistoryService;
	private final CouponIssueRepository couponIssueRepository;
	private final CouponFailCountRepository couponFailCountRepository;

	public void handleFail(CouponApplication couponApplication, Exception exception) {
		Long failCount = couponFailCountRepository.increment(couponApplication.getUserId());

		if (MAX_FAIL_COUNT > failCount) {
			log.warn("쿠폰 발급 실패 > 재시도 횟수 : {}, 사용자 ID : {}, 쿠폰 ID : {}", failCount, couponApplication.getUserId(),
				couponApplication.getCouponId(), exception);
			couponIssueRepository.add(couponApplication, System.currentTimeMillis());
			return;
		}

		log.warn("최대 실패 횟수 {}회를 초과하였습니다. 실패 횟수 : {}", MAX_FAIL_COUNT, failCount, exception);
		couponIssueFailHistoryService.saveCouponFailHistory(couponApplication);
	}
}
