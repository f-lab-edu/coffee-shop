package com.coffee_shop.coffeeshop.service.coupon;

import java.util.List;

import org.springframework.stereotype.Component;

import com.coffee_shop.coffeeshop.domain.coupon.MessageQ;
import com.coffee_shop.coffeeshop.service.coupon.dto.request.CouponApplication;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class CouponIssueFailHandler {
	private static final int MAX_RETRY_COUNT = 3;
	private final MessageQ messageQ;

	public void handleFail(CouponApplication couponApplication, Exception e) {
		couponApplication.addException(e);
		couponApplication.increaseFailCount();
		if (couponApplication.getFailCount() >= MAX_RETRY_COUNT) {
			handleTooManyFails(couponApplication);
		} else {
			messageQ.addMessage(couponApplication);
		}
	}

	private void handleTooManyFails(CouponApplication couponApplication) {
		log.info("최대 실패 횟수 (" + MAX_RETRY_COUNT + ")를 초과하였습니다. 실패 횟수 : " + couponApplication.getFailCount());
		log.info("---------------------- 예외 리스트 START ----------------------");
		List<Exception> exceptionList = couponApplication.getExceptionList();
		log.info("실패한 메시지 : " + couponApplication);
		for (int i = 0; i < couponApplication.getExceptionList().size(); i++) {
			Exception e = exceptionList.get(i);
			e.printStackTrace();
			log.info("----------------------------------");
		}
		log.info("---------------------- 예외 리스트 END ----------------------");
	}
}
