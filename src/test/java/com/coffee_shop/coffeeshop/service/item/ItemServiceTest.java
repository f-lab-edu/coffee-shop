package com.coffee_shop.coffeeshop.service.item;

import static com.coffee_shop.coffeeshop.domain.item.ItemType.*;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.coffee_shop.coffeeshop.domain.item.Item;
import com.coffee_shop.coffeeshop.domain.item.ItemRepository;
import com.coffee_shop.coffeeshop.domain.item.ItemType;
import com.coffee_shop.coffeeshop.service.IntegrationTestSupport;
import com.coffee_shop.coffeeshop.service.item.dto.request.ItemSaveServiceRequest;
import com.coffee_shop.coffeeshop.service.item.dto.request.ItemSearchServiceRequest;
import com.coffee_shop.coffeeshop.service.item.dto.request.ItemUpdateServiceRequest;
import com.coffee_shop.coffeeshop.service.item.dto.response.ItemResponse;

class ItemServiceTest extends IntegrationTestSupport {

	@Autowired
	private ItemService itemService;

	@Autowired
	private ItemRepository itemRepository;

	@AfterEach
	void tearDown() {
		itemRepository.deleteAllInBatch();
	}

	@DisplayName("상품 정보를 저장한다.")
	@Test
	void saveItem() {
		//given
		LocalDateTime lastModifiedDateTime = LocalDateTime.of(2024, 9, 21, 0, 0);

		ItemSaveServiceRequest request = ItemSaveServiceRequest.builder()
			.name("카페라떼")
			.itemType(COFFEE)
			.price(5000)
			.build();

		//when
		Long itemId = itemService.createItem(request, lastModifiedDateTime);

		//then
		assertThat(itemId).isNotNull();

		List<Item> items = itemRepository.findAll();
		assertThat(items).hasSize(1)
			.extracting("id", "name", "itemType", "price", "lastModifiedDateTime")
			.containsExactlyInAnyOrder(tuple(itemId, "카페라떼", COFFEE, 5000, lastModifiedDateTime));
	}

	@DisplayName("상품 정보를 수정한다.")
	@Test
	void updateItem() {
		//given
		LocalDateTime lastModifiedDateTime = LocalDateTime.of(2024, 9, 21, 0, 0);
		Item savedItem = createItem("카페라떼", COFFEE, 5000, lastModifiedDateTime);

		LocalDateTime updatedModifiedDateTime = LocalDateTime.of(2024, 9, 22, 0, 0);
		ItemUpdateServiceRequest request = ItemUpdateServiceRequest.builder()
			.name("케이크")
			.itemType(DESSERT)
			.price(6000)
			.build();

		//when
		itemService.updateItem(savedItem.getId(), request, updatedModifiedDateTime);

		//then
		List<Item> items = itemRepository.findAll();
		assertThat(items).hasSize(1)
			.extracting("id", "name", "itemType", "price", "lastModifiedDateTime")
			.containsExactlyInAnyOrder(tuple(savedItem.getId(), "케이크", DESSERT, 6000, updatedModifiedDateTime));
	}

	@DisplayName("상품을 삭제한다.")
	@Test
	void deleteItem() {
		//given
		LocalDateTime lastModifiedDateTime = LocalDateTime.of(2024, 9, 21, 0, 0);
		Item savedItem = createItem("카페라떼", COFFEE, 5000, lastModifiedDateTime);

		//when
		itemService.deleteItem(savedItem.getId());

		//then
		List<Item> items = itemRepository.findAll();
		assertThat(items).isEmpty();
	}

	@DisplayName("상품 목록을 첫페이지를 조회한다.")
	@Test
	void findItemsFirstPage() {
		//given
		int totalSize = 30;
		LocalDateTime lastModifiedDateTime = LocalDateTime.of(2024, 9, 21, 0, 0);
		for (int i = 1; i <= totalSize; i++) {
			itemRepository.save(createItem("카페라떼" + i, COFFEE, 5000, lastModifiedDateTime));
		}

		ItemSearchServiceRequest request = ItemSearchServiceRequest.builder()
			.build();

		List<Item> items = itemRepository.findAll();

		//when
		List<ItemResponse> itemResponses = itemService.findItems(null, request, 10);

		//then
		assertThat(itemResponses).hasSize(10);
		assertThat(itemResponses.get(0).getId()).isEqualTo(items.get(totalSize - 1).getId());
		assertThat(itemResponses.get(9).getId()).isEqualTo(items.get(totalSize - 10).getId());
	}

	@DisplayName("상품 목록을 두번째 페이지를 조회한다.")
	@Test
	void findItemsSecondPage() {
		//given
		int totalSize = 30;
		for (int i = 1; i <= totalSize; i++) {
			LocalDateTime lastModifiedDateTime = LocalDateTime.of(2024, 9, i, 0, 0);
			itemRepository.save(createItem("카페라떼" + i, COFFEE, 5000, lastModifiedDateTime));
		}

		ItemSearchServiceRequest request = ItemSearchServiceRequest.builder()
			.build();

		List<Item> items = itemRepository.findAll();
		Long lastId = items.get(totalSize - 10).getId();

		//when
		List<ItemResponse> itemResponses = itemService.findItems(lastId, request, 10);

		//then
		assertThat(itemResponses).hasSize(10);
		assertThat(itemResponses.get(0).getName()).isEqualTo(items.get(totalSize - 11).getName());
		assertThat(itemResponses.get(9).getName()).isEqualTo(items.get(totalSize - 20).getName());
	}

