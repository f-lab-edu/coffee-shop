package com.coffee_shop.coffeeshop.domain.cart;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class CartId implements Serializable {
	private Long user;
	private Long item;
}
