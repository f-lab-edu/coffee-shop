package com.coffee_shop.coffeeshop.common.dto.response;

import org.springframework.http.HttpStatus;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApiResponse<T> extends CommonResponse {
	private T data;

	private ApiResponse(String code, String message, T data) {
		super(code, message);
		this.data = data;
	}

	public static <T> ApiResponse<T> of(String code, String message, T data) {
		return new ApiResponse<>(code, message, data);
	}

	public static <T> ApiResponse<T> of(String code, T data) {
		return of(code, code, data);
	}

	public static <T> ApiResponse<T> ok(T data) {
		return of(HttpStatus.OK.name(), data);
	}

	public static <T> ApiResponse<T> noContent() {
		return of(HttpStatus.OK.name(), null);
	}

	public static <T> ApiResponse<T> created() {
		return of(HttpStatus.CREATED.name(), null);
	}

}

