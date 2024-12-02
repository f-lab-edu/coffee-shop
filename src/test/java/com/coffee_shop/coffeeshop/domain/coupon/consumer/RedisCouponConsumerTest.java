package com.coffee_shop.coffeeshop.domain.coupon.consumer;

import static com.coffee_shop.coffeeshop.domain.coupon.CouponType.*;
import static java.util.concurrent.TimeUnit.*;
import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.redis.core.RedisTemplate;

import com.coffee_shop.coffeeshop.domain.coupon.Coupon;
import com.coffee_shop.coffeeshop.domain.coupon.CouponTransactionHistory;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponIssueCountRepository;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponIssueRepository;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponRepository;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponTransactionHistoryRepository;
import com.coffee_shop.coffeeshop.domain.user.User;
import com.coffee_shop.coffeeshop.domain.user.UserRepository;
import com.coffee_shop.coffeeshop.service.IntegrationTestSupport;
import com.coffee_shop.coffeeshop.service.coupon.CouponService;
import com.coffee_shop.coffeeshop.service.coupon.dto.request.CouponApplication;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

class RedisCouponConsumerTest extends IntegrationTestSupport {
	@Autowired
	private CouponIssueCountRepository couponIssueCountRepository;

	@Autowired
	private CouponTransactionHistoryRepository couponTransactionHistoryRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CouponRepository couponRepository;

	@Autowired
	private CouponIssueRepository couponIssueRepository;

	@SpyBean
	private CouponService couponService;

	@Autowired
	private RedisTemplate<String, Long> redisTemplate;

	@AfterEach
	void tearDown() {
		couponTransactionHistoryRepository.deleteAllInBatch();
		couponRepository.deleteAllInBatch();
		userRepository.deleteAllInBatch();
		clearAll();
	}

	@DisplayName("쿠폰 최대 발급 개수를 초과해서 발급할 수 없다.")
	@Test
	void issueCouponWhenMaxCouponIssueCountExceeded() throws InterruptedException {
		//given
		Coupon coupon = createCoupon(1, 0);
		User user1 = createUser();
		User user2 = createUser();

		couponIssueRepository.add(CouponApplication.of(user1, coupon), 1731488205);
		couponIssueRepository.add(CouponApplication.of(user2, coupon), 1731488206);

		ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
		Logger logger = (Logger)LoggerFactory.getLogger(RedisCouponConsumer.class);
		logger.addAppender(listAppender);
		listAppender.start();

		//then
		await()
			.atMost(6, SECONDS)
			.untilAsserted(() -> {
				assertThat(couponTransactionHistoryRepository.findAll()).hasSize(1);
				assertTrue(couponIssueRepository.isEmpty());
				assertThat(couponIssueCountRepository.getIssueCount(coupon.getId())).isEqualTo(1);

				List<ILoggingEvent> testLogs = listAppender.list;
				assertThat(testLogs.size()).isEqualTo(1);
				assertThat(testLogs.get(0).getFormattedMessage()).isEqualTo(
					"쿠폰발급 실패 > 쿠폰이 모두 소진되어 발급할 수 없습니다.");
			});
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
		await()
			.atMost(4, SECONDS)
			.untilAsserted(() -> {
				assertThat(couponTransactionHistoryRepository.findAll()).hasSize(1);
				assertTrue(couponIssueRepository.isEmpty());
				assertThat(couponIssueCountRepository.getIssueCount(coupon.getId())).isEqualTo(1);
			});
	}

