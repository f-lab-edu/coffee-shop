package com.coffee_shop.coffeeshop.service.item.dto.request;

import com.coffee_shop.coffeeshop.domain.item.ItemType;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemUpdateServiceRequest {

	private String name;

	private ItemType itemType;

	private int price;

	@Builder
	private ItemUpdateServiceRequest(String name, ItemType itemType, int price) {
		this.name = name;
		this.itemType = itemType;
		this.price = price;
	}
}
