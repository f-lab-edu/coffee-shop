package com.coffee_shop.coffeeshop.controller.cart;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;

import com.coffee_shop.coffeeshop.controller.RestDocsSupport;
import com.coffee_shop.coffeeshop.controller.cart.dto.request.CartDeleteRequest;
import com.coffee_shop.coffeeshop.controller.cart.dto.request.CartSaveRequest;
import com.coffee_shop.coffeeshop.docs.cart.CartDocumentation;
import com.coffee_shop.coffeeshop.service.cart.dto.response.CartResponse;

class CartControllerTest extends RestDocsSupport {
	@DisplayName("장바구니에 상품을 담는다")
	@Test
	void updateCartItem() throws Exception {
		//given
		CartSaveRequest request = CartSaveRequest.builder()
			.itemId(1L)
			.count(1)
			.build();

		CartResponse response = CartResponse.builder()
			.itemId(1L)
			.itemName("아이스아메리카노")
			.itemCount(1)
			.build();

		when(cartService.updateCartItem(any(), any())).thenReturn(response);

		//when //then
		mockMvc.perform(
				RestDocumentationRequestBuilders.post("/api/users/{user}/carts", 1L)
					.content(objectMapper.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("OK"))
			.andExpect(jsonPath("$.message").value("OK"))
			.andDo(CartDocumentation.updateCart());

	}

	@DisplayName("장바구니에 담긴 상품을 삭제한다.")
	@Test
	void deleteCartItem() throws Exception {
		//given
		CartDeleteRequest request = CartDeleteRequest.builder()
			.itemId(1L)
			.build();

		//when //then
		mockMvc.perform(
				RestDocumentationRequestBuilders.delete("/api/users/{user}/carts", 1L)
					.content(objectMapper.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("OK"))
			.andExpect(jsonPath("$.message").value("OK"))
			.andDo(CartDocumentation.deleteCart());

	}

	@DisplayName("장바구니에 담긴 상품 목록을 조회한다.")
	@Test
	void findCartItems() throws Exception {
		//given
		CartResponse response = CartResponse.builder()
			.itemId(1L)
			.itemName("아이스아메리카노")
			.itemCount(1)
			.build();

		when(cartService.findCartItems(any())).thenReturn(List.of(response));

		//when //then
		mockMvc.perform(
				RestDocumentationRequestBuilders.get("/api/users/{user}/carts", 1L)
					.contentType(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("OK"))
			.andExpect(jsonPath("$.message").value("OK"))
			.andExpect(jsonPath("$.data").isArray())
			.andDo(CartDocumentation.findCarts());

	}
}
