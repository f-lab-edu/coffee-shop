package com.coffee_shop.coffeeshop.controller.coupon.api;

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
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;

import com.coffee_shop.coffeeshop.controller.RestDocsSupport;
import com.coffee_shop.coffeeshop.controller.coupon.CouponApplyController;
import com.coffee_shop.coffeeshop.controller.coupon.dto.request.CouponApplyRequest;
import com.coffee_shop.coffeeshop.docs.coupon.CouponDocumentation;
import com.coffee_shop.coffeeshop.domain.coupon.CouponIssueStatus;
import com.coffee_shop.coffeeshop.service.coupon.apply.CouponApplyService;
import com.coffee_shop.coffeeshop.service.coupon.dto.response.CouponApplyResponse;

@WebMvcTest(controllers = CouponApplyController.class)
class CouponApplyControllerTest extends RestDocsSupport {

	@MockBean
	protected CouponApplyService couponApplyService;

	@DisplayName("쿠폰 발급 시 사용자 id는 필수값이다.")
	@Test
	void applyCouponWhenUserIdIsNull() throws Exception {
		//given
		CouponApplyRequest request = CouponApplyRequest.builder()
			.couponId(1L)
			.build();

		//when //then
		mockMvc.perform(
				post("/api/coupons/apply")
					.content(objectMapper.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"))
			.andExpect(jsonPath("$.message").value("적절하지 않은 요청 값입니다."))
			.andExpect(jsonPath("$.fieldErrors[0].field").value("userId"))
			.andExpect(jsonPath("$.fieldErrors[0].message").value("사용자 ID는 필수입니다."));
	}

	@DisplayName("쿠폰 발급 시 사용자 id는 양수이다.")
	@Test
	void applyCouponWhenUserIdIsZero() throws Exception {
		//given
		CouponApplyRequest request = CouponApplyRequest.builder()
			.userId(0L)
			.couponId(1L)
			.build();

		//when //then
		mockMvc.perform(
				post("/api/coupons/apply")
					.content(objectMapper.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"))
			.andExpect(jsonPath("$.message").value("적절하지 않은 요청 값입니다."))
			.andExpect(jsonPath("$.fieldErrors[0].field").value("userId"))
			.andExpect(jsonPath("$.fieldErrors[0].message").value("사용자 ID는 양수입니다."));
	}

	@DisplayName("쿠폰 발급 시 쿠폰 id는 필수값이다.")
	@Test
	void applyCouponWhenCouponIdIsNull() throws Exception {
		//given
		CouponApplyRequest request = CouponApplyRequest.builder()
			.userId(1L)
			.build();

		//when //then
		mockMvc.perform(
				post("/api/coupons/apply")
					.content(objectMapper.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"))
			.andExpect(jsonPath("$.message").value("적절하지 않은 요청 값입니다."))
			.andExpect(jsonPath("$.fieldErrors[0].field").value("couponId"))
			.andExpect(jsonPath("$.fieldErrors[0].message").value("쿠폰 ID는 필수입니다."));
	}

	@DisplayName("쿠폰 발급 시 쿠폰 id는 양수이다.")
	@Test
	void applyCouponWhenCouponIdIsZero() throws Exception {
		//given
		CouponApplyRequest request = CouponApplyRequest.builder()
			.couponId(0L)
			.userId(1L)
			.build();

		//when //then
		mockMvc.perform(
				post("/api/coupons/apply")
					.content(objectMapper.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"))
			.andExpect(jsonPath("$.message").value("적절하지 않은 요청 값입니다."))
			.andExpect(jsonPath("$.fieldErrors[0].field").value("couponId"))
			.andExpect(jsonPath("$.fieldErrors[0].message").value("쿠폰 ID는 양수입니다."));
	}

	@DisplayName("쿠폰을 생성한다.")
	@Test
	void applyCoupon() throws Exception {
		//given
		CouponApplyRequest request = CouponApplyRequest.builder()
			.couponId(1L)
			.userId(1L)
			.build();

		doNothing().when(couponApplyService).applyCoupon(any());

		//when //then
		mockMvc.perform(
				post("/api/coupons/apply")
					.content(objectMapper.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.code").value("CREATED"))
			.andExpect(jsonPath("$.message").value("CREATED"))
			.andExpect(header().string("Location", "/api/users/1/coupons/1"))
			.andDo(CouponDocumentation.applyCoupon());
	}

	@DisplayName("쿠폰 발급 결과를 조회한다.")
	@Test
	void isIssuedCoupon() throws Exception {
		//given
		CouponApplyResponse couponApplyResponse = CouponApplyResponse.builder()
			.couponIssueStatus(CouponIssueStatus.SUCCESS)
			.position(-1)
			.build();

		when(couponApplyService.isCouponIssued(any(), any())).thenReturn(couponApplyResponse);

		//when //then
		mockMvc.perform(
				RestDocumentationRequestBuilders.get("/api/users/{userId}/coupons/{couponId}", 1L, 1L)
					.contentType(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("OK"))
			.andExpect(jsonPath("$.message").value("OK"))
			.andDo(CouponDocumentation.isIssuedCoupon());
	}
}
