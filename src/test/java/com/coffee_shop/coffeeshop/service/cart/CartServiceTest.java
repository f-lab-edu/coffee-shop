package com.coffee_shop.coffeeshop.service.cart;

import static com.coffee_shop.coffeeshop.domain.item.ItemType.*;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.coffee_shop.coffeeshop.common.exception.BusinessException;
import com.coffee_shop.coffeeshop.domain.cart.Cart;
import com.coffee_shop.coffeeshop.domain.cart.CartRepository;
import com.coffee_shop.coffeeshop.domain.item.Item;
import com.coffee_shop.coffeeshop.domain.item.ItemRepository;
import com.coffee_shop.coffeeshop.domain.user.User;
import com.coffee_shop.coffeeshop.domain.user.UserRepository;
import com.coffee_shop.coffeeshop.service.IntegrationTestSupport;
import com.coffee_shop.coffeeshop.service.cart.dto.request.CartDeleteServiceRequest;
import com.coffee_shop.coffeeshop.service.cart.dto.request.CartSaveServiceRequest;
import com.coffee_shop.coffeeshop.service.cart.dto.response.CartResponse;

class CartServiceTest extends IntegrationTestSupport {

	@Autowired
	private ItemRepository itemRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private CartService cartService;

	@BeforeEach()
	void setUp() {
		createItem();
		createUser();
	}

	@AfterEach
	void tearDown() {
		cartRepository.deleteAllInBatch();
		userRepository.deleteAllInBatch();
		itemRepository.deleteAllInBatch();
	}

	@DisplayName("장바구니 상품을 담을 경우 기존에 장바구니에 담긴 상품이면 수량만 더한다.")
	@Test
	void updateCartItemCount() {
		//given
		Item item = createItem();
		User user = createUser();

		Cart cart = createCart(user, item, 1);
		cartRepository.save(cart);

		CartSaveServiceRequest request = CartSaveServiceRequest.builder()
			.itemId(item.getId())
			.count(2)
			.build();

		//when
		CartResponse cartResponse = cartService.updateCartItem(user.getId(), request);

		//then
		List<Cart> cartList = cartRepository.findAll();
		assertThat(cartList).hasSize(1);
		assertThat(cartResponse)
			.extracting("itemId", "itemName", "itemPrice", "itemCount")
			.contains(item.getId(), item.getName(), item.getPrice(), 3);
	}

	@DisplayName("장바구니에 20개 이상 상품을 담을 수 없다.")
	@Test
	void updateCartItemMaxCount() {
		//given
		Item item = createItem();
		Item item2 = createItem();
		User user = createUser();

		Cart cart1 = createCart(user, item, 10);
		Cart cart2 = createCart(user, item2, 10);
		cartRepository.saveAll(List.of(cart1, cart2));

		CartSaveServiceRequest request = CartSaveServiceRequest.builder()
			.itemId(item.getId())
			.count(1)
			.build();

		//when, then
		assertThatThrownBy(() -> cartService.updateCartItem(user.getId(), request))
			.isInstanceOf(BusinessException.class)
			.hasMessage("최대 주문 가능 수량은 20개 입니다.");
	}

	@DisplayName("기존에 장바구니에 담긴 상품이 아니면 신규 데이터를 생성한다.")
	@Test
	void createCart() {
		//given
		Item item = createItem();
		User user = createUser();

		CartSaveServiceRequest request = CartSaveServiceRequest.builder()
			.itemId(item.getId())
			.count(2)
			.build();

		//when
		CartResponse cartResponse = cartService.updateCartItem(user.getId(), request);

		//then
		List<Cart> cartList = cartRepository.findAll();
		assertThat(cartList).hasSize(1);
		assertThat(cartResponse)
			.extracting("itemId", "itemName", "itemPrice", "itemCount")
			.contains(item.getId(), item.getName(), item.getPrice(), 2);
	}

	@DisplayName("장바구니에서 선택한 아이템을 삭제한다.")
	@Test
	void deleteCartItem() {
		//given
		Item item1 = itemRepository.save(Item.builder()
			.name("카페라떼")
			.itemType(COFFEE)
			.price(5000)
			.lastModifiedDateTime(LocalDateTime.of(2024, 9, 21, 0, 0))
			.build());
		Item item = itemRepository.findById(item1.getId()).get();
		User user = createUser();

		Cart cart = createCart(user, item, 1);
		cartRepository.save(cart);

		CartDeleteServiceRequest request = CartDeleteServiceRequest.builder()
			.itemId(item.getId())
			.build();

		//when
		cartService.deleteCartItem(user.getId(), request);

		//then
		List<Cart> cartItems = cartRepository.findAll();
		assertThat(cartItems).hasSize(0)
			.isEmpty();
	}

	@DisplayName("장바구니에 담긴 상품목록을 조회한다.")
	@Test
	void findCartItems() {
		//given
		Item item1 = createItem();
		Item item2 = createItem();
		User user = createUser();

		Cart cart1 = createCart(user, item1, 1);
		Cart cart2 = createCart(user, item2, 2);
		cartRepository.saveAll(List.of(cart1, cart2));

		//when
		List<CartResponse> cartResponse = cartService.findCartItems(user.getId());

		//then
		assertThat(cartResponse)
			.extracting("itemId", "itemName", "itemPrice", "itemCount")
			.contains(
				tuple(item1.getId(), item1.getName(), item1.getPrice(), 1),
				tuple(item2.getId(), item2.getName(), item2.getPrice(), 2)
			);
	}

	// private Item findItem() {
	// 	return itemRepository.findById(itemId).get();
	// }

	private Cart createCart(User user, Item item, int count) {
		return Cart.builder()
			.user(user)
			.item(item)
			.itemCount(count)
			.build();
	}

	private Item createItem() {
		Item item = Item.builder()
			.name("카페라떼")
			.itemType(COFFEE)
			.price(5000)
			.lastModifiedDateTime(LocalDateTime.of(2024, 9, 21, 0, 0))
			.build();
		return itemRepository.save(item);
	}

	private User createUser() {
		User user = User.builder()
			.name("우경서")
			.build();
		return userRepository.save(user);
	}
}
