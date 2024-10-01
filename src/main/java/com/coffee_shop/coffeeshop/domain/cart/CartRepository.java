package com.coffee_shop.coffeeshop.domain.cart;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.coffee_shop.coffeeshop.domain.item.Item;
import com.coffee_shop.coffeeshop.domain.user.User;

public interface CartRepository extends JpaRepository<Cart, Long> {
	@Query("select c from Cart c join fetch c.item i where c.user = :user")
	List<Cart> findByUserFetchJoin(User user);

	void deleteByUserAndItem(User user, Item item);

	Optional<Cart> findByUserAndItem(User user, Item item);

	List<Cart> findByUser(User user);
}
