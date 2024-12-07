package com.coffee_shop.coffeeshop.controller;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import com.coffee_shop.coffeeshop.controller.cart.CartController;
import com.coffee_shop.coffeeshop.controller.coupon.CouponApplyController;
import com.coffee_shop.coffeeshop.controller.coupon.CouponController;
import com.coffee_shop.coffeeshop.controller.item.ItemController;
import com.coffee_shop.coffeeshop.service.cart.CartService;
import com.coffee_shop.coffeeshop.service.coupon.CouponService;
import com.coffee_shop.coffeeshop.service.coupon.apply.CouponApplyService;
import com.coffee_shop.coffeeshop.service.item.ItemService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = {
	CartController.class,
	CouponApplyController.class,
	CouponController.class,
	ItemController.class
})
@ExtendWith(RestDocumentationExtension.class)
public abstract class RestDocsSupport {

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	protected ObjectMapper objectMapper;

	@MockBean
	protected CartService cartService;

	@MockBean
	protected CouponService couponService;

	@MockBean
	protected ItemService itemService;

	@MockBean
	protected CouponApplyService couponApplyService;

	@BeforeEach
	void setUp(WebApplicationContext webApplicationContext,
		RestDocumentationContextProvider restDocumentationContextProvider) {
		this.mockMvc = MockMvcBuilders
			.webAppContextSetup(webApplicationContext)
			.addFilters(new CharacterEncodingFilter("UTF-8", true))
			.apply(documentationConfiguration(restDocumentationContextProvider))
			.build();
	}
}
