package com.coffee_shop.coffeeshop.domain.coupon.consumer;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.coffee_shop.coffeeshop.common.exception.BusinessException;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponIssueRepository;
import com.coffee_shop.coffeeshop.service.coupon.dto.request.CouponApplication;
import com.coffee_shop.coffeeshop.service.coupon.issue.RedisCouponIssueService;
import com.coffee_shop.coffeeshop.service.coupon.issue.fail.RedisCouponIssueFailHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class RedisCouponConsumer implements CouponConsumer {
	private static final long RANGE_COUNT = 10L;
	private final CouponIssueRepository couponIssueRepository;
	private final RedisCouponIssueFailHandler redisCouponIssueFailHandler;
	private final RedisCouponIssueService redisCouponIssueService;

	@Override
	@Scheduled(fixedRate = 1000)
	public void issueCoupon() {
		if (couponIssueRepository.isEmpty()) {
			return;
		}

		Set<ZSetOperations.TypedTuple<Object>> popped = couponIssueRepository.popMin(RANGE_COUNT);
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
			redisCouponIssueService.issueCoupon(couponApplication);
		} catch (BusinessException e) {
			log.info("쿠폰발급 실패 > {}", e.getMessage());
		} catch (Exception e) {
			redisCouponIssueFailHandler.handleFail(couponApplication, e);
		}
	}
}