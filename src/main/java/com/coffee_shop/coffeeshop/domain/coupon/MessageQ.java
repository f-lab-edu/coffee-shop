package com.coffee_shop.coffeeshop.domain.coupon;

import java.util.ArrayList;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.springframework.stereotype.Component;

import com.coffee_shop.coffeeshop.service.coupon.dto.request.CouponApplication;

@Component
public class MessageQ {

	private final Deque<CouponApplication> queue;

	public MessageQ() {
		this.queue = new ConcurrentLinkedDeque<>();
	}

	public void addMessage(CouponApplication message) {
		queue.add(message);
	}

	public CouponApplication takeMessage() {
		return queue.removeFirst();
	}

	public boolean isEmpty() {
		return queue.isEmpty();
	}

	public int size() {
		return queue.size();
	}

	public void addFirst(CouponApplication couponApplication) {
		queue.addFirst(couponApplication);
	}

	public ArrayList<CouponApplication> toArrayList() {
		return new ArrayList<>(queue);
	}
}
