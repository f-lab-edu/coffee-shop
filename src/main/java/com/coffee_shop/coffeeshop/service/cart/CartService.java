package com.coffee_shop.coffeeshop.service.cart;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coffee_shop.coffeeshop.common.exception.BusinessException;
import com.coffee_shop.coffeeshop.domain.cart.Cart;
import com.coffee_shop.coffeeshop.domain.cart.CartRepository;
import com.coffee_shop.coffeeshop.domain.cart.Carts;
import com.coffee_shop.coffeeshop.domain.item.Item;
import com.coffee_shop.coffeeshop.domain.item.ItemRepository;
import com.coffee_shop.coffeeshop.domain.user.User;
import com.coffee_shop.coffeeshop.domain.user.UserRepository;
import com.coffee_shop.coffeeshop.exception.ErrorCode;
import com.coffee_shop.coffeeshop.service.cart.dto.request.CartDeleteServiceRequest;
import com.coffee_shop.coffeeshop.service.cart.dto.request.CartSaveServiceRequest;
import com.coffee_shop.coffeeshop.service.cart.dto.response.CartResponse;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class CartService {
	private final UserRepository userRepository;
	private final ItemRepository itemRepository;
	private final CartRepository cartRepository;

	@Transactional
	public CartResponse updateCartItem(Long userId, CartSaveServiceRequest request) {
		Item item = findItem(request.getItemId());
		User user = findUser(userId);

		List<Cart> cartItems = cartRepository.findByUserIdFetchJoin(user.getId());
		Carts carts = new Carts(cartItems);

		if (carts.isOverMaxOrderCount(request.getCount())) {
			throw new BusinessException(ErrorCode.OVER_MAX_ORDER_COUNT);
		}

		if (carts.containsItem(item.getId())) {
			Cart cart = cartRepository.findByUserAndItem(user, item);
			cart.addCount(request.getCount());
			return CartResponse.of(cart);
		}

		Cart savedCart = cartRepository.save(Cart.createCart(user, item, request.getCount()));
		return CartResponse.of(savedCart);
	}

	@Transactional
	public void deleteCartItem(Long userId, CartDeleteServiceRequest request) {
		cartRepository.deleteByUserIdAndItemId(userId, request.getItemId());
	}

	public List<CartResponse> findCartItems(Long userId) {
		List<Cart> cartItems = cartRepository.findByUserIdFetchJoin(userId);
		return CartResponse.listOf(cartItems);
	}

	private User findUser(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
	}

	private Item findItem(Long itemId) {
		return itemRepository.findById(itemId)
			.orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
	}

}
