package com.coffee_shop.coffeeshop.domain.coupon;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.coffee_shop.coffeeshop.common.domain.BaseTimeEntity;
import com.coffee_shop.coffeeshop.domain.cash.Cash;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "coupons")
public class Coupon extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 40)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private CouponType type;

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "amount", column = @Column(name = "discount_amount", nullable = false)),
	})
	@Column(nullable = false)
	private Cash discountAmount;

	private int minOrderAmount;

	private int maxIssueCount;

	@Builder
	private Coupon(String name, CouponType type, int discountAmount, int minOrderAmount,
		int maxIssueCount) {
		this.name = name;
		this.type = type;
		this.discountAmount = convertDiscountAmount(type, discountAmount);
		this.minOrderAmount = minOrderAmount;
		this.maxIssueCount = maxIssueCount;
	}

	private Cash convertDiscountAmount(CouponType type, int discountAmount) {
		if (type == CouponType.PERCENTAGE) {
			return Cash.of(discountAmount).divide(100);
		}

		return Cash.of(discountAmount);
	}
}
