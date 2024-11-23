package com.coffee_shop.coffeeshop.service.coupon.issue.fail;

import static com.coffee_shop.coffeeshop.domain.coupon.CouponType.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import com.coffee_shop.coffeeshop.domain.coupon.Coupon;
import com.coffee_shop.coffeeshop.domain.coupon.MessageQ;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponRepository;
import com.coffee_shop.coffeeshop.domain.coupon.repository.CouponTransactionHistoryRepository;
import com.coffee_shop.coffeeshop.domain.user.User;
import com.coffee_shop.coffeeshop.domain.user.UserRepository;
import com.coffee_shop.coffeeshop.service.IntegrationTestSupport;
import com.coffee_shop.coffeeshop.service.coupon.apply.CouponApplyService;
import com.coffee_shop.coffeeshop.service.coupon.dto.request.CouponApplication;
import com.coffee_shop.coffeeshop.service.coupon.dto.request.CouponApplyServiceRequest;
import com.coffee_shop.coffeeshop.service.coupon.issue.CouponIssueServiceImpl;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

class CouponIssueFailHandlerImplTest extends IntegrationTestSupport {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CouponRepository couponRepository;

	@Autowired
	private CouponIssueFailHandlerImpl couponIssueFailHandlerImpl;

	@Autowired
	private CouponApplyService couponApplyService;

	@Autowired
	private MessageQ messageQ;

	@Autowired
	private CouponTransactionHistoryRepository couponTransactionHistoryRepository;

	@SpyBean
	private CouponIssueServiceImpl couponIssueService;

	private Long exceptionUserId;

	@AfterEach
	void tearDown() {
		couponTransactionHistoryRepository.deleteAllInBatch();
		couponRepository.deleteAllInBatch();
		userRepository.deleteAllInBatch();
	}

	@DisplayName("한개 쿠폰 발급 실패 시 최대 실패 횟수가 초과하면 실패로그를 남기고 발급을 실패한다.")
	@Test
	void failIssueCoupon() throws InterruptedException {
		//given
		int maxFailCount = 3;
		Coupon coupon = createCoupon(10, 0);
		User user = createUser();
		LocalDateTime issueDateTime = LocalDateTime.of(2024, 8, 30, 0, 0);

		doThrow(new RuntimeException()).when(couponIssueService).issueCoupon(any(CouponApplication.class));

		ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
		Logger logger = (Logger)LoggerFactory.getLogger(CouponIssueFailHandlerImpl.class);
		logger.addAppender(listAppender);
		listAppender.start();

		//when
		couponApplyService.applyCoupon(createRequest(user.getId(), coupon.getId()));

		//then
		Thread.sleep(1000);

		assertThat(couponTransactionHistoryRepository.findAll()).hasSize(0);

		int expectedIssuedCount = couponRepository.findById(coupon.getId()).get().getIssuedCount();
		assertThat(expectedIssuedCount).isEqualTo(0);

		assertTrue(messageQ.isEmpty());

		List<ILoggingEvent> testLogs = listAppender.list;
		assertThat(testLogs.size()).isEqualTo(7);
		assertThat(testLogs.get(0).getMessage()).isEqualTo(
			"최대 실패 횟수 " + maxFailCount + "회를 초과하였습니다. 실패 횟수 : " + maxFailCount);
		assertThat(testLogs.get(2).getMessage()).isEqualTo(
			"실패한 메시지 : CouponApplication{userId=" + user.getId() + ", couponId=" + coupon.getId() + ", issueDateTime="
				+ issueDateTime + ", failCount=" + maxFailCount
				+ ", exceptionList=[java.lang.RuntimeException, java.lang.RuntimeException, java.lang.RuntimeException]}");
	}

