package com.coffee_shop.coffeeshop.controller.item.dto.request;

import com.coffee_shop.coffeeshop.service.item.dto.request.ItemSearchServiceRequest;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemSearchRequest {

	private String name;

	private String itemType;

	@Builder
	private ItemSearchRequest(String name, String itemType) {
		this.name = name;
		this.itemType = itemType;
	}

	public ItemSearchServiceRequest toServiceRequest() {
		return ItemSearchServiceRequest.builder()
			.name(name)
			.itemType(itemType)
			.build();
	}
}
