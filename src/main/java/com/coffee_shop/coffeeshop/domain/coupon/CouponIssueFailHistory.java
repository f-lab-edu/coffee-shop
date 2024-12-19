package com.coffee_shop.coffeeshop.domain.coupon;

import static jakarta.persistence.FetchType.*;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.coffee_shop.coffeeshop.common.domain.BaseTimeEntity;
import com.coffee_shop.coffeeshop.domain.user.User;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "coupon_issue_fail_histories")
public class CouponIssueFailHistory extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = LAZY, optional = false)
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne(fetch = LAZY, optional = false)
	@JoinColumn(name = "coupon_id")
	private Coupon coupon;

	@Column(nullable = false)
	private LocalDateTime issueFailDateTime;

	@Builder
	private CouponIssueFailHistory(User user, Coupon coupon, LocalDateTime issueFailDateTime) {
		this.user = user;
		this.coupon = coupon;
		this.issueFailDateTime = issueFailDateTime;
	}

	public static CouponIssueFailHistory of(User user, Coupon coupon, LocalDateTime issueFailDateTime) {
		return CouponIssueFailHistory.builder()
			.user(user)
			.coupon(coupon)
			.issueFailDateTime(issueFailDateTime)
			.build();
	}
}
