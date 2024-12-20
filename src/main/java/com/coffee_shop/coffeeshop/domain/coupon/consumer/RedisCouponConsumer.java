package com.coffee_shop.coffeeshop.domain.coupon.consumer;

import java.util.Optional;
import java.util.Set;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.coffee_shop.coffeeshop.common.exception.BusinessException;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponIssuanceRateRepository;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponIssueRepository;
import com.coffee_shop.coffeeshop.service.coupon.dto.request.CouponApplication;
import com.coffee_shop.coffeeshop.service.coupon.issue.CouponIssuanceRateService;
import com.coffee_shop.coffeeshop.service.coupon.issue.RedisCouponIssueFacadeService;
import com.coffee_shop.coffeeshop.service.coupon.issue.fail.RedisCouponIssueFailHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(name = "schedule.active", havingValue = "redis")
public class RedisCouponConsumer {
	private final CouponIssuanceRateRepository couponIssuanceRateRepository;
	private final CouponIssueRepository couponIssueRepository;
	private final RedisCouponIssueFailHandler redisCouponIssueFailHandler;
	private final RedisCouponIssueFacadeService redisCouponIssueFacadeService;
	private final CouponIssuanceRateService couponIssuanceRateService;

	@Scheduled(fixedRate = 1000)
	public void issueCoupon() {
		if (couponIssueRepository.isEmpty()) {
			return;
		}

		long rangeCount = couponIssuanceRateService.getIssuanceRate();
		Set<ZSetOperations.TypedTuple<Object>> popped = couponIssueRepository.popMin(rangeCount);
		for (ZSetOperations.TypedTuple<Object> typedTuple : popped) {
			Object object = typedTuple.getValue();
			if (!(object instanceof CouponApplication)) {
				log.warn("couponApplication object is not a CouponApplication, Unexpected object type: {}",
					Optional.ofNullable(object).getClass().getName());
				continue;
			}

			issueCoupon((CouponApplication)object);
		}
	}

	private void issueCoupon(CouponApplication couponApplication) {
		try {
			redisCouponIssueFacadeService.issueCoupon(couponApplication);
		} catch (BusinessException e) {
			log.info("쿠폰발급 실패 > {}", e.getMessage());
		} catch (Exception e) {
			redisCouponIssueFailHandler.handleFail(couponApplication, e);
		}
	}
}
