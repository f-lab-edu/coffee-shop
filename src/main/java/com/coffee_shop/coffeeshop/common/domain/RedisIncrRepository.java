package com.coffee_shop.coffeeshop.common.domain;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Repository
public class RedisIncrRepository {
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
			log.warn("Redis key '{}' does not exist or used in transaction/pipeline",
				key);
			return 0L;
		}

		return issueCount;
	}
}
