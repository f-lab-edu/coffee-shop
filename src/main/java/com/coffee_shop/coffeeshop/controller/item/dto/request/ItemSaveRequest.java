package com.coffee_shop.coffeeshop.controller.item.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.coffee_shop.coffeeshop.domain.item.ItemType;
import com.coffee_shop.coffeeshop.service.item.dto.request.ItemSaveServiceRequest;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemSaveRequest {

	@NotBlank(message = "상품 이름은 필수입니다.")
	@Size(max = 100, message = "상품 이름은 최대 100자까지 입력가능합니다.")
	private String name;

	@NotNull(message = "유효하지 않는 상품 타입입니다.")
	private ItemType itemType;

	@Min(value = 100, message = "최소 상품 가격은 100원입니다.")
	private int price;

	@Builder
	private ItemSaveRequest(String name, ItemType itemType, int price) {
		this.name = name;
		this.itemType = itemType;
		this.price = price;
	}

	public ItemSaveServiceRequest toServiceRequest() {
		return ItemSaveServiceRequest.builder()
			.name(name)
			.itemType(itemType)
			.price(price)
			.build();

	}
}
