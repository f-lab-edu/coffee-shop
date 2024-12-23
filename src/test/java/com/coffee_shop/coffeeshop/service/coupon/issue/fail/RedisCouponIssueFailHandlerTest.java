package com.coffee_shop.coffeeshop.service.coupon.issue.fail;

import static com.coffee_shop.coffeeshop.domain.coupon.CouponType.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponIssueFailHistoryRepository;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponIssueRepository;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponRepository;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponTransactionHistoryRepository;
import com.coffee_shop.coffeeshop.domain.user.User;
import com.coffee_shop.coffeeshop.domain.user.UserRepository;
import com.coffee_shop.coffeeshop.service.IntegrationTestSupport;
import com.coffee_shop.coffeeshop.service.coupon.dto.request.CouponApplication;
import com.coffee_shop.coffeeshop.service.coupon.dto.request.CouponApplyServiceRequest;
import com.coffee_shop.coffeeshop.service.coupon.issue.RedisCouponIssueService;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

class RedisCouponIssueFailHandlerTest extends IntegrationTestSupport {
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

	@SpyBean
	private RedisCouponIssueService redisCouponIssueService;

	private Long exceptionUserId;

	@AfterEach
	void tearDown() {
		couponTransactionHistoryRepository.deleteAllInBatch();
		couponIssueFailHistoryRepository.deleteAllInBatch();
		couponRepository.deleteAllInBatch();
		userRepository.deleteAllInBatch();
		clearAll();
	}

	@DisplayName("쿠폰 발급 중 최대 실패 횟수가 초과하면 실패로그를 남기고 발급을 실패한다.")
	@Test
	void retryExceedsMaxAttempts() throws InterruptedException {
		//given
		int maxFailCount = 3;
		Coupon coupon = createCoupon(10, 0);
		User user = createUser();

		doThrow(new RuntimeException()).when(redisCouponIssueService).issueCoupon(any(CouponApplication.class));

		ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
		Logger logger = (Logger)LoggerFactory.getLogger(RedisCouponIssueFailHandler.class);
		logger.addAppender(listAppender);
		listAppender.start();

		//when
		couponIssueRepository.add(CouponApplication.of(user, coupon), 1731488205);

		//then
		Thread.sleep(3000);

		assertThat(couponTransactionHistoryRepository.findAll()).hasSize(0);
		assertThat(couponIssueFailHistoryRepository.findAll()).hasSize(1);

		assertTrue(couponIssueRepository.isEmpty());

		List<ILoggingEvent> testLogs = listAppender.list;
		assertThat(testLogs.size()).isEqualTo(3);
		assertThat(testLogs.get(2).getFormattedMessage()).isEqualTo(
			"최대 실패 횟수 " + maxFailCount + "회를 초과하였습니다. 실패 횟수 : " + maxFailCount);
	}

	@DisplayName("쿠폰 발급 중 최대 실패 회수를 초과한 쿠폰은 제외하고 나머지는 정상 발급된다.")
	@Test
	void issueCouponsExceptFail() throws InterruptedException {
		//given
		int maxIssueCount = 20;
		int maxFailCount = 3;

		Coupon coupon = createCoupon(maxIssueCount, 0);

		//log 체크
		ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
		Logger logger = (Logger)LoggerFactory.getLogger(RedisCouponIssueFailHandler.class);
		logger.addAppender(listAppender);
		listAppender.start();

		Queue<User> users = new ConcurrentLinkedDeque<>();
		for (int i = 0; i < maxIssueCount; i++) {
			User user = createUser();
			users.add(user);
			if (i == 2) {
				exceptionUserId = user.getId();
			}
		}

		doThrow(new RuntimeException()).when(redisCouponIssueService).issueCoupon(CouponApplication.builder()
			.userId(exceptionUserId)
			.couponId(coupon.getId())
			.failCount(0)
			.build());

		//when
		ExecutorService executorService = Executors.newFixedThreadPool(32);
		CountDownLatch latch = new CountDownLatch(maxIssueCount);

		for (int i = 0; i < maxIssueCount; i++) {
			long score = i;
			executorService.submit(() -> {
				try {
					couponIssueRepository.add(CouponApplication.of(users.remove(), coupon), 1731488205 + score);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();

		//then
		Thread.sleep(4000);

		assertThat(couponTransactionHistoryRepository.findAll()).hasSize(maxIssueCount - 1);
		assertThat(couponIssueFailHistoryRepository.findAll()).hasSize(1);

		assertTrue(couponIssueRepository.isEmpty());

		List<ILoggingEvent> testLogs = listAppender.list;
		assertThat(testLogs.size()).isEqualTo(3);
		assertThat(testLogs.get(2).getFormattedMessage()).isEqualTo(
			"최대 실패 횟수 " + maxFailCount + "회를 초과하였습니다. 실패 횟수 : " + maxFailCount);
	}

	private CouponApplyServiceRequest createRequest(Long userId, Long couponId) {
		return CouponApplyServiceRequest.builder()
			.userId(userId)
			.couponId(couponId)
			.build();
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