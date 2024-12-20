package com.coffee_shop.coffeeshop.common.exception;

import com.coffee_shop.coffeeshop.exception.ErrorCode;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
	private final ErrorCode errorCode;

	public BusinessException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

	public BusinessException(ErrorCode errorCode, String errorMessage) {
		super((errorCode.getMessage() + " " + errorMessage));
		this.errorCode = errorCode;
	}
}
