package com.coffee_shop.coffeeshop.domain.cart;

import static com.coffee_shop.coffeeshop.domain.item.ItemType.*;

import java.time.LocalDateTime;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.coffee_shop.coffeeshop.common.exception.BusinessException;
import com.coffee_shop.coffeeshop.domain.item.Item;
import com.coffee_shop.coffeeshop.domain.user.User;

class CartTest {

	@DisplayName("장바구니에 담긴 수량이 20개 이상인지 확인한다.")
	@Test
	void addCount() {
		//given
		Item item = createItem();
		User user = createUser();
		Cart cart = Cart.createCart(user, item, 20);

		//when, then
		Assertions.assertThatThrownBy(() -> cart.addCount(1))
			.isInstanceOf(BusinessException.class)
			.hasMessage("최대 주문 가능 수량은 20개 입니다.");
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
