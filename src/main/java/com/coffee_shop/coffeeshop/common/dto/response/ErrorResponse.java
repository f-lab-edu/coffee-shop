package com.coffee_shop.coffeeshop.common.dto.response;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolationException;

import org.springframework.validation.BindingResult;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.coffee_shop.coffeeshop.exception.ErrorCode;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ErrorResponse extends CommonResponse {
	private List<FieldError> fieldErrors;

	private ErrorResponse(ErrorCode errorCode, List<FieldError> fieldErrors) {
		super(errorCode.getCode(), errorCode.getMessage());
		this.fieldErrors = fieldErrors;
	}

	private ErrorResponse(ErrorCode errorCode) {
		super(errorCode.getCode(), errorCode.getMessage());
		this.fieldErrors = new ArrayList<>();
	}

	public static ErrorResponse of(ErrorCode errorCode) {
		return new ErrorResponse(errorCode);

	}

	public static ErrorResponse of(ErrorCode errorCode, BindingResult bindingResult) {
		return new ErrorResponse(errorCode, FieldError.of(bindingResult));
	}

	public static ErrorResponse of(MethodArgumentTypeMismatchException exception) {
		List<FieldError> errors = FieldError.of(
			exception.getName(), ErrorCode.INVALID_TYPE_VALUE.getMessage());
		return new ErrorResponse(ErrorCode.INVALID_TYPE_VALUE, errors);
	}

	public static ErrorResponse of(ErrorCode errorCode, ConstraintViolationException exception) {
		return new ErrorResponse(errorCode, FieldError.of(exception));
	}

	@Getter
	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	static class FieldError {
		private String field;
		private String message;

		private FieldError(String field, String message) {
			this.field = field;
			this.message = message;
		}

		private static List<FieldError> of(ConstraintViolationException exception) {
			return exception.getConstraintViolations()
				.stream()
				.map(error -> new FieldError(
						error.getPropertyPath().toString(),
						error.getMessage()
					)
				)
				.collect(Collectors.toList());
		}

		private static List<FieldError> of(BindingResult bindingResult) {
			return bindingResult.getFieldErrors()
				.stream()
				.map(error -> new FieldError(
						error.getField(),
						error.getDefaultMessage()
					)
				)
				.collect(Collectors.toList());
		}

		public static List<FieldError> of(String field, String message) {
			List<FieldError> fieldErrors = new ArrayList<>();
			fieldErrors.add(new FieldError(field, message));
			return fieldErrors;
		}
	}
}

