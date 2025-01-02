package com.coffee_shop.coffeeshop.service.coupon;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.coffee_shop.coffeeshop.domain.coupon.Coupon;
import com.coffee_shop.coffeeshop.domain.coupon.CouponIssueFailHistory;
import com.coffee_shop.coffeeshop.domain.coupon.CouponIssueStatus;
import com.coffee_shop.coffeeshop.domain.coupon.CouponTransactionHistory;
import com.coffee_shop.coffeeshop.domain.coupon.producer.CouponProducer;
import com.coffee_shop.coffeeshop.domain.coupon.producer.RedisCouponProducer;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponIssueFailHistoryRepository;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponIssueRepository;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponRepository;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponTransactionHistoryRepository;
import com.coffee_shop.coffeeshop.domain.user.User;
import com.coffee_shop.coffeeshop.domain.user.UserRepository;
import com.coffee_shop.coffeeshop.service.IntegrationTestSupport;
import com.coffee_shop.coffeeshop.service.coupon.dto.request.CouponApplication;
import com.coffee_shop.coffeeshop.service.coupon.dto.response.CouponApplyResponse;

@ActiveProfiles("messageQ")
class RedisCouponIssueStatusServiceTest extends IntegrationTestSupport {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CouponRepository couponRepository;

	@Autowired
	private CouponTransactionHistoryRepository couponTransactionHistoryRepository;

	@Autowired
	private CouponIssueFailHistoryRepository couponIssueFailHistoryRepository;

	@Autowired
	private CouponIssueRepository couponIssueRepository;

	@Autowired
	private RedisTemplate<String, Long> redisTemplate;

	private CouponIssueStatusService couponIssueStatusService;

	private CouponProducer couponProducer;

	@BeforeEach
	void setUp() {
		couponProducer = new RedisCouponProducer(couponIssueRepository);
		couponIssueStatusService = new CouponIssueStatusService(userRepository, couponRepository, couponProducer,
			couponTransactionHistoryRepository, couponIssueFailHistoryRepository);
	}

	@AfterEach
	void tearDown() {
		couponTransactionHistoryRepository.deleteAllInBatch();
		couponIssueFailHistoryRepository.deleteAllInBatch();
		couponRepository.deleteAllInBatch();
		userRepository.deleteAllInBatch();
		clearAll();
	}

	@DisplayName("쿠폰 발급이 완료된다면 발급 결과 조회시 발급 결과는 성공, 대기 순번은 -1로 반환된다.")
	@Test
	void findPositionWhenIssueCouponSuccessfully() {
		//given
		LocalDateTime issueDateTime = LocalDateTime.of(2024, 8, 30, 0, 0);
		Coupon coupon = createCoupon(10, 0);
		User user = createUser();

		createCouponTransactionHistory(coupon, user, issueDateTime);

		//when
		CouponApplyResponse response = couponIssueStatusService.isCouponIssued(user.getId(), coupon.getId());

		//then
		assertThat(response.getCouponIssueStatus()).isEqualTo(CouponIssueStatus.SUCCESS);
		assertThat(response.getPosition()).isEqualTo(-1);
	}

	@DisplayName("쿠폰 발급 실패한다면 발급 결과 조회시 발급 결과는 실패, 대기 순번은 -1로 반환된다.")
	@Test
	void findPositionWhenFailToIssueCoupon() {
		//given
		Coupon coupon = createCoupon(10, 0);
		User user = createUser();

		CouponIssueFailHistory couponIssueFailHistory = CouponIssueFailHistory.of(user, coupon, LocalDateTime.now());
		couponIssueFailHistoryRepository.save(couponIssueFailHistory);

		//when
		CouponApplyResponse response = couponIssueStatusService.isCouponIssued(user.getId(), coupon.getId());

		//then
		assertThat(response.getCouponIssueStatus()).isEqualTo(CouponIssueStatus.FAILURE);
		assertThat(response.getPosition()).isEqualTo(-1);
	}

	@DisplayName("쿠폰 발급중이라면 발급 결과 조회시 발급 결과는 발급중, 현재 대기열 순번이 반환된다.")
	@Test
	void findPositionWhenCouponIsBeingIssued() throws InterruptedException {
		//given
		int maxIssueCount = 5;
		Coupon coupon = createCoupon(10, 0);

		ExecutorService executorService = Executors.newFixedThreadPool(32);
		CountDownLatch latch = new CountDownLatch(maxIssueCount);

		int expectedPosition = 3;
		Long expectedUserId = null;
		Queue<User> users = new ConcurrentLinkedDeque<>();

		for (int i = 0; i < maxIssueCount; i++) {
			User user = createUser();
			users.add(user);
			if (i == expectedPosition - 1) {
				expectedUserId = user.getId();
			}
		}

		for (int i = 0; i < maxIssueCount; i++) {
			executorService.submit(() -> {
				try {
					couponIssueRepository.add(CouponApplication.of(users.remove(), coupon), System.currentTimeMillis());
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					latch.countDown();
				}

			});

			Thread.sleep(500);
		}

		latch.await();

		//when
		CouponApplyResponse response = couponIssueStatusService.isCouponIssued(expectedUserId, coupon.getId());

		//then
		assertThat(couponIssueRepository.count()).isEqualTo(maxIssueCount);
		assertThat(response.getCouponIssueStatus()).isEqualTo(CouponIssueStatus.IN_PROGRESS);
		assertThat(response.getPosition()).isEqualTo(expectedPosition);
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

	private CouponTransactionHistory createCouponTransactionHistory(Coupon coupon, User user,
		LocalDateTime issueDateTime) {
		CouponTransactionHistory history = CouponTransactionHistory.builder()
			.user(user)
			.coupon(coupon)
			.issueDateTime(issueDateTime)
			.build();

		return couponTransactionHistoryRepository.save(history);
	}
}