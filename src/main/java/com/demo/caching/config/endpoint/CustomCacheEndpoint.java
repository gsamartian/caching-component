package com.demo.caching.config.endpoint;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.mvc.EndpointMvcAdapter;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.demo.caching.util.CacheUtils;

@Component
public class CustomCacheEndpoint extends EndpointMvcAdapter {
	private final Logger LOG = LoggerFactory.getLogger(getClass());
		
	private final CacheEndpoint delegate;
	@Autowired
	CacheManager cacheMgr;
	
	@Autowired
	CacheUtils cacheUtils;

	public CustomCacheEndpoint(CacheEndpoint delegate) {
		super(delegate);
		this.delegate = delegate;

	}

	@RequestMapping(value = "/expiration/refresh", method = RequestMethod.POST)
	@ResponseBody
	public String doRefresh() {
		LOG.debug("Entering doRefresh");
		return this.delegate.invoke();
	}
	

	@SuppressWarnings("unused")
	@RequestMapping(value = "/cachemanager/refresh", method = RequestMethod.POST)
	@ResponseBody
	public String doCacheManagerRefresh() {
		LOG.debug("Entering CacheManager Refresh...");
		Cache cache=cacheMgr.getCache("test");
		LOG.debug("Leaving doCacheManagerRefresh.");
		return "Success";
	}
	
	@RequestMapping(value = "/invalidatekeys/{cacheName}", method = RequestMethod.DELETE)
	@ResponseBody
	public String invalidateCacheKeys(@PathVariable("cacheName") String cacheName,@RequestBody List<String> cacheKeyList) {
		LOG.debug("Entering invalidateCacheKeys..");
		LOG.debug("Received params: cacheName: {}, cacheKeyList:{}",cacheName,cacheKeyList);
		cacheUtils.invalidCacheKeys(cacheName, cacheKeyList);
		LOG.debug("Leaving invalidateCacheKeys..");
		return "Success";
	}

	
	@RequestMapping(value = "/clear/{cacheName}", method = RequestMethod.DELETE)
	@ResponseBody
	public String clearCache(@PathVariable("cacheName") String cacheName) {
		LOG.debug("Entering clearCache..");
		LOG.debug("Received params: cacheName: {}",cacheName);
		cacheUtils.clearCache(cacheName);
		LOG.debug("Leaving clearCache..");
		return "Success";
	}
}