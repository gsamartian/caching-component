package com.demo.caching.config;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@ConditionalOnProperty(value = "spring.cache.type", havingValue = "redis")
@EnableCaching
public class RedisCacheConfig implements CachingConfigurer {
	private final Logger LOG = LoggerFactory.getLogger(getClass());

	@Autowired
	RedisTemplate<?, ?> redisTemplate;

	@Autowired
	AppConfig appConfig;

	@Bean
	public CacheManagerCustomizer<RedisCacheManager> cacheManagerCustomizer() {
		return new CacheManagerCustomizer<RedisCacheManager>() {
			@Override
			public void customize(RedisCacheManager cacheManager) {
				cacheManager.setUsePrefix(true);
				List<CacheConfig> cacheConfigList = appConfig.getCacheConfig();
				if (null != cacheConfigList && cacheConfigList.size() > 0) {
					cacheManager.setExpires(getCacheExpirySecondsMap(cacheConfigList));
				}

			}
		};
	}

	private Map<String, Long> getCacheExpirySecondsMap(List<CacheConfig> cacheConfigList) {
		LOG.info("Entering...");
		Map<String, Long> cacheConfigMap = cacheConfigList.stream()
				.filter(cacheConfig -> cacheConfig.getExpirySeconds() != null && cacheConfig.getExpirySeconds() != 0)
				.collect(Collectors.toMap(x -> x.getName(), x -> x.getExpirySeconds()));
		cacheConfigMap.forEach((k, v) -> LOG.info((k + ":" + v)));

		LOG.info("Leaving.");

		return cacheConfigMap;
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
		return new RedisCacheError();
	}

	@Override
	public CacheManager cacheManager() {
		return null;
	}
}
