package com.coffee_shop.coffeeshop.domain.cash;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class Cash {
	private static final int SCALE = 2;

	@Column(nullable = false)
	private BigDecimal amount;

	public Cash(BigDecimal amount) {
		this.amount = amount;
	}

	public static Cash of(final int amount) {
		return new Cash(new BigDecimal(amount));
	}

	public static Cash of(final String amount) {
		return new Cash(new BigDecimal(amount));
	}

	public Cash divide(final int divisor) {
		return new Cash(amount.divide(BigDecimal.valueOf(divisor), SCALE, RoundingMode.HALF_UP));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Cash cash = (Cash)o;
		return amount.compareTo(cash.amount) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(amount);
	}
}
