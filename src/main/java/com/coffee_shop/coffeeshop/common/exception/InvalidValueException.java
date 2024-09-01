package com.coffee_shop.coffeeshop.common.exception;

import com.coffee_shop.coffeeshop.exception.ErrorCode;

public class InvalidValueException extends BusinessException {
	public InvalidValueException(ErrorCode errorCode) {
		super(errorCode);
	}
}
