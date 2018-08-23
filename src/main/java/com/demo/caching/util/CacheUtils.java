package com.demo.caching.util;

import java.net.URI;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.demo.caching.config.AppConfig;
import com.demo.caching.config.CacheConfig;
import com.demo.caching.config.CacheConstants;

@Component
public class CacheUtils {

	private final Logger LOG = LoggerFactory.getLogger(getClass());


	@Autowired
	RedisTemplate<String, Object> redisTemplate;

	@Autowired
	AppConfig appConfig;
	
	@Autowired
	CacheManager cacheMgr;
	
	@Autowired
	CloudUtils cloudUtils;

	
	

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
						updateExpiryExistingKeys(cacheConfig.getName(), String.valueOf(cacheConfig.getExpirySeconds()));
					}
				}
			};
			cacheConfigList.forEach(action);
		}
		LOG.debug("Leaving.");
	}

	public void invokeRefreshOnService() {
		LOG.debug("Entering...");
		invokeEndpoint(CacheConstants.REFRESH_PATH);
		LOG.debug("Leaving.");
	}

	public void refreshCacheManager() {
		LOG.debug("Entering...");
		invokeEndpoint(CacheConstants.REFRESH_CACHEMANAGER_PATH);
		LOG.debug("Leaving.");
	}


	
	public void updateExpiryExistingKeys(String cacheName, String seconds) {
		LOG.debug("Entering...");
		LOG.info("Received cacheName:{} , seconds:{}", cacheName, seconds);
		
		byte[] cacheNameLockBytes=(cacheName +CacheConstants.CACHE_LOCK).getBytes();
		RedisConnection redisConnection = redisTemplate.getConnectionFactory().getConnection();
		redisConnection.set(cacheNameLockBytes, cacheNameLockBytes);
		redisConnection.eval(CacheConstants.SET_EXPIRATION__KEYS_BY_PATTERN_LUA, ReturnType.INTEGER, 0, (cacheName + ":*").getBytes(),seconds.getBytes());
		redisConnection.del(cacheNameLockBytes, cacheNameLockBytes);
		redisConnection.close();
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
					String key=cacheName + ":" + temp;
					connection.multi();
					LOG.info("next key is: {}", key);
					connection.del(key.getBytes());
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
	
	private void invokeEndpoint(String type) {
		LOG.debug("Entering");
		List<URI> uriList=cloudUtils.getURIList();
		uriList.forEach( (uri) -> {
			LOG.info("Next URL is:{}",uri);
			HttpHeaders headers = new HttpHeaders();
			headers.set("Authorization",cloudUtils.getTokenForEnvironment());
			RestTemplate restTemplate = new RestTemplate();
			HttpEntity<String> request = new HttpEntity<>(new String("test"),headers);
			ResponseEntity<String> entityResponse=restTemplate.exchange(uri + type, HttpMethod.POST, request, String.class);
			String response=entityResponse.getBody();
			LOG.info("Received Response:" + response);
		});
		LOG.debug("Leaving.");
	}
}
