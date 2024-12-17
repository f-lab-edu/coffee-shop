package com.coffee_shop.coffeeshop.domain.coupon.consumer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.coffee_shop.coffeeshop.common.exception.BusinessException;
import com.coffee_shop.coffeeshop.domain.coupon.MessageQ;
import com.coffee_shop.coffeeshop.service.coupon.dto.request.CouponApplication;
import com.coffee_shop.coffeeshop.service.coupon.issue.CouponIssueServiceImpl;
import com.coffee_shop.coffeeshop.service.coupon.issue.fail.CouponIssueFailHandlerImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(name = "schedule.active", havingValue = "messageQ")
public class CouponMessageQConsumer {
	private final MessageQ messageQ;
	private final CouponIssueFailHandlerImpl couponIssueFailHandler;
	private final CouponIssueServiceImpl couponIssueService;

	@Scheduled(cron = "0/1 * * * * *")
	public synchronized void issueCoupon() {
		while (!messageQ.isEmpty()) {
			CouponApplication couponApplication = messageQ.takeMessage();
			try {
				couponIssueService.issueCoupon(couponApplication);
			} catch (BusinessException e) {
				log.info("쿠폰발급 실패 > {}", e.getMessage());
			} catch (Exception e) {
				couponIssueFailHandler.handleFail(couponApplication, e);
			}
		}
	}
}