	@DisplayName("쿠폰을 여러명에게 발급한다.")
	@Test
	public void issueCouponsToMultipleUsers() throws InterruptedException {
		//given
		int maxIssueCount = 20;
		Coupon coupon = createCoupon(maxIssueCount, 0);

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
		await()
			.atMost(6, SECONDS)
			.untilAsserted(() -> {
				assertThat(couponTransactionHistoryRepository.findAll()).hasSize(maxIssueCount);
				assertTrue(couponIssueRepository.isEmpty());
				assertThat(couponIssueCountRepository.getIssueCount(coupon.getId())).isEqualTo(maxIssueCount);
			});
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
		await()
			.atMost(4, SECONDS)
			.untilAsserted(() -> {
				assertThat(couponTransactionHistoryRepository.findAll()).hasSize(3);
				CouponTransactionHistory couponTransactionHistory1 = couponTransactionHistoryRepository.findByCouponAndUser(
					coupon, user1).get();
				CouponTransactionHistory couponTransactionHistory2 = couponTransactionHistoryRepository.findByCouponAndUser(
					coupon, user2).get();
				CouponTransactionHistory couponTransactionHistory3 = couponTransactionHistoryRepository.findByCouponAndUser(
					coupon, user3).get();

				assertThat(couponTransactionHistory2.getIssueDateTime()).isAfter(
					couponTransactionHistory1.getIssueDateTime());
				assertThat(couponTransactionHistory3.getIssueDateTime()).isAfter(
					couponTransactionHistory2.getIssueDateTime());
				assertTrue(couponIssueRepository.isEmpty());
				assertThat(couponIssueCountRepository.getIssueCount(coupon.getId())).isEqualTo(3);
			});
	}

	@DisplayName("쿠폰개수를 동기화한다.")
	@Test
	public void shouldSyncCouponCount() throws InterruptedException {
		//given
		int maxIssueCount = 19;
		Coupon coupon = createCoupon(maxIssueCount, 0);

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
		await()
			.atMost(4, SECONDS)
			.untilAsserted(() -> {
				assertThat(couponTransactionHistoryRepository.findAll()).hasSize(maxIssueCount);
				assertTrue(couponIssueRepository.isEmpty());
				assertThat(couponIssueCountRepository.getIssueCount(coupon.getId())).isEqualTo(maxIssueCount);
				assertThat(couponRepository.findById(coupon.getId()).get().getIssuedCount()).isEqualTo(maxIssueCount);
			});
	}

	@DisplayName("쿠폰개수를 10단위로 동기화한다.")
	@Test
	public void syncCouponCountInTens() throws InterruptedException {
		//given
		int syncCount = 10;
		int maxIssueCount = 20;
		int issueCount = 19;
		Coupon coupon = createCoupon(maxIssueCount, 0);

		Queue<User> users = new ConcurrentLinkedDeque<>();
		for (int i = 0; i < issueCount; i++) {
			User user = createUser();
			users.add(user);
		}

		//when
		ExecutorService executorService = Executors.newFixedThreadPool(32);
		CountDownLatch latch = new CountDownLatch(issueCount);

		for (int i = 0; i < issueCount; i++) {
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
		await()
			.atMost(4, SECONDS)
			.untilAsserted(() -> {
				assertThat(couponTransactionHistoryRepository.findAll()).hasSize(issueCount);
				assertTrue(couponIssueRepository.isEmpty());
				assertThat(couponIssueCountRepository.getIssueCount(coupon.getId())).isEqualTo(issueCount);
				assertThat(couponRepository.findById(coupon.getId()).get().getIssuedCount()).isEqualTo(
					issueCount / syncCount * syncCount);
			});
	}

	@DisplayName("쿠폰 동기화 중 예외 발생시 쿠폰 발급은 정상처리된다.")
	@Test
	public void issueCouponsSuccessfullyWhenSyncFails() throws InterruptedException {
		//given
		int maxIssueCount = 10;
		Coupon coupon = createCoupon(maxIssueCount, 0);

		Queue<User> users = new ConcurrentLinkedDeque<>();
		for (int i = 0; i < maxIssueCount; i++) {
			User user = createUser();
			users.add(user);
		}

		doThrow(new RuntimeException()).when(couponService).syncCouponIssuedCount(coupon.getId(), (long)maxIssueCount);

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
		await()
			.atMost(4, SECONDS)
			.untilAsserted(() -> {
				assertThat(couponTransactionHistoryRepository.findAll()).hasSize(maxIssueCount);
				assertTrue(couponIssueRepository.isEmpty());
				assertThat(couponIssueCountRepository.getIssueCount(coupon.getId())).isEqualTo(maxIssueCount);
				assertThat(couponRepository.findById(coupon.getId()).get().getIssuedCount()).isEqualTo(0);
			});
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