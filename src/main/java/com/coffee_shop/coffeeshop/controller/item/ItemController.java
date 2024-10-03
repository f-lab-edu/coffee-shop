package com.coffee_shop.coffeeshop.controller.item;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.coffee_shop.coffeeshop.common.dto.response.ApiResponse;
import com.coffee_shop.coffeeshop.controller.item.dto.request.ItemSaveRequest;
import com.coffee_shop.coffeeshop.controller.item.dto.request.ItemSearchRequest;
import com.coffee_shop.coffeeshop.controller.item.dto.request.ItemUpdateRequest;
import com.coffee_shop.coffeeshop.service.item.ItemService;
import com.coffee_shop.coffeeshop.service.item.dto.response.ItemResponse;

import lombok.RequiredArgsConstructor;

@Validated
@RequestMapping("/api/items")
@RequiredArgsConstructor
@RestController
public class ItemController {

	private final ItemService itemService;

	@PostMapping
	public ResponseEntity<ApiResponse<Void>> createItem(@RequestBody @Valid ItemSaveRequest request) {
		Long itemId = itemService.createItem(request.toServiceRequest(), LocalDateTime.now());
		return ResponseEntity.created(URI.create("/api/items/" + itemId)).body(ApiResponse.created());
	}

	@PutMapping("/{itemId}")
	public ApiResponse<Void> updateItem(@PathVariable Long itemId, @RequestBody @Valid ItemUpdateRequest request) {
		itemService.updateItem(itemId, request.toServiceRequest(), LocalDateTime.now());
		return ApiResponse.noContent();
	}

	@DeleteMapping("/{itemId}")
	public ApiResponse<Void> deleteItem(@PathVariable Long itemId) {
		itemService.deleteItem(itemId);
		return ApiResponse.noContent();
	}

	@GetMapping
	public ApiResponse<List<ItemResponse>> findItems(
		Long itemId,
		ItemSearchRequest itemSearchRequest,
		@Max(value = 100, message = "최대 페이지 사이즈는 100입니다.")
		@RequestParam(value = "pageSize", defaultValue = "10") int pageSize
	) {
		List<ItemResponse> response = itemService.findItems(itemId, itemSearchRequest.toServiceRequest(), pageSize);
		return ApiResponse.ok(response);
	}

	@GetMapping("/{itemId}")
	public ApiResponse<ItemResponse> findItem(@PathVariable Long itemId) {
		ItemResponse response = itemService.findItem(itemId);
		return ApiResponse.ok(response);
	}

}
