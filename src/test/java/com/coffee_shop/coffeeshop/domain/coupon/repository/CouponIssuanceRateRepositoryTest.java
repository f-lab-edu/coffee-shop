// package com.coffee_shop.coffeeshop.domain.coupon.repository;
//
// import org.junit.jupiter.api.AfterEach;
// import org.junit.jupiter.api.DisplayName;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.data.redis.core.RedisTemplate;
//
// import com.coffee_shop.coffeeshop.service.IntegrationTestSupport;
//
// class CouponIssuanceRateRepositoryTest extends IntegrationTestSupport {
// 	private static final String COUPON_ISSUANCE_RATE_KEY_PREFIX = "coupon_issuance_rate";
//
// 	@Autowired
// 	private CouponIssuanceRateRepository couponIssuanceRateRepository;
//
// 	@Autowired
// 	private RedisTemplate<String, String> redisTemplateString;
//
// 	@AfterEach
// 	void tearDown() {
// 		clearAll();
// 	}
//
// 	@DisplayName("redis에서 쿠폰 처리량 설정 값을 가져온다.")
// 	@Test
// 	void getIssuanceRate() {
// 		//given
// 		redisTemplateString.opsForValue().set(COUPON_ISSUANCE_RATE_KEY_PREFIX, "20");
//
// 		//when
// 		long rangeCount = couponIssuanceRateRepository.getRangeCount();
//
// 		//then
// 		Assertions.assertThat(rangeCount).isEqualTo(20L);
// 	}
//
// 	@DisplayName("redis에서 쿠폰 처리량 설정 값을 가져올 때 해당 키가 없으면 기본값인 10을 반환한다.")
// 	@Test
// 	void getDefaultIssuanceRateWhenRedisDoesNotExistKey() {
// 		//when
// 		long rangeCount = couponIssuanceRateRepository.getRangeCount();
//
// 		//then
// 		Assertions.assertThat(rangeCount).isEqualTo(10L);
// 	}
//
// 	@DisplayName("쿠폰 처리량 설정 값을 숫자로 변환할 수 없는 값을 넣었을때 기본값인 10을 반환한다.")
// 	@Test
// 	void getDefaultIssuanceRateWhenValueCanNotConvertToNumber() {
// 		//given
// 		redisTemplateString.opsForValue().set(COUPON_ISSUANCE_RATE_KEY_PREFIX, "test");
//
// 		//when
// 		long rangeCount = couponIssuanceRateRepository.getRangeCount();
//
// 		//then
// 		Assertions.assertThat(rangeCount).isEqualTo(10L);
// 	}
//
// 	@DisplayName("쿠폰 처리량 설정 값을 최대 처리량보다 큰 값을 넣었을때 최대값인 50을 반환한다.")
// 	@Test
// 	void getMaxIssuanceRateWhenValueExceededMaxValue() {
// 		//given
// 		redisTemplateString.opsForValue().set(COUPON_ISSUANCE_RATE_KEY_PREFIX, "55");
//
// 		//when
// 		long rangeCount = couponIssuanceRateRepository.getRangeCount();
//
// 		//then
// 		Assertions.assertThat(rangeCount).isEqualTo(50L);
// 	}
//
// 	private void clearAll() {
// 		Set<String> keys = redisTemplateString.keys("*");
// 		if (keys != null && !keys.isEmpty()) {
// 			redisTemplateString.delete(keys);
// 		}
// 	}
// }