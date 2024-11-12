package com.coffee_shop.coffeeshop.domain.coupon.consumer;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.coffee_shop.coffeeshop.common.exception.BusinessException;
import com.coffee_shop.coffeeshop.domain.coupon.MessageQ;
import com.coffee_shop.coffeeshop.service.coupon.dto.request.CouponApplication;
import com.coffee_shop.coffeeshop.service.coupon.issue.CouponIssueFailHandler;
import com.coffee_shop.coffeeshop.service.coupon.issue.CouponIssueService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class CouponMessageQConsumer implements CouponConsumer {
	private final MessageQ messageQ;
	private final CouponIssueFailHandler couponIssueFailHandler;
	private final CouponIssueService couponIssueService;

	@Override
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
