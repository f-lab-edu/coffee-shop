package com.coffee_shop.coffeeshop.common.domain;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Repository
public class RedisRepository {
	private final RedisTemplate<String, Long> redisTemplate;

	public Long increment(String key) {
		return redisTemplate
			.opsForValue()
			.increment(key);
	}

	public Long getIssueCount(String key) {
		Long issueCount = redisTemplate
			.opsForValue()
			.get(key);

		if (issueCount == null) {
			return 0L;
		}

		return issueCount;
	}
}
