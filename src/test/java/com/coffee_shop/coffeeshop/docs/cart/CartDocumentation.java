package com.coffee_shop.coffeeshop.docs.cart;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;

import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.payload.JsonFieldType;

public class CartDocumentation {
	public static RestDocumentationResultHandler updateCart() {
		return document("cart/update",
			preprocessRequest(prettyPrint()),
			preprocessResponse(prettyPrint()),
			pathParameters(
				parameterWithName("user").description("사용자 ID")
			),
			requestFields(
				fieldWithPath("itemId").type(JsonFieldType.NUMBER).description("상품 ID"),
				fieldWithPath("count").type(JsonFieldType.NUMBER).description("상품 수량")
			),
			responseFields(
				fieldWithPath("code").type(JsonFieldType.STRING)
					.description("코드"),
				fieldWithPath("message").type(JsonFieldType.STRING)
					.description("메시지"),
				fieldWithPath("data.itemId").type(JsonFieldType.NUMBER)
					.description("상품 ID"),
				fieldWithPath("data.itemName").type(JsonFieldType.STRING)
					.description("상품명"),
				fieldWithPath("data.itemPrice").type(JsonFieldType.NUMBER)
					.description("상품가격"),
				fieldWithPath("data.itemCount").type(JsonFieldType.NUMBER)
					.description("상품수량")
			)
		);
	}

	public static RestDocumentationResultHandler deleteCart() {
		return document("cart/delete",
			preprocessRequest(prettyPrint()),
			preprocessResponse(prettyPrint()),
			pathParameters(
				parameterWithName("user").description("사용자 ID")
			),
			requestFields(
				fieldWithPath("itemId").type(JsonFieldType.NUMBER).description("상품 ID")
			),
			responseFields(
				fieldWithPath("code").type(JsonFieldType.STRING)
					.description("코드"),
				fieldWithPath("message").type(JsonFieldType.STRING)
					.description("메시지"),
				fieldWithPath("data").type(JsonFieldType.NULL)
					.description("응답 데이터")
			));
	}

	public static RestDocumentationResultHandler findCarts() {
		return document("cart/findAll",
			preprocessRequest(prettyPrint()),
			preprocessResponse(prettyPrint()),
			pathParameters(
				parameterWithName("user").description("사용자 ID")
			),
			responseFields(
				fieldWithPath("code").type(JsonFieldType.STRING)
					.description("코드"),
				fieldWithPath("message").type(JsonFieldType.STRING)
					.description("메시지"),
				fieldWithPath("data").type(JsonFieldType.ARRAY)
					.description("응답 데이터"),
				fieldWithPath("data.[].itemId").type(JsonFieldType.NUMBER)
					.description("상품 ID"),
				fieldWithPath("data.[].itemName").type(JsonFieldType.STRING)
					.description("상품명"),
				fieldWithPath("data.[].itemPrice").type(JsonFieldType.NUMBER)
					.description("상품가격"),
				fieldWithPath("data.[].itemCount").type(JsonFieldType.NUMBER)
					.description("상품수량")
			));
	}
}
