package com.coffee_shop.coffeeshop.controller.redisq;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coffee_shop.coffeeshop.controller.redisq.dto.RedisQRequest;
import com.coffee_shop.coffeeshop.controller.redisq.dto.RedisQResponse;
import com.coffee_shop.coffeeshop.service.redisq.RedisQService;

@RestController
@RequestMapping("/api/queue")
public class RedisQController {

	@Autowired
	private RedisQService redisQService;

	@PostMapping("/enqueue")
	public String enqueue(@RequestBody @Valid RedisQRequest request) {
		redisQService.enqueue(request.getUserId());
		return "Data enqueued: " + request.getUserId();
	}

	@GetMapping("/dequeue")
	public RedisQResponse dequeue() {
		Long userId = redisQService.dequeue();
		return new RedisQResponse(userId);
	}
}
