package com.demo.caching.util;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.demo.caching.config.AppConfig;
import com.demo.caching.config.CacheConfig;

@Component
public class CacheUtils {

	private final Logger LOG = LoggerFactory.getLogger(getClass());

	@Value("${refresh.endpoint}")
	String refreshEndpoint;

	@Autowired
	RedisTemplate<String, Object> redisTemplate;

	@Autowired
	AppConfig appConfig;
	
	@Autowired
	CacheManager cacheMgr;

	@SuppressWarnings("unused")
	public void findAndSetExpirationTimeForCaches() {
		LOG.debug("Entering..");

		LOG.debug("Updating Spring RedisCacheManager to have the  values updated in Configuration for new Keys...");
		// Update RedisCacheManager (using /refresh Endpoint) so that New Keys Created
		// for the passed "cacheName" will have the Expiration Time Set to "seconds"
		invokeRefreshOnService();

		LOG.info("Updated Spring RedisCacheManager to have the  values updated in Configuration for new Keys");

		// Required to Refresh CacheManagerBean which has RefreshScope
		refreshCacheManager();

		List<CacheConfig> cacheConfigList = appConfig.getCacheConfig();
		if (null != cacheConfigList && cacheConfigList.size() > 0) {
			LOG.debug("CacheConfigList is:");
			Consumer<CacheConfig> action = new Consumer<CacheConfig>() {

				@Override
				public void accept(CacheConfig cacheConfig) {
					if (null != cacheConfig) {
						LOG.debug("Next:{}", cacheConfig.toString());
						// Update Existing Keys for Updated Caches in Configuration
						updateExpiryExistingKeys(cacheConfig.getName(), cacheConfig.getExpirySeconds());
					}
				}
			};
			cacheConfigList.forEach(action);
		}
		LOG.debug("Leaving.");
	}

	public void invokeRefreshOnService() {
		LOG.debug("Entering...");
		LOG.debug("Configured refreshEndpoint is: {}", refreshEndpoint);

		for (String node : StringUtils.commaDelimitedListToStringArray(refreshEndpoint)) {
			LOG.debug("Next Node is:" + node);
			RestTemplate restTemplate = new RestTemplate();
			HttpEntity<String> request = new HttpEntity<>(new String("refresh"));
			String response = restTemplate.postForObject(node + "/refresh", request, String.class);
			LOG.info("Received Response:" + response);
		}

		LOG.debug("Leaving.");
	}

	public void refreshCacheManager() {
		LOG.debug("Entering...");
		LOG.debug("Configured refreshEndpoint is: {}", refreshEndpoint);

		for (String node : StringUtils.commaDelimitedListToStringArray(refreshEndpoint)) {
			LOG.debug("Next Node is:" + node);
			RestTemplate restTemplate = new RestTemplate();
			HttpEntity<String> request = new HttpEntity<>(new String("refreshCacheManager"));
			String response = restTemplate.postForObject(node + "/cache/cachemanager/refresh", request, String.class);
			LOG.info("Received Response:" + response);
		}

		LOG.debug("Leaving.");
	}

	public void updateExpiryExistingKeys(String cacheName, long seconds) {
		LOG.debug("Entering...");
		LOG.info("Received cacheName:{} , seconds:{}", cacheName, seconds);

		LOG.debug("Fetching all keys of cache:{}", cacheName);
		
		// Fetch All Keys with pattern cachename*
		RedisConnection redisConnection = redisTemplate.getConnectionFactory().getConnection();
		String cacheNamePattern = cacheName + "*";

		Set<String> keySet = redisTemplate.keys(cacheNamePattern);
		redisConnection.close();

		LOG.debug("Updating Expiration Time of All Existing Keys of  cache:{}...", cacheName);
		
		// Update Expiration Time for Keys returned based on pattern cachename*
		redisTemplate.executePipelined(new SessionCallback<Object>() {

			@SuppressWarnings("unchecked")
			@Override
			public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
				keySet.forEach((temp) -> {
					operations.multi();
					LOG.info("next key To Expire is: {}", temp);
					operations.expire((K) temp, seconds, TimeUnit.SECONDS);
				});
				operations.exec();
				return null;
			}
		});
		LOG.info("Updated Expiration Time for Existing keys of cache:{}, To : new Time: {}", cacheName, seconds);
		LOG.debug("Leaving.");
	}

	public void invalidCacheKeys(String cacheName, List<String> cacheKeyList) {
		LOG.debug("Entering...");
		LOG.info("Received cacheName:{} , cacheKeyList:{}", cacheName, cacheKeyList);

		redisTemplate.executePipelined(new RedisCallback<Object>() {

			@Override
			public Object doInRedis(RedisConnection connection) throws DataAccessException {

				cacheKeyList.forEach((temp) -> {
					connection.multi();
					LOG.info("next key is: {}", temp);
					connection.del(temp.getBytes());
				});
				connection.exec();

				return null;
			}

		});
		LOG.info("Invalidated Keys:{} from cache : {}", cacheKeyList, cacheName);
		LOG.debug("Leaving.");

	}
	
	public void clearCache(String cacheName) {
		LOG.debug("Entering");
		cacheMgr.getCache(cacheName).clear();
		LOG.info("Cleared/Invalidated Cache: {}",cacheName);
		LOG.debug("Leaving.");
	}
}
