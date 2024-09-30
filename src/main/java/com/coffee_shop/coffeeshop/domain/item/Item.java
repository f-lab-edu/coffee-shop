package com.coffee_shop.coffeeshop.domain.item;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.coffee_shop.coffeeshop.common.domain.BaseTimeEntity;
import com.coffee_shop.coffeeshop.service.item.dto.request.ItemSaveServiceRequest;
import com.coffee_shop.coffeeshop.service.item.dto.request.ItemUpdateServiceRequest;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "items")
public class Item extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Enumerated(EnumType.STRING)
	private ItemType itemType;

	@Column(nullable = false)
	private int price;

	@Column(nullable = false)
	private LocalDateTime lastModifiedDateTime;

	@Builder
	private Item(String name, ItemType itemType, int price, LocalDateTime lastModifiedDateTime) {
		this.name = name;
		this.itemType = itemType;
		this.price = price;
		this.lastModifiedDateTime = lastModifiedDateTime;
	}

	public static Item of(ItemSaveServiceRequest request, LocalDateTime lastModifiedDateTime) {
		return Item.builder()
			.name(request.getName())
			.itemType(request.getItemType())
			.price(request.getPrice())
			.lastModifiedDateTime(lastModifiedDateTime)
			.build();
	}

	public Item update(ItemUpdateServiceRequest request, LocalDateTime lastModifiedDateTime) {
		this.name = request.getName();
		this.itemType = request.getItemType();
		this.price = request.getPrice();
		this.lastModifiedDateTime = lastModifiedDateTime;
		return this;
	}
}
