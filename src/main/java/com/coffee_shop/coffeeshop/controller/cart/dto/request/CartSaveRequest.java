package com.coffee_shop.coffeeshop.controller.cart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import com.coffee_shop.coffeeshop.service.cart.dto.request.CartSaveServiceRequest;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CartSaveRequest {

	@NotNull(message = "상품 아이디는 필수입니다.")
	@Positive(message = "상품 아이디는 양수입니다.")
	private Long itemId;

	@Min(value = 1, message = "최소 주문 상품 수는 1개 이상이어야 합니다.")
	private int count;

	@Builder
	private CartSaveRequest(Long itemId, int count) {
		this.itemId = itemId;
		this.count = count;
	}

	public CartSaveServiceRequest toServiceRequest() {
		return CartSaveServiceRequest.builder()
			.itemId(itemId)
			.count(count)
			.build();
	}
}
