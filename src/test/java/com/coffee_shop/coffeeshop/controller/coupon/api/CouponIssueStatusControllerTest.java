package com.coffee_shop.coffeeshop.controller.coupon.api;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.context.ActiveProfiles;

import com.coffee_shop.coffeeshop.controller.RestDocsSupport;
import com.coffee_shop.coffeeshop.docs.coupon.CouponDocumentation;
import com.coffee_shop.coffeeshop.domain.coupon.CouponIssueStatus;
import com.coffee_shop.coffeeshop.service.coupon.dto.response.CouponApplyResponse;

@ActiveProfiles("messageQ")
class CouponIssueStatusControllerTest extends RestDocsSupport {
	@DisplayName("쿠폰 발급 결과를 조회한다.")
	@Test
	void isIssuedCoupon() throws Exception {
		//given
		CouponApplyResponse couponApplyResponse = CouponApplyResponse.builder()
			.couponIssueStatus(CouponIssueStatus.SUCCESS)
			.position(-1)
			.build();

		when(couponIssueStatusService.isCouponIssued(any(), any())).thenReturn(couponApplyResponse);

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
