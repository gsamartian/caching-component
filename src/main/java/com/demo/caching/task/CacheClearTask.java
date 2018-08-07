package com.demo.caching.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;

public class CacheClearTask implements Runnable {
	private final Logger LOG = LoggerFactory.getLogger(getClass());

	CacheManager mgr;
	String cacheName;
	
	public CacheClearTask(CacheManager mgr,String cacheName){
		this.mgr=mgr;
		this.cacheName=cacheName;
	}

	@Override
	public void run() {
		LOG.info("About to Clear: {}  Cache...",cacheName);
		mgr.getCache(this.cacheName).clear();
		LOG.info("Cache {} ,  Cleared Successfully",cacheName);
	}

}
