package com.coffee_shop.coffeeshop.domain.coupon.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coffee_shop.coffeeshop.domain.coupon.Coupon;
import com.coffee_shop.coffeeshop.domain.coupon.CouponIssueFailHistory;
import com.coffee_shop.coffeeshop.domain.user.User;

public interface CouponIssueFailHistoryRepository extends JpaRepository<CouponIssueFailHistory, Long> {
	Optional<CouponIssueFailHistory> findByCouponAndUser(Coupon coupon, User user);
}
