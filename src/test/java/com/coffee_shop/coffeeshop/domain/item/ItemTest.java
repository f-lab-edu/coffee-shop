package com.coffee_shop.coffeeshop.domain.item;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ItemTest {

	@DisplayName("상품을 등록 시 마지막 수정시간을 기록한다.")
	@Test
	void create() {
		//given
		LocalDateTime lastModifiedDateTime = LocalDateTime.of(2024, 9, 21, 0, 0);
		Item item = createItem("카페라떼", ItemType.COFFEE, 5000, null);

		//when
		Item savedItem = item.create(lastModifiedDateTime);

		//then
		assertThat(savedItem.getLastModifiedDateTime()).isEqualTo(lastModifiedDateTime);
	}

	@DisplayName("상품 정보를 수정한다.")
	@Test
	void update() {
		//given
		LocalDateTime lastModifiedDateTime = LocalDateTime.of(2024, 9, 21, 0, 0);
		Item item = createItem("카페라떼", ItemType.COFFEE, 5000, lastModifiedDateTime);

		LocalDateTime updatedModifiedDateTime = LocalDateTime.of(2024, 9, 22, 0, 0);
		Item updateItem = createItem("케이크", ItemType.DESSERT, 6000, null);

		//when
		Item updatedItem = item.update(updateItem, updatedModifiedDateTime);

		//then
		assertThat(updatedItem.getName()).isEqualTo("케이크");
		assertThat(updatedItem.getItemType()).isEqualTo(ItemType.DESSERT);
		assertThat(updatedItem.getPrice()).isEqualTo(6000);
		assertThat(updatedItem.getLastModifiedDateTime()).isEqualTo(updatedModifiedDateTime);
	}

	private Item createItem(String name, ItemType type, int price, LocalDateTime lastModifiedDateTime) {
		return Item.builder()
			.name(name)
			.itemType(type)
			.price(price)
			.lastModifiedDateTime(lastModifiedDateTime)
			.build();
	}
}
