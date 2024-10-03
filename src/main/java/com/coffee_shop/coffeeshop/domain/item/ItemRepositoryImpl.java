package com.coffee_shop.coffeeshop.domain.item;

import static com.coffee_shop.coffeeshop.domain.item.QItem.*;
import static org.springframework.util.StringUtils.*;

import java.util.List;

import jakarta.persistence.EntityManager;

import com.coffee_shop.coffeeshop.service.item.dto.request.ItemSearchServiceRequest;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

public class ItemRepositoryImpl implements ItemRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	public ItemRepositoryImpl(EntityManager em) {
		this.queryFactory = new JPAQueryFactory(em);
	}

	@Override
	public List<Item> search(Long itemId, ItemSearchServiceRequest request, int pageSize) {
		return queryFactory
			.selectFrom(item)
			.where(
				ltItemId(itemId),
				nameEq(request.getName()),
				itemTypeEq(request.getItemType())
			)
			.orderBy(item.id.desc())
			.limit(pageSize)
			.fetch();
	}

	private BooleanExpression ltItemId(Long itemId) {
		if (itemId == null) {
			return null;
		}

		return item.id.lt(itemId);
	}

	private BooleanExpression nameEq(String name) {
		return hasText(name) ? item.name.contains(name) : null;
	}

	private BooleanExpression itemTypeEq(String itemType) {
		ItemType type = ItemType.of(itemType);
		if (type == null) {
			return null;
		}

		return item.itemType.eq(type);
	}
}
