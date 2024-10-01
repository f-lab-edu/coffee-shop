package com.coffee_shop.coffeeshop.service.cart;

import java.util.List;
import java.util.Optional;

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

		List<Cart> cartItems = cartRepository.findByUser(user);
		Carts carts = new Carts(cartItems);

		if (carts.isOverMaxOrderCount(request.getCount())) {
			throw new BusinessException(ErrorCode.OVER_MAX_ORDER_COUNT);
		}

		Optional<Cart> optionalCart = cartRepository.findByUserAndItem(user, item);
		if (optionalCart.isPresent()) {
			Cart cart = optionalCart.get();
			cart.addCount(request.getCount());
			return CartResponse.of(cart);
		}

		Cart savedCart = cartRepository.save(Cart.createCart(user, item, request.getCount()));
		return CartResponse.of(savedCart);
	}

	@Transactional
	public void deleteCartItem(Long userId, CartDeleteServiceRequest request) {
		User user = findUser(userId);
		Item item = findItem(request.getItemId());
		cartRepository.deleteByUserAndItem(user, item);
	}

	public List<CartResponse> findCartItems(Long userId) {
		User user = findUser(userId);
		List<Cart> cartItems = cartRepository.findByUserFetchJoin(user);
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