	@DisplayName("상품 목록을 이름으로 검색한다.")
	@Test
	void searchItemsByName() {
		//given
		LocalDateTime lastModifiedDateTime = LocalDateTime.of(2024, 9, 21, 0, 0);
		Item item1 = createItem("카페라떼", COFFEE, 5000, lastModifiedDateTime);
		Item item2 = createItem("바닐라라떼", COFFEE, 5000, lastModifiedDateTime);
		Item item3 = createItem("딸기케이크", DESSERT, 5000, lastModifiedDateTime);

		ItemSearchServiceRequest request = ItemSearchServiceRequest.builder()
			.name("라떼")
			.build();

		//when
		List<ItemResponse> itemResponses = itemService.findItems(null, request, 10);

		//then
		assertThat(itemResponses)
			.hasSize(2)
			.extracting("name")
			.containsExactly(item2.getName(), item1.getName());
	}

	@DisplayName("빈 이름으로 상품 검색시 전체 상품이 조회된다.")
	@Test
	void searchItemsByEmptyName() {
		//given
		LocalDateTime lastModifiedDateTime = LocalDateTime.of(2024, 9, 21, 0, 0);
		Item item1 = createItem("카페라떼", COFFEE, 5000, lastModifiedDateTime);
		Item item2 = createItem("바닐라라떼", COFFEE, 5000, lastModifiedDateTime);
		Item item3 = createItem("딸기케이크", DESSERT, 5000, lastModifiedDateTime);

		ItemSearchServiceRequest request = ItemSearchServiceRequest.builder()
			.name("")
			.build();

		//when
		List<ItemResponse> itemResponses = itemService.findItems(null, request, 10);

		//then
		assertThat(itemResponses).hasSize(3);
	}

	@DisplayName("상품 목록을 상품 타입별로 조회한다.")
	@Test
	void searchItemsByType() {
		//given
		LocalDateTime lastModifiedDateTime = LocalDateTime.of(2024, 9, 21, 0, 0);
		Item item1 = createItem("카페라떼", COFFEE, 5000, lastModifiedDateTime);
		Item item2 = createItem("바나나케이크", DESSERT, 5000, lastModifiedDateTime);
		Item item3 = createItem("딸기케이크", DESSERT, 5000, lastModifiedDateTime);

		ItemSearchServiceRequest request = ItemSearchServiceRequest.builder()
			.itemType("DESSERT")
			.build();

		//when
		List<ItemResponse> itemResponses = itemService.findItems(null, request, 10);

		//then
		assertThat(itemResponses)
			.hasSize(2)
			.extracting("name")
			.containsExactly(item3.getName(), item2.getName());
	}

	@DisplayName("유효하지 않는 타입으로 상품 조회시 전체 상품이 조회된다.")
	@Test
	void searchItemsByInvalidType() {
		//given
		LocalDateTime lastModifiedDateTime = LocalDateTime.of(2024, 9, 21, 0, 0);
		Item item1 = createItem("카페라떼", COFFEE, 5000, lastModifiedDateTime);
		Item item2 = createItem("바나나케이크", DESSERT, 5000, lastModifiedDateTime);
		Item item3 = createItem("딸기케이크", DESSERT, 5000, lastModifiedDateTime);

		ItemSearchServiceRequest request = ItemSearchServiceRequest.builder()
			.itemType("test")
			.build();

		//when
		List<ItemResponse> itemResponses = itemService.findItems(null, request, 10);

		//then
		assertThat(itemResponses).hasSize(3);
	}

	@DisplayName("상품 목록을 상품타입과 이름으로 조회한다.")
	@Test
	void searchItemsByNameAndType() {
		//given
		LocalDateTime lastModifiedDateTime = LocalDateTime.of(2024, 9, 21, 0, 0);
		Item item1 = itemRepository.save(createItem("카페라떼", COFFEE, 5000, lastModifiedDateTime));
		Item item2 = itemRepository.save(createItem("딸기라떼", COFFEE, 5000, lastModifiedDateTime));
		Item item3 = itemRepository.save(createItem("딸기케이크", DESSERT, 5000, lastModifiedDateTime));

		ItemSearchServiceRequest request = ItemSearchServiceRequest.builder()
			.name("딸기")
			.itemType("DESSERT")
			.build();

		//when
		List<ItemResponse> itemResponses = itemService.findItems(null, request, 10);

		//then
		assertThat(itemResponses)
			.hasSize(1)
			.extracting("name")
			.containsExactly(item3.getName());
	}

	@DisplayName("상품 상세정보를 조회한다.")
	@Test
	void findItemById() {
		//given
		LocalDateTime lastModifiedDateTime = LocalDateTime.of(2024, 9, 21, 0, 0);
		Item item = itemRepository.save(createItem("카페라떼", COFFEE, 5000, lastModifiedDateTime));

		//when
		ItemResponse itemResponse = itemService.findItem(item.getId());

		//then
		assertThat(itemResponse)
			.extracting("name", "itemType", "price", "lastModifiedDateTime")
			.contains(item.getName(), item.getItemType(), item.getPrice(), lastModifiedDateTime);
	}

	private Item createItem(String name, ItemType type, int price, LocalDateTime lastModifiedDateTime) {
		Item item = Item.builder()
			.name(name)
			.itemType(type)
			.price(price)
			.lastModifiedDateTime(lastModifiedDateTime)
			.build();

		return itemRepository.save(item);
	}
}
