package com.coffee_shop.coffeeshop.domain.coupon.producer;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.springframework.stereotype.Component;

import com.coffee_shop.coffeeshop.domain.coupon.Coupon;
import com.coffee_shop.coffeeshop.domain.coupon.MessageQ;
import com.coffee_shop.coffeeshop.domain.user.User;
import com.coffee_shop.coffeeshop.service.coupon.dto.request.CouponApplication;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class CouponMessageQProducer implements CouponProducer {
	private static final int POSITION_NOT_FOUND = -1;

	private final MessageQ messageQ;

	@Override
	public void applyCoupon(User user, Coupon coupon, LocalDateTime issueDateTime) {
		CouponApplication couponApplication = CouponApplication.createCouponApplication(user, coupon, issueDateTime);
		try {
			messageQ.addMessage(couponApplication);
		} catch (NullPointerException e) {
			log.warn("Unable to insert null into coupon issuance queue, CouponApplication : {}", couponApplication);
		} catch (Exception e) {
			log.warn("Unable to insert into coupon issuance queue, CouponApplication : {}", couponApplication);
		}
	}

	@Override
	public int getPosition(Long userId, Long couponId) {
		ArrayList<CouponApplication> couponApplications = messageQ.toArrayList();
		int qSize = couponApplications.size();

		for (int i = 0; i < qSize; i++) {
			CouponApplication application = couponApplications.get(i);
			if (isMatchingApplication(userId, couponId, application)) {
				return i + 1;
			}
		}

		return POSITION_NOT_FOUND;
	}

	@Override
	public boolean isPositionNotFound(int position) {
		return position == POSITION_NOT_FOUND;
	}

	private boolean isMatchingApplication(Long userId, Long couponId, CouponApplication application) {
		return application.getUserId().equals(userId)
			&& application.getCouponId().equals(couponId);
	}
}
