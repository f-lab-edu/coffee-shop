package com.coffee_shop.coffeeshop.domain.item;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Getter;

@Getter
public enum ItemType {
	DESSERT("DESSERT", "디저트"),
	COFFEE("COFFEE", "커피"),
	NON_COFFEE("NON_COFFEE", "기타");

	private final String code;
	private final String name;

	ItemType(String code, String name) {
		this.code = code;
		this.name = name;
	}

	@JsonCreator
	public static ItemType of(String itemType) {
		return Arrays.stream(ItemType.values())
			.filter(type -> type.code.equals(itemType))
			.findFirst()
			.orElse(null);
	}
}
