package com.coffee_shop.coffeeshop.domain.coupon;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.stereotype.Component;

import com.coffee_shop.coffeeshop.service.coupon.dto.request.CouponApplication;

@Component
public class MessageQ {

	private final Queue<CouponApplication> queue;

	public MessageQ() {
		this.queue = new ConcurrentLinkedQueue<>();
	}

	public void addMessage(CouponApplication message) {
		queue.offer(message);
	}

	public CouponApplication takeMessage() {
		return queue.poll();
	}

	public boolean isEmpty() {
		return queue.isEmpty();
	}

	public int size() {
		return queue.size();
	}
}
