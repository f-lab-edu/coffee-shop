package com.coffee_shop.coffeeshop.docs.coupon;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.*;

import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultHandler;

public class CouponDocumentation {
	public static RestDocumentationResultHandler createCoupon() {
		return document("coupon/create",
			preprocessRequest(prettyPrint()),
			preprocessResponse(prettyPrint()),
			requestFields(
				fieldWithPath("name").type(JsonFieldType.STRING).description("쿠폰명")
					.attributes(key("constraints").value("1~30자")),
				fieldWithPath("type").type(JsonFieldType.STRING).description("쿠폰 타입")
					.attributes(key("constraints").value("PERCENTAGE(비율할인) / AMOUNT(금액할인)")),
				fieldWithPath("discountAmount").type(JsonFieldType.NUMBER).description("할인 금액")
					.attributes(key("constraints").value(
						"PERCENTAGE : 1~100 사이 수 / AMOUNT : 양수"
					)),
				fieldWithPath("minOrderAmount").type(JsonFieldType.NUMBER).description("주문 최소 금액")
					.optional()
					.attributes(
						key("constraints").value("0이상"),
						key("defaultValue").value("0 (주문 최소 금액 없음)")
					),
				fieldWithPath("maxIssueCount").type(JsonFieldType.NUMBER).description("최대 발급개수")
					.optional()
					.attributes(
						key("constraints").value("0이상, 0일 경우 무제한"),
						key("defaultValue").value("0 (무제한)")
					)
			),
			responseFields(
				fieldWithPath("code").type(JsonFieldType.STRING)
					.description("코드"),
				fieldWithPath("message").type(JsonFieldType.STRING)
					.description("메시지"),
				fieldWithPath("data").type(JsonFieldType.NULL)
					.description("응답 데이터")
			),
			responseHeaders(
				headerWithName("Location").description("생성된 쿠폰 URI")
			)
		);
	}

	public static RestDocumentationResultHandler applyCoupon() {
		return document("coupon/apply",
			preprocessRequest(prettyPrint()),
			preprocessResponse(prettyPrint()),
			requestFields(
				fieldWithPath("userId").type(JsonFieldType.NUMBER).description("사용자 ID")
					.attributes(key("constraints").value("양수")),
				fieldWithPath("couponId").type(JsonFieldType.NUMBER).description("쿠폰 ID")
					.attributes(key("constraints").value("양수"))
			),
			responseFields(
				fieldWithPath("code").type(JsonFieldType.STRING)
					.description("코드"),
				fieldWithPath("message").type(JsonFieldType.STRING)
					.description("메시지"),
				fieldWithPath("data").type(JsonFieldType.NULL)
					.description("응답 데이터")
			),
			responseHeaders(
				headerWithName("Location").description("쿠폰 발급 결과 URI")
			)
		);
	}

	public static ResultHandler isIssuedCoupon() {
		return document("coupon/issued",
			preprocessResponse(prettyPrint()),
			pathParameters(
				parameterWithName("userId").description("사용자 ID"),
				parameterWithName("couponId").description("쿠폰 ID")
			),
			responseFields(
				fieldWithPath("code").type(JsonFieldType.STRING)
					.description("코드"),
				fieldWithPath("message").type(JsonFieldType.STRING)
					.description("메시지"),
				fieldWithPath("data").type(JsonFieldType.OBJECT)
					.description("응답 데이터"),
				fieldWithPath("data.result").type(JsonFieldType.STRING)
					.description("쿠폰 발급 결과")
					.attributes(key("constraints").value("SUCCESS(발급완료) / FAIL(발급전)")),
				fieldWithPath("data.issuedDateTime").type(JsonFieldType.STRING)
					.description("발급일")
			));
	}
}
