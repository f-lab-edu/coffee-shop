package com.coffee_shop.coffeeshop.domain.cart;

import static jakarta.persistence.FetchType.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.coffee_shop.coffeeshop.common.domain.BaseTimeEntity;
import com.coffee_shop.coffeeshop.common.exception.BusinessException;
import com.coffee_shop.coffeeshop.domain.item.Item;
import com.coffee_shop.coffeeshop.domain.user.User;
import com.coffee_shop.coffeeshop.exception.ErrorCode;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@IdClass(CartId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "carts")
public class Cart extends BaseTimeEntity {
	private static final int MAX_ORDER_COUNT = 20;
	@Id
	@ManyToOne(fetch = LAZY, optional = false)
	@JoinColumn(name = "user_id")
	private User user;

	@Id
	@ManyToOne(fetch = LAZY, optional = false)
	@JoinColumn(name = "item_id")
	private Item item;

	@Column(nullable = false)
	private int itemCount;

	@Builder
	private Cart(User user, Item item, int itemCount) {
		this.user = user;
		this.item = item;
		this.itemCount = itemCount;
	}

	public static Cart createCart(User user, Item item, int count) {
		return Cart.builder()
			.user(user)
			.item(item)
			.itemCount(count)
			.build();
	}

	public void addCount(int count) {
		int totalCount = this.itemCount + count;

		if (totalCount > MAX_ORDER_COUNT) {
			throw new BusinessException(ErrorCode.OVER_MAX_ORDER_COUNT);
		}

		this.itemCount = totalCount;
	}
}
