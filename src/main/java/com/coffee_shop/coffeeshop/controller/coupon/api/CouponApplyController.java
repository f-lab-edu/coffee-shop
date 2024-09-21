package com.coffee_shop.coffeeshop.controller.coupon.api;

import java.net.URI;
import java.time.LocalDateTime;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coffee_shop.coffeeshop.common.dto.response.ApiResponse;
import com.coffee_shop.coffeeshop.controller.coupon.api.dto.request.CouponApplyRequest;
import com.coffee_shop.coffeeshop.service.coupon.CouponApplyService;

import lombok.RequiredArgsConstructor;

@RequestMapping("/api/coupons")
@RequiredArgsConstructor
@RestController
public class CouponApplyController {
	private final CouponApplyService couponApplyService;

	@PostMapping("/apply")
	public ResponseEntity<ApiResponse<Void>> issueCoupon(@RequestBody @Valid CouponApplyRequest request) {
		couponApplyService.applyCoupon(request.toServiceRequest(), LocalDateTime.now());
		return ResponseEntity.created(
				URI.create("/api/users/" + request.getUserId() + "/coupons/" + request.getCouponId()))
			.body(ApiResponse.created());
	}
}
