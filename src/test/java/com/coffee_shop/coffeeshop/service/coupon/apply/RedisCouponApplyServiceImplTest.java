package com.coffee_shop.coffeeshop.service.coupon.apply;

import static com.coffee_shop.coffeeshop.domain.coupon.CouponType.*;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;

import com.coffee_shop.coffeeshop.common.exception.BusinessException;
import com.coffee_shop.coffeeshop.domain.coupon.Coupon;
import com.coffee_shop.coffeeshop.domain.coupon.CouponTransactionHistory;
import com.coffee_shop.coffeeshop.domain.coupon.consumer.CouponConsumer;
import com.coffee_shop.coffeeshop.domain.coupon.repository.AppliedUserRepository;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponIssueCountRepository;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponIssueRepository;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponRepository;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponTransactionHistoryRepository;
import com.coffee_shop.coffeeshop.domain.user.User;
import com.coffee_shop.coffeeshop.domain.user.UserRepository;
import com.coffee_shop.coffeeshop.service.IntegrationTestSupport;
import com.coffee_shop.coffeeshop.service.coupon.dto.request.CouponApplyServiceRequest;

class RedisCouponApplyServiceImplTest extends IntegrationTestSupport {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CouponRepository couponRepository;

	@Autowired
	private CouponTransactionHistoryRepository couponTransactionHistoryRepository;

	@Autowired
	private CouponIssueRepository couponIssueRepository;

	@Autowired
	private CouponIssueCountRepository couponIssueCountRepository;

	@Autowired
	private AppliedUserRepository appliedUserRepository;

	@Autowired
	private RedisTemplate<String, Long> redisTemplate;

	@Autowired
	private CouponApplyService redisCouponApplyServiceImpl;

	@MockBean
	private CouponConsumer couponMessageQConsumer;

	@AfterEach
	void tearDown() {
		couponTransactionHistoryRepository.deleteAllInBatch();
		couponRepository.deleteAllInBatch();
		userRepository.deleteAllInBatch();
		clearAll();
	}

	@DisplayName("고객이 쿠폰 발급 신청할경우 sorted set에 쌓인다.")
	@Test
	void applyCoupon() {
		//given
		Coupon coupon = createCoupon(10, 0);
		User user = createUser();
		LocalDateTime issueDateTime = LocalDateTime.of(2024, 8, 30, 0, 0);

		//when
		redisCouponApplyServiceImpl.applyCoupon(createRequest(user.getId(), coupon.getId()));

		//then
		assertThat(couponIssueRepository.count()).isEqualTo(1);
	}

	@DisplayName("쿠폰을 여러명이 발급 신청할경우 sorted set에 신청 개수만큼 쌓인다.")
	@Test
	void applyCoupons() throws InterruptedException {
		//given
		int maxIssueCount = 1000;
		Coupon coupon = createCoupon(maxIssueCount, 0);
		LocalDateTime issueDateTime = LocalDateTime.of(2024, 8, 30, 0, 0);

		//1000명 유저 생성
		Queue<Long> users = new ConcurrentLinkedDeque<>();
		for (int i = 0; i < maxIssueCount; i++) {
			User user = createUser();
			users.add(user.getId());
		}

		//when
		ExecutorService executorService = Executors.newFixedThreadPool(32);
		CountDownLatch latch = new CountDownLatch(maxIssueCount);

		for (int i = 0; i < maxIssueCount; i++) {
			executorService.submit(() -> {
				try {
					redisCouponApplyServiceImpl.applyCoupon(createRequest(users.remove(), coupon.getId()));
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();

		//then
		assertThat(couponIssueRepository.count()).isEqualTo(maxIssueCount);
	}

	@DisplayName("선착순 쿠폰 수량이 소진된 경우 쿠폰 발급 신청시 발급이 불가능하다.")
	@Test
	public void applyCouponWhenCouponLimitReached() {
		//given
		Coupon coupon = createCoupon(1, 0);
		User user = createUser();
		LocalDateTime issueDateTime = LocalDateTime.of(2024, 8, 30, 0, 0);

		couponIssueCountRepository.increment(coupon.getId());

		//when, then
		assertThatThrownBy(
			() -> redisCouponApplyServiceImpl.applyCoupon(createRequest(user.getId(), coupon.getId())))
			.isInstanceOf(BusinessException.class)
			.hasMessage("쿠폰이 모두 소진되어 발급할 수 없습니다.");
	}

	@DisplayName("한명의 사용자에게 동일한 쿠폰을 중복으로 발급할 수 없다.")
	@Test
	public void applyCouponToUniqueUser() {
		//given
		Coupon coupon = createCoupon(10, 0);
		User user = createUser();
		LocalDateTime issueDateTime = LocalDateTime.of(2024, 8, 30, 0, 0);

		appliedUserRepository.add(user.getId());

		//when, then
		assertThatThrownBy(
			() -> redisCouponApplyServiceImpl.applyCoupon(createRequest(user.getId(), coupon.getId())))
			.isInstanceOf(BusinessException.class)
			.hasMessage("이미 발급된 쿠폰입니다. 사용자 ID = " + user.getId() + ", 사용자 이름 = " + user.getName());
	}

	private CouponApplyServiceRequest createRequest(Long userId, Long couponId) {
		return CouponApplyServiceRequest.builder()
			.userId(userId)
			.couponId(couponId)
			.build();
	}

	private CouponTransactionHistory createCouponTransactionHistory(Coupon coupon, User user,
		LocalDateTime issueDateTime) {
		CouponTransactionHistory history = CouponTransactionHistory.builder()
			.user(user)
			.coupon(coupon)
			.issueDateTime(issueDateTime)
			.build();

		return couponTransactionHistoryRepository.save(history);
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
