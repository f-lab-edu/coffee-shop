package com.coffee_shop.coffeeshop.domain.cart;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.coffee_shop.coffeeshop.domain.item.Item;
import com.coffee_shop.coffeeshop.domain.user.User;

public interface CartRepository extends JpaRepository<Cart, Long> {
	@Query("select c from Cart c join fetch c.item i where c.user.id = :userId")
	List<Cart> findByUserIdFetchJoin(@Param("userId") Long userId);

	@Modifying
	@Query("delete from Cart c where c.user.id = :userId and c.item.id = :itemId")
	void deleteByUserIdAndItemId(@Param("userId") Long userId, @Param("itemId") Long itemId);

	Cart findByUserAndItem(User user, Item item);
}
