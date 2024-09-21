package com.coffee_shop.coffeeshop.service.item.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.coffee_shop.coffeeshop.domain.item.Item;
import com.coffee_shop.coffeeshop.domain.item.ItemType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.querydsl.core.annotations.QueryProjection;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ItemResponse {

	private Long id;

	private String name;

	private ItemType itemType;

	private int price;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
	private LocalDateTime lastModifiedDateTime;

	@Builder
	@QueryProjection
	public ItemResponse(Long id, String name, ItemType itemType, int price, LocalDateTime lastModifiedDateTime) {
		this.id = id;
		this.name = name;
		this.itemType = itemType;
		this.price = price;
		this.lastModifiedDateTime = lastModifiedDateTime;
	}

	public static ItemResponse of(Item item) {
		return ItemResponse.builder()
			.id(item.getId())
			.name(item.getName())
			.itemType(item.getItemType())
			.price(item.getPrice())
			.lastModifiedDateTime(item.getLastModifiedDateTime())
			.build();
	}

	public static List<ItemResponse> listOf(List<Item> items) {
		return items.stream()
			.map(ItemResponse::of)
			.collect(Collectors.toList());
	}
}
