package com.coffee_shop.coffeeshop.controller.order;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class OrderController {
	@GetMapping("/favicon.ico")
	@ResponseBody
	void returnNoFavicon() {
	}
}
