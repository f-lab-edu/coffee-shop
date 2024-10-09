package com.coffee_shop.coffeeshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class IndexController {
	@GetMapping("/")
	public String index() {
		return "index";
	}

	@GetMapping("/favicon.ico")
	@ResponseBody
	public void returnNoFavicon() {
	}
}

