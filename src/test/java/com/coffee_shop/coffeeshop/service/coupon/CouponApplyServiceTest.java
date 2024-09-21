package com.coffee_shop.coffeeshop.service.coupon;

import static com.coffee_shop.coffeeshop.domain.coupon.CouponType.*;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.coffee_shop.coffeeshop.common.exception.BusinessException;
import com.coffee_shop.coffeeshop.domain.coupon.Coupon;
import com.coffee_shop.coffeeshop.domain.coupon.CouponRepository;
import com.coffee_shop.coffeeshop.domain.coupon.CouponTransactionHistory;
import com.coffee_shop.coffeeshop.domain.coupon.CouponTransactionHistoryRepository;
import com.coffee_shop.coffeeshop.domain.coupon.MessageQ;
import com.coffee_shop.coffeeshop.domain.coupon.consumer.CouponConsumer;
import com.coffee_shop.coffeeshop.domain.coupon.producer.CouponMessageQProducer;
import com.coffee_shop.coffeeshop.domain.user.User;
import com.coffee_shop.coffeeshop.domain.user.UserRepository;
import com.coffee_shop.coffeeshop.service.IntegrationTestSupport;
import com.coffee_shop.coffeeshop.service.coupon.dto.request.CouponApplyServiceRequest;

class CouponApplyServiceTest extends IntegrationTestSupport {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CouponRepository couponRepository;

	@Autowired
	private CouponTransactionHistoryRepository couponTransactionHistoryRepository;

	@MockBean
	private CouponConsumer couponMessageQConsumer;

	private CouponApplyService couponApplyService;
	private MessageQ messageQ;

	@BeforeEach
	void setUp() {
		messageQ = new MessageQ();
		CouponMessageQProducer couponMessageQProducer = new CouponMessageQProducer(messageQ);
		couponApplyService = new CouponApplyService(userRepository, couponRepository, couponMessageQProducer,
			couponTransactionHistoryRepository);
	}

	@AfterEach
	void tearDown() {
		couponTransactionHistoryRepository.deleteAllInBatch();
		couponRepository.deleteAllInBatch();
		userRepository.deleteAllInBatch();
	}

	@DisplayName("고객이 쿠폰 발급 신청할경우 메시지 큐에 쌓인다.")
	@Test
	void applyCoupon() {
		//given
		Coupon coupon = couponRepository.save(createCoupon(10, 0));
		User user = userRepository.save(createUser());
		LocalDateTime issueDateTime = LocalDateTime.of(2024, 8, 30, 0, 0);

		//when
		couponApplyService.applyCoupon(createRequest(user.getId(), coupon.getId()), issueDateTime);

		//then
		assertThat(messageQ.size()).isEqualTo(1);
	}

	@DisplayName("쿠폰을 여러명이 발급 신청할경우 메시지 큐에 신청 개수만큼 쌓인다.")
	@Test
	void applyCoupons() throws InterruptedException {
		//given
		int maxIssueCount = 1000;
		Coupon coupon = couponRepository.save(createCoupon(maxIssueCount, 0));
		User user = userRepository.save(createUser());
		LocalDateTime issueDateTime = LocalDateTime.of(2024, 8, 30, 0, 0);

		//when
		ExecutorService executorService = Executors.newFixedThreadPool(32);
		CountDownLatch latch = new CountDownLatch(maxIssueCount);

		for (int i = 0; i < maxIssueCount; i++) {
			executorService.submit(() -> {
				try {
					couponApplyService.applyCoupon(createRequest(user.getId(), coupon.getId()), issueDateTime);
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();

		//then
		assertThat(messageQ.size()).isEqualTo(maxIssueCount);
	}

	@DisplayName("선착순 쿠폰 수량이 소진된 경우 쿠폰 발급 신청시 발급이 불가능하다.")
	@Test
	public void applyCouponWhenCouponLimitReached() {
		//given
		Coupon coupon = couponRepository.save(createCoupon(10, 10));
		User user = userRepository.save(createUser());

		LocalDateTime issueDateTime = LocalDateTime.of(2024, 8, 30, 0, 0);

		//when, then
		assertThatThrownBy(
			() -> couponApplyService.applyCoupon(createRequest(user.getId(), coupon.getId()), issueDateTime))
			.isInstanceOf(BusinessException.class)
			.hasMessage("쿠폰이 모두 소진되어 발급할 수 없습니다.");
	}

	@DisplayName("한명의 사용자에게 동일한 쿠폰을 중복으로 발급할 수 없다.")
	@Test
	public void applyCouponToUniqueUser() {
		//given
		Coupon coupon = couponRepository.save(createCoupon(10, 0));
		User user = userRepository.save(createUser());
		LocalDateTime issueDateTime = LocalDateTime.of(2024, 8, 30, 0, 0);

		couponTransactionHistoryRepository.save(createCouponTransactionHistory(coupon, user, issueDateTime));

		//when, then
		assertThatThrownBy(
			() -> couponApplyService.applyCoupon(createRequest(user.getId(), coupon.getId()), issueDateTime))
			.isInstanceOf(BusinessException.class)
			.hasMessage("이미 발급된 쿠폰입니다. 사용자 ID, 이름 : " + user.getId() + ", "
				+ user.getName());
	}

	private CouponApplyServiceRequest createRequest(Long userId, Long couponId) {
		return CouponApplyServiceRequest.builder()
			.userId(userId)
			.couponId(couponId)
			.build();
	}

	private CouponTransactionHistory createCouponTransactionHistory(Coupon coupon, User user,
		LocalDateTime issueDateTime) {
		return CouponTransactionHistory.builder()
			.user(user)
			.coupon(coupon)
			.issueDateTime(issueDateTime)
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
