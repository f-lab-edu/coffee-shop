package com.coffee_shop.coffeeshop.controller.coupon;

import java.net.URI;
import java.time.LocalDateTime;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.coffee_shop.coffeeshop.common.dto.response.ApiResponse;
import com.coffee_shop.coffeeshop.controller.coupon.dto.request.CouponApplyRequest;
import com.coffee_shop.coffeeshop.service.coupon.applyservice.CouponApplyService;
import com.coffee_shop.coffeeshop.service.coupon.dto.response.CouponApplyResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class CouponApplyController {
	private final CouponApplyService couponApplyService;

	@PostMapping("/api/coupons/apply")
	public ResponseEntity<ApiResponse<Void>> issueCoupon(@RequestBody @Valid CouponApplyRequest request) {
		couponApplyService.applyCoupon(request.toServiceRequest(), LocalDateTime.now());
		return ResponseEntity.created(
				URI.create("/api/users/" + request.getUserId() + "/coupons/" + request.getCouponId()))
			.body(ApiResponse.created());
	}

	@GetMapping("/api/users/{userId}/coupons/{couponId}")
	public ApiResponse<CouponApplyResponse> isCouponIssued(@PathVariable Long userId, @PathVariable Long couponId) {
		CouponApplyResponse response = couponApplyService.isCouponIssued(userId, couponId);
		return ApiResponse.ok(response);
	}
}
