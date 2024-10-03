package com.coffee_shop.coffeeshop.domain.cart;

import static com.coffee_shop.coffeeshop.domain.item.ItemType.*;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.coffee_shop.coffeeshop.domain.item.Item;
import com.coffee_shop.coffeeshop.domain.user.User;

class CartsTest {
	@DisplayName("장바구니 수량이 20개가 넘는지 확인한다.")
	@Test
	void isOverMaxOrderCount() {
		//given
		Item item = createItem();
		User user = createUser();
		Cart cart = createCart(user, item, 20);

		Carts carts = new Carts(List.of(cart));

		//when
		boolean result = carts.isOverMaxOrderCount(1);

		//then
		assertThat(result).isTrue();
	}

	private Cart createCart(User user, Item item, int count) {
		return Cart.builder()
			.user(user)
			.item(item)
			.itemCount(count)
			.build();
	}

	private Item createItem() {
		return Item.builder()
			.name("카페라떼")
			.itemType(COFFEE)
			.price(5000)
			.lastModifiedDateTime(LocalDateTime.of(2024, 9, 21, 0, 0))
			.build();
	}

	private User createUser() {
		return User.builder()
			.name("우경서")
			.build();
	}
}
