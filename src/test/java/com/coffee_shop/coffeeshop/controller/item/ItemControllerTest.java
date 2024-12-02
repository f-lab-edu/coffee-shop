package com.coffee_shop.coffeeshop.controller.item;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;

import com.coffee_shop.coffeeshop.controller.RestDocsSupport;
import com.coffee_shop.coffeeshop.controller.item.dto.request.ItemSaveRequest;
import com.coffee_shop.coffeeshop.controller.item.dto.request.ItemUpdateRequest;
import com.coffee_shop.coffeeshop.docs.item.ItemDocumentation;
import com.coffee_shop.coffeeshop.domain.item.ItemType;
import com.coffee_shop.coffeeshop.service.item.dto.response.ItemResponse;

class ItemControllerTest extends RestDocsSupport {
	@DisplayName("상품을 등록한다")
	@Test
	void createItem() throws Exception {
		//given
		ItemSaveRequest request = ItemSaveRequest.builder()
			.name("카페라떼")
			.itemType(ItemType.COFFEE)
			.price(5000)
			.build();

		when(itemService.createItem(any(), any())).thenReturn(1L);

		//when //then
		mockMvc.perform(
				RestDocumentationRequestBuilders.post("/api/items")
					.content(objectMapper.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.code").value("CREATED"))
			.andExpect(jsonPath("$.message").value("CREATED"))
			.andExpect(header().string("Location", "/api/items/1"))
			.andDo(ItemDocumentation.createItem());
	}

	@DisplayName("상품 등록 시 상품 이름은 필수값이다.")
	@Test
	void createItemWhenNameIsNull() throws Exception {
		//given
		ItemSaveRequest request = ItemSaveRequest.builder()
			.itemType(ItemType.COFFEE)
			.price(5000)
			.build();

		//when //then
		mockMvc.perform(
				post("/api/items")
					.content(objectMapper.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"))
			.andExpect(jsonPath("$.message").value("적절하지 않은 요청 값입니다."))
			.andExpect(jsonPath("$.fieldErrors[0].field").value("name"))
			.andExpect(jsonPath("$.fieldErrors[0].message").value("상품 이름은 필수입니다."));
	}

	@DisplayName("상품 등록 시 상품 이름은 최소1글자 이상이다.")
	@Test
	void createItemWithEmptyName() throws Exception {
		ItemSaveRequest request = ItemSaveRequest.builder()
			.name("")
			.itemType(ItemType.COFFEE)
			.price(5000)
			.build();

		//when //then
		mockMvc.perform(
				post("/api/items")
					.content(objectMapper.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"))
			.andExpect(jsonPath("$.message").value("적절하지 않은 요청 값입니다."))
			.andExpect(jsonPath("$.fieldErrors[0].field").value("name"))
			.andExpect(jsonPath("$.fieldErrors[0].message").value("상품 이름은 필수입니다."));

	}

	@DisplayName("상품 등록 시 상품 이름은 최대 100자까지 입력 가능하다.")
	@Test
	void createItemWhenNameIsOverMaxLength() throws Exception {
		ItemSaveRequest request = ItemSaveRequest.builder()
			.name("A".repeat(101))
			.itemType(ItemType.COFFEE)
			.price(5000)
			.build();

		//when //then
		mockMvc.perform(
				post("/api/items")
					.content(objectMapper.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"))
			.andExpect(jsonPath("$.message").value("적절하지 않은 요청 값입니다."))
			.andExpect(jsonPath("$.fieldErrors[0].field").value("name"))
			.andExpect(jsonPath("$.fieldErrors[0].message").value("상품 이름은 최대 100자까지 입력가능합니다."));

	}

	@DisplayName("상품 등록 시 상품 타입은 null이 아니어야 한다.")
	@Test
	void createItemWhenTypeIsNull() throws Exception {
		//given
		ItemSaveRequest request = ItemSaveRequest.builder()
			.name("카페라떼")
			.price(5000)
			.itemType(null)
			.build();

		//when //then
		mockMvc.perform(
				post("/api/items")
					.content(objectMapper.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"))
			.andExpect(jsonPath("$.message").value("적절하지 않은 요청 값입니다."))
			.andExpect(jsonPath("$.fieldErrors[0].field").value("itemType"))
			.andExpect(jsonPath("$.fieldErrors[0].message").value("유효하지 않는 상품 타입입니다."));
	}

	@DisplayName("상품 등록 시 상품 가격은 최소 100원입니다.")
	@Test
	void createItemWhenPriceIsLessThanMinimumPrice() throws Exception {
		//given
		ItemSaveRequest request = ItemSaveRequest.builder()
			.name("카페라떼")
			.itemType(ItemType.COFFEE)
			.price(99)
			.build();

		//when //then
		mockMvc.perform(
				post("/api/items")
					.content(objectMapper.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"))
			.andExpect(jsonPath("$.message").value("적절하지 않은 요청 값입니다."))
			.andExpect(jsonPath("$.fieldErrors[0].field").value("price"))
			.andExpect(jsonPath("$.fieldErrors[0].message").value("최소 상품 가격은 100원입니다."));
	}

	@DisplayName("상품을 수정한다")
	@Test
	void updateItem() throws Exception {
		//given
		ItemUpdateRequest request = ItemUpdateRequest.builder()
			.name("카페라떼")
			.itemType(ItemType.COFFEE)
			.price(5000)
			.build();

		//when //then
		mockMvc.perform(
				RestDocumentationRequestBuilders.put("/api/items/{itemId}", 1L)
					.content(objectMapper.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("OK"))
			.andExpect(jsonPath("$.message").value("OK"))
			.andDo(ItemDocumentation.updateItem());
	}

	@DisplayName("상품 수정 시 상품 이름은 필수값이다.")
	@Test
	void updateItemWithoutName() throws Exception {
		//given
		ItemUpdateRequest request = ItemUpdateRequest.builder()
			.itemType(ItemType.COFFEE)
			.price(5000)
			.build();

		//when //then
		mockMvc.perform(
				put("/api/items/{itemId}", 1L)
					.content(objectMapper.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"))
			.andExpect(jsonPath("$.message").value("적절하지 않은 요청 값입니다."))
			.andExpect(jsonPath("$.fieldErrors[0].field").value("name"))
			.andExpect(jsonPath("$.fieldErrors[0].message").value("상품 이름은 필수입니다."));
	}

	@DisplayName("상품 수정 시 상품 이름은 최소1글자 이상이다.")
	@Test
	void updateItemWithEmptyName() throws Exception {
		ItemUpdateRequest request = ItemUpdateRequest.builder()
			.name("")
			.itemType(ItemType.COFFEE)
			.price(5000)
			.build();

		//when //then
		mockMvc.perform(
				put("/api/items/{itemId}", 1L)
					.content(objectMapper.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"))
			.andExpect(jsonPath("$.message").value("적절하지 않은 요청 값입니다."))
			.andExpect(jsonPath("$.fieldErrors[0].field").value("name"))
			.andExpect(jsonPath("$.fieldErrors[0].message").value("상품 이름은 필수입니다."));

	}

	@DisplayName("상품 수정 시 상품 이름은 최대100자까지 입력 가능하다.")
	@Test
	void updateItemWhenNameIsOverMaxLength() throws Exception {
		ItemUpdateRequest request = ItemUpdateRequest.builder()
			.name("A".repeat(101))
			.itemType(ItemType.COFFEE)
			.price(5000)
			.build();

		//when //then
		mockMvc.perform(
				put("/api/items/{itemId}", 1L)
					.content(objectMapper.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"))
			.andExpect(jsonPath("$.message").value("적절하지 않은 요청 값입니다."))
			.andExpect(jsonPath("$.fieldErrors[0].field").value("name"))
			.andExpect(jsonPath("$.fieldErrors[0].message").value("상품 이름은 최대 100자까지 입력가능합니다."));

	}

	@DisplayName("상품 수정 시 상품 타입은 null이 아니어야 한다.")
	@Test
	void updateItemWhenTypeIsNull() throws Exception {
		//given
		ItemUpdateRequest request = ItemUpdateRequest.builder()
			.name("카페라떼")
			.price(5000)
			.build();

		//when //then
		mockMvc.perform(
				put("/api/items/{itemId}", 1L)
					.content(objectMapper.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"))
			.andExpect(jsonPath("$.message").value("적절하지 않은 요청 값입니다."))
			.andExpect(jsonPath("$.fieldErrors[0].field").value("itemType"))
			.andExpect(jsonPath("$.fieldErrors[0].message").value("유효하지 않는 상품 타입입니다."));
	}

	@DisplayName("상품 수정 시 최소 상품 가격은 100원이다.")
	@Test
	void updateItemWhenPriceIsLessThanMinimumPrice() throws Exception {
		//given
		ItemUpdateRequest request = ItemUpdateRequest.builder()
			.name("카페라떼")
			.itemType(ItemType.COFFEE)
			.price(99)
			.build();

		//when //then
		mockMvc.perform(
				put("/api/items/{itemId}", 1L)
					.content(objectMapper.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"))
			.andExpect(jsonPath("$.message").value("적절하지 않은 요청 값입니다."))
			.andExpect(jsonPath("$.fieldErrors[0].field").value("price"))
			.andExpect(jsonPath("$.fieldErrors[0].message").value("최소 상품 가격은 100원입니다."));
	}

	@DisplayName("상품을 삭제한다.")
	@Test
	void deleteItem() throws Exception {
		//when //then
		mockMvc.perform(
				RestDocumentationRequestBuilders.delete("/api/items/{itemId}", 1L)
					.contentType(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("OK"))
			.andExpect(jsonPath("$.message").value("OK"))
			.andDo(ItemDocumentation.deleteItem());
	}

	@DisplayName("상품 목록을 조회한다.")
	@Test
	void findItems() throws Exception {
		//given
		ItemResponse itemResponse = ItemResponse.builder()
			.id(1L)
			.name("아이스아메리카노")
			.itemType(ItemType.COFFEE)
			.price(1000)
			.lastModifiedDateTime(LocalDateTime.now())
			.build();

		List<ItemResponse> result = List.of(itemResponse);

		when(itemService.findItems(any(), any(), anyInt())).thenReturn(result);

		//when //then
		mockMvc.perform(
				RestDocumentationRequestBuilders.get("/api/items")
					.contentType(MediaType.APPLICATION_JSON)
					.queryParam("page", "0")
					.queryParam("size", "1")
					.queryParam("name", "아이스")
					.queryParam("itemType", "COFFEE")
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("OK"))
			.andExpect(jsonPath("$.message").value("OK"))
			.andExpect(jsonPath("$.data").isArray())
			.andDo(ItemDocumentation.findItems());
	}

	@DisplayName("검색 조건을 빈값으로 검색 시 전체 상품 목록이 조회된다.")
	@Test
	void findItemsWithEmptySearchConditions() throws Exception {
		//given
		List<ItemResponse> result = List.of();

		when(itemService.findItems(any(), any(), anyInt())).thenReturn(result);

		//when //then
		mockMvc.perform(
				get("/api/items")
					.queryParam("name", "")
					.queryParam("itemType", "")
					.contentType(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("OK"))
			.andExpect(jsonPath("$.message").value("OK"))
			.andExpect(jsonPath("$.data").isArray());
	}

	@DisplayName("상품 목록 최대 요청 사이즈는 100이다.")
	@Test
	void findItemsMaxValue() throws Exception {
		//when //then
		mockMvc.perform(
				get("/api/items")
					.contentType(MediaType.APPLICATION_JSON)
					.queryParam("pageSize", "101")
			)
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"))
			.andExpect(jsonPath("$.message").value("적절하지 않은 요청 값입니다."))
			.andExpect(jsonPath("$.fieldErrors[0].field").value("findItems.pageSize"))
			.andExpect(jsonPath("$.fieldErrors[0].message").value("최대 페이지 사이즈는 100입니다."));
	}

	@DisplayName("상품 상세정보를 조회한다.")
	@Test
	void findItem() throws Exception {
		//given
		ItemResponse itemResponse = ItemResponse.builder()
			.id(1L)
			.name("아이스아메리카노")
			.itemType(ItemType.COFFEE)
			.price(1000)
			.lastModifiedDateTime(LocalDateTime.now())
			.build();

		when(itemService.findItem(any())).thenReturn(itemResponse);

		//when //then
		mockMvc.perform(
				RestDocumentationRequestBuilders.get("/api/items/{itemId}", 1L)
					.contentType(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("OK"))
			.andExpect(jsonPath("$.message").value("OK"))
			.andDo(ItemDocumentation.findItem());
	}
}
