package com.coffee_shop.coffeeshop.service.cart.dto.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CartDeleteServiceRequest {

	private Long itemId;

	@Builder
	private CartDeleteServiceRequest(Long itemId) {
		this.itemId = itemId;
	}
}
