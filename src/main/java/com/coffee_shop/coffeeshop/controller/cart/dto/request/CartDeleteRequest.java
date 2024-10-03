package com.coffee_shop.coffeeshop.controller.cart.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import com.coffee_shop.coffeeshop.service.cart.dto.request.CartDeleteServiceRequest;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CartDeleteRequest {

	@NotNull(message = "상품 아이디는 필수입니다.")
	@Positive(message = "상품 아이디는 양수입니다.")
	private Long itemId;

	@Builder
	private CartDeleteRequest(Long itemId) {
		this.itemId = itemId;
	}

	public CartDeleteServiceRequest toServiceRequest() {
		return CartDeleteServiceRequest.builder()
			.itemId(itemId)
			.build();
	}
}
