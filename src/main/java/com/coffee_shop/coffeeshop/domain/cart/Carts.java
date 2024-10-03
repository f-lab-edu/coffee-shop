package com.coffee_shop.coffeeshop.domain.cart;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class Carts {

	private static final int MAX_ORDER_COUNT = 20;

	private List<Cart> cartItems;

	public boolean isOverMaxOrderCount(int count) {
		int totalCount = calculateTotalCount();

		if (totalCount + count > MAX_ORDER_COUNT) {
			return true;
		}

		return false;
	}

	private int calculateTotalCount() {
		return cartItems.stream()
			.mapToInt(Cart::getItemCount)
			.sum();
	}

	public boolean containsItem(Long itemId) {
		return cartItems.stream()
			.anyMatch(cart -> cart.getItem().getId().equals(itemId));
	}
}
