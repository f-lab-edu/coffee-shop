package com.coffee_shop.coffeeshop.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "내부 서버 오류가 발생했습니다."),

	METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", "허용되지 않는 메서드입니다."),

	INVALID_JSON_FORMAT(HttpStatus.BAD_REQUEST, "INVALID_JSON_FORMAT", "JSON 형식이 잘못되었습니다."),
	INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "INVALID_INPUT_VALUE", "적절하지 않은 요청 값입니다."),
	INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "INVALID_TYPE_VALUE", "요청 값의 타입이 잘못되었습니다."),
	MISSING_REQUEST_PARAM(HttpStatus.BAD_REQUEST, "MISSING_REQUEST_PARAM", "요청 파라미터를 누락하였습니다."),

	ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "ENTITY_NOT_FOUND", "해당 데이터를 찾을 수 없습니다."),

	COUPON_TYPE_NOT_FOUND(HttpStatus.BAD_REQUEST, "COUPON_TYPE_NOT_FOUND", "유효하지 않는 쿠폰 타입입니다."),
	INVALID_DISCOUNT_PERCENTAGE(HttpStatus.BAD_REQUEST, "INVALID_DISCOUNT_PERCENTAGE", "할인율은 1~100까지 입력가능합니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
