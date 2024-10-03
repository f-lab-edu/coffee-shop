package com.coffee_shop.coffeeshop.service.item.dto.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemSearchServiceRequest {

	private String name;

	private String itemType;

	@Builder
	private ItemSearchServiceRequest(String name, String itemType) {
		this.name = name;
		this.itemType = itemType;
	}
}
