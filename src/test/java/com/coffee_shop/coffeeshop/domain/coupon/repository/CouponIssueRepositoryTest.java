package com.coffee_shop.coffeeshop.domain.coupon.repository;

import static com.coffee_shop.coffeeshop.domain.coupon.CouponType.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.coffee_shop.coffeeshop.domain.coupon.Coupon;
import com.coffee_shop.coffeeshop.domain.user.User;
import com.coffee_shop.coffeeshop.domain.user.UserRepository;
import com.coffee_shop.coffeeshop.service.IntegrationTestSupport;

class CouponIssueRepositoryTest extends IntegrationTestSupport {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CouponRepository couponRepository;

	@Autowired
	private CouponIssueRepository couponIssueRepository;

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@AfterEach
	void tearDown() {
		clearAll();
	}

	@Test
	void add() {
		Coupon coupon = createCoupon();
		User user = createUser();

		couponIssueRepository.add(coupon, user, 1731488205);
		couponIssueRepository.add(coupon, user, 1731488206);

		Assertions.assertThat(couponIssueRepository.count()).isEqualTo(1L);
	}

	@Test
	void count() {
		Coupon coupon = createCoupon();
		User user1 = createUser();
		User user2 = createUser();

		couponIssueRepository.add(coupon, user1, 1731488205);
		couponIssueRepository.add(coupon, user2, 1731488206);

		Assertions.assertThat(couponIssueRepository.count()).isEqualTo(2L);
	}

	@Test
	void isEmpty() {
		assertTrue(couponIssueRepository.isEmpty());

		Coupon coupon = createCoupon();
		User user = createUser();

		couponIssueRepository.add(coupon, user, 1731488205);
		assertFalse(couponIssueRepository.isEmpty());
	}

	@Test
	void range() {
		Coupon coupon = createCoupon();
		User user1 = createUser();
		User user2 = createUser();
		User user3 = createUser();

		couponIssueRepository.add(coupon, user1, 1731488205);
		couponIssueRepository.add(coupon, user2, 1731488206);
		couponIssueRepository.add(coupon, user3, 1731488206);

		Set<Object> couponApplications = couponIssueRepository.range(1, 2);
		assertThat(couponApplications)
			.extracting("userId", "couponId", "failCount", "exceptionList")
			.containsExactly(
				tuple(user2.getId(), coupon.getId(), 0, List.of()),
				tuple(user3.getId(), coupon.getId(), 0, List.of())
			);
	}

	private void clearAll() {
		Set<String> keys = redisTemplate.keys("*");
		if (keys != null && !keys.isEmpty()) {
			redisTemplate.delete(keys);
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
