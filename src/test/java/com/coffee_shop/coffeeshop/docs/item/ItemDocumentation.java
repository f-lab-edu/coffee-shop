package com.coffee_shop.coffeeshop.docs.item;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.*;

import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.payload.JsonFieldType;

public class ItemDocumentation {
	public static RestDocumentationResultHandler createItem() {
		return document("item/create",
			preprocessRequest(prettyPrint()),
			preprocessResponse(prettyPrint()),
			requestFields(
				fieldWithPath("name").type(JsonFieldType.STRING).description("상품명")
					.attributes(key("constraints").value("1~100자")),
				fieldWithPath("itemType").type(JsonFieldType.STRING).description("상품타입")
					.attributes(key("constraints").value("COFFEE(커피) / DESSERT(디저트) / NON_COFFEE(기타 음료)")),
				fieldWithPath("price").type(JsonFieldType.NUMBER).description("상품가격")
					.attributes(key("constraints").value("최소금액 : 100원"))
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
				headerWithName("Location").description("생성된 상품 ID")
			)
		);
	}

	public static RestDocumentationResultHandler updateItem() {
		return document("item/update",
			preprocessRequest(prettyPrint()),
			preprocessResponse(prettyPrint()),
			pathParameters(
				parameterWithName("itemId").description("상품 ID")
			),
			requestFields(
				fieldWithPath("name").type(JsonFieldType.STRING).description("상품명")
					.attributes(key("constraints").value("1~100자")),
				fieldWithPath("itemType").type(JsonFieldType.STRING).description("상품타입")
					.attributes(key("constraints").value("COFFEE(커피) / DESSERT(디저트) / NON_COFFEE(기타 음료)")),
				fieldWithPath("price").type(JsonFieldType.NUMBER).description("상품가격")
					.attributes(key("constraints").value("최소금액 : 100원"))
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

	public static RestDocumentationResultHandler deleteItem() {
		return document("item/delete",
			preprocessRequest(prettyPrint()),
			preprocessResponse(prettyPrint()),
			pathParameters(
				parameterWithName("itemId").description("상품 ID")
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

	public static RestDocumentationResultHandler findItem() {
		return document("item/find",
			preprocessRequest(prettyPrint()),
			preprocessResponse(prettyPrint()),
			pathParameters(
				parameterWithName("itemId").description("상품 ID")
			),
			responseFields(
				fieldWithPath("code").type(JsonFieldType.STRING)
					.description("코드"),
				fieldWithPath("message").type(JsonFieldType.STRING)
					.description("메시지"),
				fieldWithPath("data").type(JsonFieldType.OBJECT)
					.description("응답 데이터"),
				fieldWithPath("data.id").type(JsonFieldType.NUMBER)
					.description("상품 ID"),
				fieldWithPath("data.name").type(JsonFieldType.STRING)
					.description("상품명"),
				fieldWithPath("data.itemType").type(JsonFieldType.STRING)
					.description("상품타입"),
				fieldWithPath("data.price").type(JsonFieldType.NUMBER)
					.description("상품가격"),
				fieldWithPath("data.lastModifiedDateTime").type(JsonFieldType.STRING)
					.description("최근 수정일")
			));
	}

	public static RestDocumentationResultHandler findItems() {
		return document("item/findAll",
			preprocessRequest(prettyPrint()),
			preprocessResponse(prettyPrint()),
			queryParameters(
				parameterWithName("page").description("페이지 번호"),
				parameterWithName("size").description("한 페이지 당 데이터 수"),
				parameterWithName("name").description("상품명"),
				parameterWithName("itemType").description("상품타입")
			),
			responseFields(
				fieldWithPath("code").type(JsonFieldType.STRING)
					.description("코드"),
				fieldWithPath("message").type(JsonFieldType.STRING)
					.description("메시지"),
				fieldWithPath("data").type(JsonFieldType.ARRAY)
					.description("응답 데이터"),
				fieldWithPath("data.[].id").type(JsonFieldType.NUMBER)
					.description("상품 ID"),
				fieldWithPath("data.[].name").type(JsonFieldType.STRING)
					.description("상품명"),
				fieldWithPath("data.[].itemType").type(JsonFieldType.STRING)
					.description("상품타입"),
				fieldWithPath("data.[].price").type(JsonFieldType.NUMBER)
					.description("상품가격"),
				fieldWithPath("data.[].lastModifiedDateTime").type(JsonFieldType.STRING)
					.description("최근 수정일")
			));
	}
}
