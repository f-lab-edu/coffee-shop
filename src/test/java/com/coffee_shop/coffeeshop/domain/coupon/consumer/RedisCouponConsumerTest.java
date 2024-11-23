package com.coffee_shop.coffeeshop.domain.coupon.consumer;

import static com.coffee_shop.coffeeshop.domain.coupon.CouponType.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.coffee_shop.coffeeshop.domain.coupon.Coupon;
import com.coffee_shop.coffeeshop.domain.coupon.CouponTransactionHistory;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponIssueRepository;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponRepository;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponTransactionHistoryRepository;
import com.coffee_shop.coffeeshop.domain.user.User;
import com.coffee_shop.coffeeshop.domain.user.UserRepository;
import com.coffee_shop.coffeeshop.service.IntegrationTestSupport;
import com.coffee_shop.coffeeshop.service.coupon.apply.RedisCouponApplyService;
import com.coffee_shop.coffeeshop.service.coupon.dto.request.CouponApplication;

class RedisCouponConsumerTest extends IntegrationTestSupport {
	@Autowired
	private RedisCouponApplyService redisCouponApplyService;

	@Autowired
	private CouponTransactionHistoryRepository couponTransactionHistoryRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CouponRepository couponRepository;

	@Autowired
	private CouponIssueRepository couponIssueRepository;

	@Autowired
	private RedisTemplate<String, Long> redisTemplate;

	@AfterEach
	void tearDown() {
		couponTransactionHistoryRepository.deleteAllInBatch();
		couponRepository.deleteAllInBatch();
		userRepository.deleteAllInBatch();
		clearAll();
	}

	@DisplayName("한명의 고객에게 쿠폰을 발급한다.")
	@Test
	void issueCoupon() throws InterruptedException {
		//given
		Coupon coupon = createCoupon(10, 0);
		User user = createUser();

		//when
		couponIssueRepository.add(CouponApplication.of(user, coupon), 1731488205);

		//then
		Thread.sleep(1000);

		assertThat(couponTransactionHistoryRepository.findAll()).hasSize(1);
		assertTrue(couponIssueRepository.isEmpty());
	}

	@DisplayName("쿠폰을 여러명에게 발급한다.")
	@Test
	public void issueCouponsToMultipleUsers() throws InterruptedException {
		//given
		int maxIssueCount = 20;
		Coupon coupon = createCoupon(maxIssueCount, 0);

		//20명 유저 생성
		Queue<User> users = new ConcurrentLinkedDeque<>();
		for (int i = 0; i < maxIssueCount; i++) {
			User user = createUser();
			users.add(user);
		}

		//when
		ExecutorService executorService = Executors.newFixedThreadPool(32);
		CountDownLatch latch = new CountDownLatch(maxIssueCount);

		for (int i = 0; i < maxIssueCount; i++) {
			executorService.submit(() -> {
				try {
					couponIssueRepository.add(CouponApplication.of(users.remove(), coupon), 1731488205);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();

		//then
		Thread.sleep(1000);

		assertThat(couponTransactionHistoryRepository.findAll()).hasSize(maxIssueCount);
		assertTrue(couponIssueRepository.isEmpty());
	}

	@DisplayName("쿠폰을 여러명에게 순차적으로 발급한다.")
	@Test
	public void issueCouponsSequentiallyToMultipleUsers() throws InterruptedException {
		//given
		Coupon coupon = createCoupon(10, 0);
		User user1 = createUser();
		User user2 = createUser();
		User user3 = createUser();

		CouponApplication couponApplication1 = CouponApplication.of(user1, coupon);
		CouponApplication couponApplication2 = CouponApplication.of(user2, coupon);
		CouponApplication couponApplication3 = CouponApplication.of(user3, coupon);

		//when
		couponIssueRepository.add(couponApplication1, 1731488205);
		couponIssueRepository.add(couponApplication2, 1731488206);
		couponIssueRepository.add(couponApplication3, 1731488207);

		//then
		Thread.sleep(3000);
		assertThat(couponTransactionHistoryRepository.findAll()).hasSize(3);
		CouponTransactionHistory couponTransactionHistory1 = couponTransactionHistoryRepository.findByCouponAndUser(
			coupon, user1).get();
		CouponTransactionHistory couponTransactionHistory2 = couponTransactionHistoryRepository.findByCouponAndUser(
			coupon, user2).get();
		CouponTransactionHistory couponTransactionHistory3 = couponTransactionHistoryRepository.findByCouponAndUser(
			coupon, user3).get();

		assertThat(couponTransactionHistory2.getIssueDateTime()).isAfter(couponTransactionHistory1.getIssueDateTime());
		assertThat(couponTransactionHistory3.getIssueDateTime()).isAfter(couponTransactionHistory2.getIssueDateTime());
		assertTrue(couponIssueRepository.isEmpty());
	}

	private Coupon createCoupon(int maxIssueCount, int issuedCount) {
		Coupon coupon = Coupon.builder()
			.name("오픈기념 선착순 할인 쿠폰")
			.type(AMOUNT)
			.discountAmount(1000)
			.minOrderAmount(4000)
			.maxIssueCount(maxIssueCount)
			.issuedCount(issuedCount)
			.build();
		return couponRepository.save(coupon);
	}

	private User createUser() {
		User user = User.builder()
			.name("우경서")
			.build();
		return userRepository.save(user);
	}

	private void clearAll() {
		Set<String> keys = redisTemplate.keys("*");
		if (keys != null && !keys.isEmpty()) {
			redisTemplate.delete(keys);
		}
	}
}