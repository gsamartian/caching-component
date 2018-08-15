package com.demo.caching.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@Configuration
@ConditionalOnProperty(value = "spring.cache.type", havingValue = "redis")
@EnableCaching
@RefreshScope
public class RedisCacheConfig implements CachingConfigurer {
	private final Logger LOG = LoggerFactory.getLogger(getClass());

	@Value("${spring.redis.sentinel.master}")
	String redisSentinelMaster;

	@Value("${spring.redis.sentinel.nodes}")
	String redisSentinelNodes;

	@Bean
	@RefreshScope
	public List<RedisNode> createSentinels() {
		List<RedisNode> nodes = new ArrayList<RedisNode>();
		for (String node : StringUtils.commaDelimitedListToStringArray(redisSentinelNodes)) {
			try {
				String[] parts = StringUtils.split(node, ":");
				Assert.state(parts.length == 2, "Must be defined as 'host:port'");
				nodes.add(new RedisNode(parts[0], Integer.valueOf(parts[1])));
			} catch (RuntimeException ex) {
				throw new IllegalStateException("Invalid redis sentinel " + "property '" + node + "'", ex);
			}
		}
		return nodes;
	}

	@Bean
	@RefreshScope
	public RedisConnectionFactory jedisConnectionFactory() {
		LOG.debug("Redis Sentinel Nodes:{}", redisSentinelNodes);
		List<RedisNode> nodes = createSentinels();
		RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration().master(redisSentinelMaster);
		sentinelConfig.setSentinels(nodes);
		return new JedisConnectionFactory(sentinelConfig);
	}

	@Bean
	@RefreshScope
	public RedisTemplate<Object, Object> redisTemplate() {
		RedisTemplate<Object, Object> template = new RedisTemplate<Object, Object>();
		template.setConnectionFactory(jedisConnectionFactory());
		return template;
	}

	@Autowired
	AppConfig appConfig;

	@Primary
	@Bean
	@RefreshScope
	public RedisCacheManager cacheManager() {
		LOG.debug("Entering...");

		RedisCacheManager cacheManager = new RedisCacheManager(redisTemplate());
		cacheManager.setUsePrefix(true);
		List<CacheConfig> cacheConfigList = appConfig.getCacheConfig();
		long defaultCacheExpiryTime = appConfig.getDefaultCacheExpiryTime();
		if (defaultCacheExpiryTime <= 0) {
			defaultCacheExpiryTime = 60 * 5;
		}
		LOG.debug("Setting DefaultCacheExpiryTime : {}", defaultCacheExpiryTime);
		cacheManager.setDefaultExpiration(defaultCacheExpiryTime);
		if (null != cacheConfigList && cacheConfigList.size() > 0) {
			LOG.debug("Setting Individual Cache Configuration");
			cacheConfigList.forEach(x -> LOG.debug("CacheConfig:{}", x));
			cacheManager.setExpires(getCacheExpirySecondsMap(cacheConfigList));
		}
		LOG.debug("Leaving.");
		return cacheManager;
	}

	public Map<String, Long> getCacheExpirySecondsMap(List<CacheConfig> cacheConfigList) {
		LOG.debug("Entering...");
		Map<String, Long> cacheConfigMap = cacheConfigList.stream()
				.filter(cacheConfig -> cacheConfig.getExpirySeconds() != null && cacheConfig.getExpirySeconds() != 0)
				.collect(Collectors.toMap(x -> x.getName(), x -> x.getExpirySeconds()));
		cacheConfigMap.forEach((k, v) -> LOG.debug((k + ":" + v)));
		LOG.debug("Leaving.");
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

}
