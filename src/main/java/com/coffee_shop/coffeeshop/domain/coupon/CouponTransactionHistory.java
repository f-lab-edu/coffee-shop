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
import com.coffee_shop.coffeeshop.domain.order.Order;
import com.coffee_shop.coffeeshop.domain.user.User;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "coupon_transaction_histories")
public class CouponTransactionHistory extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = LAZY, optional = false)
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "order_id")
	private Order order;

	@ManyToOne(fetch = LAZY, optional = false)
	@JoinColumn(name = "coupon_id")
	private Coupon coupon;

	@Column(nullable = false)
	private LocalDateTime issueDateTime;

	@Builder
	private CouponTransactionHistory(User user, Order order, Coupon coupon, LocalDateTime issueDateTime) {
		this.user = user;
		this.order = order;
		this.coupon = coupon;
		this.issueDateTime = issueDateTime;
	}

	public static CouponTransactionHistory issueCoupon(User user, Coupon coupon, LocalDateTime issueDateTime) {
		return CouponTransactionHistory.builder()
			.user(user)
			.coupon(coupon)
			.issueDateTime(issueDateTime)
			.build();
	}
}