	@DisplayName("여러개의 쿠폰 발급 실패 시 한개의 쿠폰발급에서 예외가 발생하면 큐의 맨앞으로 넣어 재시도후 성공하면 정상 발급한다.")
	@Test
	void retryFailIssueCoupons() throws InterruptedException {
		//given
		int maxIssueCount = 1000;
		int maxFailCount = 3;

		Coupon coupon = createCoupon(maxIssueCount, 0);

		LocalDateTime issueDateTime = LocalDateTime.of(2024, 8, 30, 0, 0);

		//log 체크
		ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
		Logger logger = (Logger)LoggerFactory.getLogger(CouponIssueFailHandlerImpl.class);
		logger.addAppender(listAppender);
		listAppender.start();

		//10명 유저 생성
		Queue<Long> users = new ConcurrentLinkedDeque<>();
		for (int i = 0; i < maxIssueCount; i++) {
			User user = createUser();
			users.add(user.getId());
			if (i == 0) {
				exceptionUserId = user.getId();
			}
		}

		for (int i = 0; i < maxFailCount - 1; i++) {
			doThrow(new RuntimeException()).when(couponIssueService).issueCoupon(CouponApplication.builder()
				.userId(exceptionUserId)
				.couponId(coupon.getId())
				.failCount(i)
				.build());
		}

		//when
		ExecutorService executorService = Executors.newFixedThreadPool(32);
		CountDownLatch latch = new CountDownLatch(maxIssueCount);

		for (int i = 0; i < maxIssueCount; i++) {
			executorService.submit(() -> {
				try {
					couponApplyService.applyCoupon(createRequest(users.remove(), coupon.getId()));
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

		assertThat(couponTransactionHistoryRepository.findAll()).hasSize(maxIssueCount);

		List<Coupon> coupons = couponRepository.findAll();
		assertThat(coupons.get(0).getIssuedCount()).isEqualTo(maxIssueCount);

		assertTrue(messageQ.isEmpty());

		List<ILoggingEvent> testLogs = listAppender.list;
		assertThat(testLogs.size()).isEqualTo(0);
	}

	@DisplayName("여러개의 쿠폰 발급 실패 시 한개의 쿠폰이 예외가 터져 최대 실패 회수를 초과하여 발급에 실패하면 해당 발급은 실패로그를 남기고 실패한다. 나머지는 정상 발급된다.")
	@Test
	void failIssueCoupons() throws InterruptedException {
		//given
		int maxIssueCount = 1000;
		int maxFailCount = 3;

		Coupon coupon = createCoupon(maxIssueCount, 0);

		LocalDateTime issueDateTime = LocalDateTime.of(2024, 8, 30, 0, 0);

		//log 체크
		ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
		Logger logger = (Logger)LoggerFactory.getLogger(CouponIssueFailHandlerImpl.class);
		logger.addAppender(listAppender);
		listAppender.start();

		//1000명 유저 생성
		Queue<Long> users = new ConcurrentLinkedDeque<>();
		for (int i = 0; i < maxIssueCount; i++) {
			User user = createUser();
			users.add(user.getId());
			if (i == 2) {
				exceptionUserId = user.getId();
			}
		}

		for (int i = 0; i < maxFailCount; i++) {
			doThrow(new RuntimeException()).when(couponIssueService).issueCoupon(CouponApplication.builder()
				.userId(exceptionUserId)
				.couponId(coupon.getId())
				.failCount(i)
				.build());
		}

		//when
		ExecutorService executorService = Executors.newFixedThreadPool(32);
		CountDownLatch latch = new CountDownLatch(maxIssueCount);

		for (int i = 0; i < maxIssueCount; i++) {
			executorService.submit(() -> {
				try {
					couponApplyService.applyCoupon(createRequest(users.remove(), coupon.getId()));
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

		List<Coupon> coupons = couponRepository.findAll();
		assertThat(coupons.get(0).getIssuedCount()).isEqualTo(maxIssueCount - 1);

		assertTrue(messageQ.isEmpty());

		List<ILoggingEvent> testLogs = listAppender.list;
		assertThat(testLogs.size()).isEqualTo(7);
		assertThat(testLogs.get(0).getMessage()).isEqualTo(
			"최대 실패 횟수 " + maxFailCount + "회를 초과하였습니다. 실패 횟수 : " + maxFailCount);
		assertThat(testLogs.get(2).getMessage()).isEqualTo(
			"실패한 메시지 : CouponApplication{userId=" + exceptionUserId + ", couponId=" + coupon.getId()
				+ ", issueDateTime="
				+ issueDateTime + ", failCount=" + maxFailCount
				+ ", exceptionList=[java.lang.RuntimeException, java.lang.RuntimeException, java.lang.RuntimeException]}");
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

}
