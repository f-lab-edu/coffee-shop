package com.coffee_shop.coffeeshop.domain.coupon.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coffee_shop.coffeeshop.domain.coupon.Coupon;
import com.coffee_shop.coffeeshop.domain.coupon.CouponTransactionHistory;
import com.coffee_shop.coffeeshop.domain.user.User;

public interface CouponTransactionHistoryRepository extends JpaRepository<CouponTransactionHistory, Long> {
	Optional<CouponTransactionHistory> findByCouponAndUser(Coupon coupon, User user);
}
