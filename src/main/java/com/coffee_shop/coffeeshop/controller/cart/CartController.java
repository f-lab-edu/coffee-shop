package com.coffee_shop.coffeeshop.controller.cart;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coffee_shop.coffeeshop.common.dto.response.ApiResponse;
import com.coffee_shop.coffeeshop.controller.cart.dto.request.CartDeleteRequest;
import com.coffee_shop.coffeeshop.controller.cart.dto.request.CartSaveRequest;
import com.coffee_shop.coffeeshop.service.cart.CartService;
import com.coffee_shop.coffeeshop.service.cart.dto.response.CartResponse;

import lombok.RequiredArgsConstructor;

@RequestMapping("/api/users/{userId}/carts")
@RequiredArgsConstructor
@RestController
public class CartController {

	private final CartService cartService;

	@PostMapping
	public ApiResponse<CartResponse> updateCartItem(@PathVariable Long userId,
		@RequestBody @Valid CartSaveRequest request) {
		CartResponse response = cartService.updateCartItem(userId, request.toServiceRequest());
		return ApiResponse.ok(response);
	}

	@DeleteMapping
	public ApiResponse<Void> deleteCartItem(@PathVariable Long userId,
		@RequestBody @Valid CartDeleteRequest request) {
		cartService.deleteCartItem(userId, request.toServiceRequest());
		return ApiResponse.noContent();
	}

	@GetMapping
	public ApiResponse<List<CartResponse>> findCartItems(@PathVariable Long userId) {
		List<CartResponse> response = cartService.findCartItems(userId);
		return ApiResponse.ok(response);
	}
}
