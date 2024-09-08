package com.coffee_shop.coffeeshop.service.coupon.dto.request;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CouponApplication {
	private final Long userId;

	private final Long couponId;

	private final LocalDateTime issueDateTime;

	private int failCount;

	private List<Exception> exceptionList = new ArrayList<>();

	public void addException(Exception e) {
		exceptionList.add(e);
	}

	public void increaseFailCount() {
		failCount += 1;
	}

}
