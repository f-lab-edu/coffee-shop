package com.coffee_shop.coffeeshop.service.coupon.dto.request;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.coffee_shop.coffeeshop.domain.coupon.Coupon;
import com.coffee_shop.coffeeshop.domain.user.User;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CouponApplication {
	private Long userId;

	private Long couponId;

	private LocalDateTime issueDateTime;

	private int failCount;

	private List<Exception> exceptionList = new ArrayList<>();

	@Builder
	private CouponApplication(Long userId, Long couponId, LocalDateTime issueDateTime, int failCount) {
		this.userId = userId;
		this.couponId = couponId;
		this.issueDateTime = issueDateTime;
		this.failCount = failCount;
	}

	public static CouponApplication createCouponApplication(User user, Coupon coupon, LocalDateTime issueDateTime) {
		return CouponApplication.builder()
			.userId(user.getId())
			.couponId(coupon.getId())
			.issueDateTime(issueDateTime)
			.build();
	}

	public void addException(Exception e) {
		exceptionList.add(e);
	}

	public void increaseFailCount() {
		failCount += 1;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		CouponApplication that = (CouponApplication)o;
		return getFailCount() == that.getFailCount() && Objects.equals(getUserId(), that.getUserId())
			&& Objects.equals(getCouponId(), that.getCouponId()) && Objects.equals(getIssueDateTime(),
			that.getIssueDateTime());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getUserId(), getCouponId(), getIssueDateTime(), getFailCount());
	}

	@Override
	public String toString() {
		return "CouponApplication{" +
			"userId=" + userId +
			", couponId=" + couponId +
			", issueDateTime=" + issueDateTime +
			", failCount=" + failCount +
			", exceptionList=" + exceptionList +
			'}';
	}
}
