package com.coffee_shop.coffeeshop.domain.item;

import java.util.List;

import com.coffee_shop.coffeeshop.service.item.dto.request.ItemSearchServiceRequest;

public interface ItemRepositoryCustom {

	List<Item> search(Long itemId, ItemSearchServiceRequest request, int pageSize);
}
