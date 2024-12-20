package com.coffee_shop.coffeeshop.domain.coupon.repository;

import static com.coffee_shop.coffeeshop.domain.coupon.CouponType.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.context.ActiveProfiles;

import com.coffee_shop.coffeeshop.domain.coupon.Coupon;
import com.coffee_shop.coffeeshop.domain.user.User;
import com.coffee_shop.coffeeshop.domain.user.UserRepository;
import com.coffee_shop.coffeeshop.service.IntegrationTestSupport;
import com.coffee_shop.coffeeshop.service.coupon.dto.request.CouponApplication;

@ActiveProfiles("messageQ")
class CouponIssueRepositoryTest extends IntegrationTestSupport {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CouponRepository couponRepository;

	@Autowired
	private CouponIssueRepository couponIssueRepository;

	@Autowired
	private RedisTemplate<String, Object> redisTemplateJson;

	@AfterEach
	void tearDown() {
		couponRepository.deleteAllInBatch();
		userRepository.deleteAllInBatch();
		clearAll();
	}

	@Test
	void addUnique() {
		Coupon coupon = createCoupon();
		User user = createUser();

		CouponApplication couponApplication1 = CouponApplication.of(user, coupon);
		CouponApplication couponApplication2 = CouponApplication.of(user, coupon, 0);
		couponIssueRepository.add(couponApplication1, 1731488205);
		couponIssueRepository.add(couponApplication2, 1731488206);

		assertThat(couponIssueRepository.count()).isEqualTo(1L);
	}

	@Test
	void count() {
		Coupon coupon = createCoupon();
		User user1 = createUser();
		User user2 = createUser();

		couponIssueRepository.add(CouponApplication.of(user1, coupon), 1731488205);
		couponIssueRepository.add(CouponApplication.of(user2, coupon), 1731488206);

		assertThat(couponIssueRepository.count()).isEqualTo(2L);
	}

	@Test
	void countWhenKeyDoesNotExist() {
		assertThat(couponIssueRepository.count()).isEqualTo(0L);
	}

	@Test
	void isEmpty() {
		assertTrue(couponIssueRepository.isEmpty());

		Coupon coupon = createCoupon();
		User user = createUser();

		couponIssueRepository.add(CouponApplication.of(user, coupon), 1731488205);
		assertFalse(couponIssueRepository.isEmpty());
	}

	@Test
	void popMin() {
		Coupon coupon = createCoupon();
		User user1 = createUser();
		User user2 = createUser();
		User user3 = createUser();

		couponIssueRepository.add(CouponApplication.of(user1, coupon), 1731488205);
		couponIssueRepository.add(CouponApplication.of(user2, coupon), 1731488206);
		couponIssueRepository.add(CouponApplication.of(user3, coupon), 1731488204);

		Set<ZSetOperations.TypedTuple<Object>> popped = couponIssueRepository.popMin(10);
		assertThat(popped)
			.extracting("value")
			.containsExactly(
				CouponApplication.of(user3, coupon, 0),
				CouponApplication.of(user1, coupon, 0),
				CouponApplication.of(user2, coupon, 0)
			);
	}

	@Test
	void findPosition() {
		Coupon coupon = createCoupon();
		User user1 = createUser();
		User user2 = createUser();

		CouponApplication couponApplication1 = CouponApplication.of(user1, coupon);
		CouponApplication couponApplication2 = CouponApplication.of(user2, coupon);
		couponIssueRepository.add(couponApplication1, 1731488205);
		couponIssueRepository.add(couponApplication2, 1731488206);

		assertThat(couponIssueRepository.findPosition(couponApplication1)).isEqualTo(0L);
		assertThat(couponIssueRepository.findPosition(couponApplication2)).isEqualTo(1L);
	}

	@Test
	void findPositionWhenQueueDoesNotExist() {
		Coupon coupon = createCoupon();
		User user = createUser();
		CouponApplication couponApplication = CouponApplication.of(user, coupon);

		assertThat(couponIssueRepository.findPosition(couponApplication)).isEqualTo(null);
	}

	@Test
	void findPositionWhenValueDoesNotExist() {
		Coupon coupon = createCoupon();
		User user1 = createUser();
		User user2 = createUser();
		CouponApplication couponApplication = CouponApplication.of(user1, coupon);
		CouponApplication couponApplication2 = CouponApplication.of(user2, coupon);
		couponIssueRepository.add(couponApplication, 1731488206);

		assertThat(couponIssueRepository.findPosition(couponApplication2)).isEqualTo(null);
	}

	private void clearAll() {
		Set<String> keys = redisTemplateJson.keys("*");
		if (keys != null && !keys.isEmpty()) {
			redisTemplateJson.delete(keys);
		}
	}

	private Coupon createCoupon() {
		Coupon coupon = Coupon.builder()
			.name("오픈기념 선착순 할인 쿠폰")
			.type(AMOUNT)
			.discountAmount(1000)
			.minOrderAmount(4000)
			.maxIssueCount(10)
			.issuedCount(0)
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
