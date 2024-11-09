package com.coffee_shop.coffeeshop.domain.coupon.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coffee_shop.coffeeshop.domain.coupon.Coupon;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
}
