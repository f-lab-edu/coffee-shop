package com.coffee_shop.coffeeshop.domain.coupon.consumer;

import static com.coffee_shop.coffeeshop.domain.coupon.CouponType.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.coffee_shop.coffeeshop.domain.coupon.Coupon;
import com.coffee_shop.coffeeshop.domain.coupon.CouponRepository;
import com.coffee_shop.coffeeshop.domain.coupon.CouponTransactionHistoryRepository;
import com.coffee_shop.coffeeshop.domain.coupon.MessageQ;
import com.coffee_shop.coffeeshop.domain.user.User;
import com.coffee_shop.coffeeshop.domain.user.UserRepository;
import com.coffee_shop.coffeeshop.service.IntegrationTestSupport;
import com.coffee_shop.coffeeshop.service.coupon.CouponApplyService;
import com.coffee_shop.coffeeshop.service.coupon.dto.request.CouponApplyServiceRequest;

class CouponMessageQConsumerTest extends IntegrationTestSupport {
	@Autowired
	private CouponApplyService couponApplyService;

	@Autowired
	private CouponTransactionHistoryRepository couponTransactionHistoryRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CouponRepository couponRepository;

	@Autowired
	private MessageQ messageQ;

	@AfterEach
	void tearDown() {
		couponTransactionHistoryRepository.deleteAllInBatch();
		couponRepository.deleteAllInBatch();
		userRepository.deleteAllInBatch();
	}

	@DisplayName("한명의 고객에게 쿠폰을 발급한다.")
	@Test
	void issueCoupon() throws InterruptedException {
		//given
		Coupon coupon = couponRepository.save(createCoupon(10, 0));
		User user = userRepository.save(createUser());
		LocalDateTime issueDateTime = LocalDateTime.of(2024, 8, 30, 0, 0);

		//when
		couponApplyService.applyCoupon(createRequest(user.getId(), coupon.getId()), issueDateTime);

		//then
		Thread.sleep(1000);

		assertThat(couponTransactionHistoryRepository.findAll()).hasSize(1);

		List<Coupon> coupons = couponRepository.findAll();
		assertThat(coupons.get(0).getIssuedCount()).isEqualTo(1);

		assertTrue(messageQ.isEmpty());
	}

	@DisplayName("쿠폰을 여러명에게 발급한다.")
	@Test
	public void issueCouponsToMultipleUsers() throws InterruptedException {
		//given
		int maxIssueCount = 1000;
		Coupon coupon = couponRepository.save(createCoupon(maxIssueCount, 0));

		LocalDateTime issueDateTime = LocalDateTime.of(2024, 8, 30, 0, 0);

		//1000명 유저 생성
		Queue<Long> users = new ConcurrentLinkedDeque<>();
		for (int i = 0; i < maxIssueCount; i++) {
			User user = userRepository.save(createUser());
			users.add(user.getId());
		}

		//when
		ExecutorService executorService = Executors.newFixedThreadPool(32);
		CountDownLatch latch = new CountDownLatch(maxIssueCount);

		for (int i = 0; i < maxIssueCount; i++) {
			executorService.submit(() -> {
				try {
					couponApplyService.applyCoupon(createRequest(users.remove(), coupon.getId()), issueDateTime);
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();

		//then
		Thread.sleep(4000);

		assertThat(couponTransactionHistoryRepository.findAll()).hasSize(maxIssueCount);

		List<Coupon> coupons = couponRepository.findAll();
		assertThat(coupons.get(0).getIssuedCount()).isEqualTo(maxIssueCount);

		assertTrue(messageQ.isEmpty());
	}

	private CouponApplyServiceRequest createRequest(Long userId, Long couponId) {
		return CouponApplyServiceRequest.builder()
			.userId(userId)
			.couponId(couponId)
			.build();
	}

	private Coupon createCoupon(int maxIssueCount, int issuedCount) {
		return Coupon.builder()
			.name("오픈기념 선착순 할인 쿠폰")
			.type(AMOUNT)
			.discountAmount(1000)
			.minOrderAmount(4000)
			.maxIssueCount(maxIssueCount)
			.issuedCount(issuedCount)
			.build();
	}

	private User createUser() {
		return User.builder()
			.name("우경서")
			.build();
	}
}
