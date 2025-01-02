package com.coffee_shop.coffeeshop.service.coupon.issue.fail;

import java.util.List;

import org.springframework.stereotype.Service;

import com.coffee_shop.coffeeshop.domain.coupon.MessageQ;
import com.coffee_shop.coffeeshop.service.coupon.dto.request.CouponApplication;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class CouponIssueFailHandlerImpl {
	private static final int MAX_FAIL_COUNT = 3;
	private final MessageQ messageQ;
	private final CouponIssueFailHistoryService couponIssueFailHistoryService;

	public void handleFail(CouponApplication couponApplication, Exception exception) {
		couponApplication.addException(exception);
		couponApplication.increaseFailCount();

		if (MAX_FAIL_COUNT > couponApplication.getFailCount()) {
			messageQ.addFirst(couponApplication);
			return;
		}

		handleTooManyFails(couponApplication);
		couponIssueFailHistoryService.saveCouponFailHistory(couponApplication);
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
}
