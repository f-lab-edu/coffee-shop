package com.coffee_shop.coffeeshop.controller.coupon.api;

import static com.coffee_shop.coffeeshop.domain.coupon.CouponType.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import com.coffee_shop.coffeeshop.controller.ControllerSupport;
import com.coffee_shop.coffeeshop.controller.coupon.api.dto.request.CouponSaveRequest;
import com.coffee_shop.coffeeshop.service.coupon.CouponService;

@WebMvcTest(controllers = CouponController.class)
class CouponControllerTest extends ControllerSupport {

	@MockBean
	protected CouponService couponService;

	@DisplayName("쿠폰 등록 시 쿠폰명은 필수값이다.")
	@Test
	void createCouponWhenNameIsEmpty() throws Exception {
		//given
		CouponSaveRequest request = CouponSaveRequest.builder()
			.name("")
			.type(PERCENTAGE.getCode())
			.discountAmount(100)
			.minOrderAmount(100)
			.build();

		//when //then
		mockMvc.perform(
				post("/api/coupons")
					.content(objectMapper.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"))
			.andExpect(jsonPath("$.message").value("적절하지 않은 요청 값입니다."))
			.andExpect(jsonPath("$.fieldErrors[0].field").value("name"))
			.andExpect(jsonPath("$.fieldErrors[0].message").value("쿠폰명은 필수입니다."));
	}

	@DisplayName("쿠폰 등록 시 쿠폰명은 30자까지 입력가능하다.")
	@Test
	void createCouponWhenNameIsOverMaxLength() throws Exception {
		//given
		CouponSaveRequest request = CouponSaveRequest.builder()
			.name("A".repeat(31))
			.type(PERCENTAGE.getCode())
			.discountAmount(100)
			.minOrderAmount(100)
			.build();

		//when //then
		mockMvc.perform(
				post("/api/coupons")
					.content(objectMapper.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"))
			.andExpect(jsonPath("$.message").value("적절하지 않은 요청 값입니다."))
			.andExpect(jsonPath("$.fieldErrors[0].field").value("name"))
			.andExpect(jsonPath("$.fieldErrors[0].message").value("쿠폰명은 최대 30자까지 입력가능합니다."));
	}

	@DisplayName("쿠폰 등록 시 쿠폰 타입은 필수값이다.")
	@Test
	void createCouponWhenTypeIsEmpty() throws Exception {
		//given
		CouponSaveRequest request = CouponSaveRequest.builder()
			.name("할인쿠폰")
			.type("")
			.discountAmount(100)
			.minOrderAmount(100)
			.build();

		//when //then
		mockMvc.perform(
				post("/api/coupons")
					.content(objectMapper.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"))
			.andExpect(jsonPath("$.message").value("적절하지 않은 요청 값입니다."))
			.andExpect(jsonPath("$.fieldErrors[0].field").value("type"))
			.andExpect(jsonPath("$.fieldErrors[0].message").value("쿠폰 타입은 필수입니다."));
	}

	@DisplayName("쿠폰 등록 시 쿠폰 타입은 null이 아니어야 한다.")
	@Test
	void createCouponWhenTypeIsNull() throws Exception {
		//given
		CouponSaveRequest request = CouponSaveRequest.builder()
			.name("할인쿠폰")
			.type(null)
			.discountAmount(100)
			.minOrderAmount(100)
			.build();

		//when //then
		mockMvc.perform(
				post("/api/coupons")
					.content(objectMapper.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"))
			.andExpect(jsonPath("$.message").value("적절하지 않은 요청 값입니다."))
			.andExpect(jsonPath("$.fieldErrors[0].field").value("type"))
			.andExpect(jsonPath("$.fieldErrors[0].message").value("쿠폰 타입은 필수입니다."));
	}

	@DisplayName("쿠폰 등록 시 유효한 쿠폰 타입을 입력해야한다.")
	@Test
	void createCouponWhenCouponTypeIsInvalid() throws Exception {
		//given
		CouponSaveRequest request = CouponSaveRequest.builder()
			.name("할인쿠폰")
			.type("test")
			.discountAmount(100)
			.minOrderAmount(100)
			.build();

		//when //then
		mockMvc.perform(
				post("/api/coupons")
					.content(objectMapper.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("COUPON_TYPE_NOT_FOUND"))
			.andExpect(jsonPath("$.message").value("유효하지 않는 쿠폰 타입입니다."));
	}

	@DisplayName("쿠폰 등록 시 할인 금액은 양수이다.")
	@Test
	void createCouponWhenDiscountAmountIsZero() throws Exception {
		//given
		CouponSaveRequest request = CouponSaveRequest.builder()
			.name("할인쿠폰")
			.type(AMOUNT.getCode())
			.discountAmount(0)
			.minOrderAmount(100)
			.build();

		//when //then
		mockMvc.perform(
				post("/api/coupons")
					.content(objectMapper.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"))
			.andExpect(jsonPath("$.message").value("적절하지 않은 요청 값입니다."))
			.andExpect(jsonPath("$.fieldErrors[0].field").value("discountAmount"))
			.andExpect(jsonPath("$.fieldErrors[0].message").value("할인금액은 양수입니다."));
	}

	@DisplayName("쿠폰 등록 시 쿠폰 타입이 비율 할인일 경우 할인 금액은 1~100까지 입력가능하다.")
	@Test
	void createCouponWhenCouponTypeIsPercentage() throws Exception {
		//given
		CouponSaveRequest request = CouponSaveRequest.builder()
			.name("할인쿠폰")
			.type(PERCENTAGE.getCode())
			.discountAmount(101)
			.minOrderAmount(100)
			.build();

		//when //then
		mockMvc.perform(
				post("/api/coupons")
					.content(objectMapper.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_DISCOUNT_PERCENTAGE"))
			.andExpect(jsonPath("$.message").value("할인율은 1~100까지 입력가능합니다."));
	}

	@DisplayName("쿠폰 등록 시 주문 최소 금액은 0일수 있다.")
	@Test
	void createCouponWhenMinOrderAmountIsZero() throws Exception {
		//given
		CouponSaveRequest request = CouponSaveRequest.builder()
			.name("할인쿠폰")
			.type(AMOUNT.getCode())
			.discountAmount(100)
			.minOrderAmount(0)
			.build();

		when(couponService.createCoupon(any())).thenReturn(1L);

		//when //then
		mockMvc.perform(
				post("/api/coupons")
					.content(objectMapper.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.code").value("CREATED"))
			.andExpect(jsonPath("$.message").value("CREATED"))
			.andExpect(header().string("Location", "/api/coupons/1"));
	}

	@DisplayName("쿠폰 등록 시 주문 최소 금액은 0이상이다.")
	@Test
	void createCouponWhenMinOrderAmountIsNegativeNumber() throws Exception {
		//given
		CouponSaveRequest request = CouponSaveRequest.builder()
			.name("할인쿠폰")
			.type(AMOUNT.getCode())
			.discountAmount(100)
			.minOrderAmount(-1)
			.build();

		//when //then
		mockMvc.perform(
				post("/api/coupons")
					.content(objectMapper.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"))
			.andExpect(jsonPath("$.message").value("적절하지 않은 요청 값입니다."))
			.andExpect(jsonPath("$.fieldErrors[0].field").value("minOrderAmount"))
			.andExpect(jsonPath("$.fieldErrors[0].message").value("주문 최소 금액은 0이상입니다."));
	}

	@DisplayName("쿠폰 등록 시 최대 발급 개수는 0이상이다.")
	@Test
	void createCouponWhenMaxIssueCountIsNegativeNumber() throws Exception {
		//given
		CouponSaveRequest request = CouponSaveRequest.builder()
			.name("할인쿠폰")
			.type(AMOUNT.getCode())
			.discountAmount(100)
			.minOrderAmount(100)
			.maxIssueCount(-1)
			.build();

		//when //then
		mockMvc.perform(
				post("/api/coupons")
					.content(objectMapper.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"))
			.andExpect(jsonPath("$.message").value("적절하지 않은 요청 값입니다."))
			.andExpect(jsonPath("$.fieldErrors[0].field").value("maxIssueCount"))
			.andExpect(jsonPath("$.fieldErrors[0].message").value("최대 발급개수는 0이상입니다. 0일경우 무제한으로 발급가능합니다."));
	}

	@DisplayName("쿠폰을 생성한다.")
	@Test
	void createCoupon() throws Exception {
		//given
		CouponSaveRequest request = CouponSaveRequest.builder()
			.name("할인쿠폰")
			.type(AMOUNT.getCode())
			.discountAmount(1000)
			.minOrderAmount(10000)
			.maxIssueCount(1000)
			.build();

		when(couponService.createCoupon(any())).thenReturn(1L);

		//when //then
		mockMvc.perform(
				post("/api/coupons")
					.content(objectMapper.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.code").value("CREATED"))
			.andExpect(jsonPath("$.message").value("CREATED"))
			.andExpect(header().string("Location", "/api/coupons/1"));
	}
}
