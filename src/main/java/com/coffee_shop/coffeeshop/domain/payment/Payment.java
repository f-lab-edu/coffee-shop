package com.coffee_shop.coffeeshop.domain.payment;

import java.time.OffsetDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.coffee_shop.coffeeshop.common.domain.BaseTimeEntity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "payments")
public class Payment extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String paymentKey;
	private String orderId;
	private int amount;
	private OffsetDateTime requestedAt;
	private OffsetDateTime approvedAt;
	@Enumerated(EnumType.STRING)
	private PaymentStatus status;
}
