package com.coffee_shop.coffeeshop.domain.coupon.producer;

import java.util.ArrayList;

import org.springframework.stereotype.Component;

import com.coffee_shop.coffeeshop.common.exception.BusinessException;
import com.coffee_shop.coffeeshop.domain.coupon.Coupon;
import com.coffee_shop.coffeeshop.domain.coupon.MessageQ;
import com.coffee_shop.coffeeshop.domain.user.User;
import com.coffee_shop.coffeeshop.exception.ErrorCode;
import com.coffee_shop.coffeeshop.service.coupon.dto.request.CouponApplication;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class CouponMessageQProducer implements CouponProducer {
	private final MessageQ messageQ;

	@Override
	public void applyCoupon(User user, Coupon coupon) {
		CouponApplication couponApplication = CouponApplication.of(user, coupon);
		try {
			messageQ.addMessage(couponApplication);
		} catch (NullPointerException e) {
			log.warn("Unable to insert null into coupon issuance queue, CouponApplication : {}", couponApplication);
		} catch (Exception e) {
			log.warn("Unable to insert into coupon issuance queue, CouponApplication : {}", couponApplication);
		}
	}

	@Override
	public int getPosition(User user, Coupon coupon) {
		ArrayList<CouponApplication> couponApplications = messageQ.toArrayList();
		int qSize = couponApplications.size();

		for (int i = 0; i < qSize; i++) {
			CouponApplication application = couponApplications.get(i);
			if (isMatchingApplication(user.getId(), coupon.getId(), application)) {
				return i + 1;
			}
		}

		throw new BusinessException(ErrorCode.POSITION_NOT_FOUND);
	}

	private boolean isMatchingApplication(Long userId, Long couponId, CouponApplication application) {
		return application.getUserId().equals(userId)
			&& application.getCouponId().equals(couponId);
	}
}
