package com.demo.caching.config;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@EnableCaching
public class RedisCacheConfig implements CachingConfigurer {
	private final Logger LOG = LoggerFactory.getLogger(getClass());
	
	@Autowired
	RedisTemplate<?, ?> redisTemplate;

	@Autowired
	AppConfig appConfig;

	@Bean
	public CacheManager cacheManager() {
		RedisCacheManager redisCacheManager = new RedisCacheManager(redisTemplate);
		redisCacheManager.setDefaultExpiration(5 * 60);
		List<CacheConfig> cacheConfigList = appConfig.getCacheConfig();
		if (null != cacheConfigList && cacheConfigList.size()>0) {
			redisCacheManager.setExpires(getCacheExpirySecondsMap(cacheConfigList));
			setExpiryTimeList(cacheConfigList);
		}
		return redisCacheManager;
	}

	private void setExpiryTimeList(List<CacheConfig> cacheConfigList) {
		Map<String, String> cacheConfigMap = cacheConfigList.stream().filter(
				cacheConfig -> cacheConfig.getExpiryTime() != null && cacheConfig.getExpiryTime().length() > 0)
				.collect(Collectors.toMap(x -> x.getName(), x -> x.getExpiryTime()));

		cacheConfigMap.forEach((k, v) -> LOG.info((k + ":" + v)));
		
	}

	private Map<String, Long> getCacheExpirySecondsMap(List<CacheConfig> cacheConfigList) {
		Map<String, Long> cacheConfigMap = cacheConfigList.stream().filter(
				cacheConfig -> cacheConfig.getExpirySeconds() != null && cacheConfig.getExpirySeconds().length() > 0)
				.collect(Collectors.toMap(x -> x.getName(), x -> toLong(x.getExpirySeconds())));

		cacheConfigMap.forEach((k, v) -> LOG.info((k + ":" + v)));
		return cacheConfigMap;

	}

	public Long toLong(String value) {
		return Long.parseLong(value);
	}

	@Override
	public CacheResolver cacheResolver() {
		return null;
	}

	@Override
	public KeyGenerator keyGenerator() {
		return null;
	}

	@Override
	public CacheErrorHandler errorHandler() {
		return null;
	}
}
