package com.coffee_shop.coffeeshop.controller.coupon.api;

import java.net.URI;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coffee_shop.coffeeshop.common.dto.response.ApiResponse;
import com.coffee_shop.coffeeshop.controller.coupon.api.dto.request.CouponSaveRequest;
import com.coffee_shop.coffeeshop.service.coupon.CouponService;

import lombok.RequiredArgsConstructor;

@RequestMapping("/api/coupons")
@RequiredArgsConstructor
@RestController
public class CouponController {
	private final CouponService couponService;

	@PostMapping
	public ResponseEntity<ApiResponse<Void>> createCoupon(@RequestBody @Valid CouponSaveRequest request) {
		Long couponId = couponService.createCoupon(request.toServiceRequest());
		return ResponseEntity.created(URI.create("/api/coupons/" + couponId)).body(ApiResponse.created());
	}
}
