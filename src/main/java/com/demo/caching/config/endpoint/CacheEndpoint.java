package com.demo.caching.config.endpoint;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.AbstractEndpoint;
import org.springframework.stereotype.Component;

import com.demo.caching.util.CacheUtils;

@Component
public class CacheEndpoint extends AbstractEndpoint<String> {
	
	private final Logger LOG = LoggerFactory.getLogger(getClass());
	
	@Autowired
	CacheUtils cacheUtils;

	public CacheEndpoint() {
		super("cache");
	}

	@Override
	public String invoke() {
		LOG.debug("Entering invoke");
		cacheUtils.findAndSetExpirationTimeForCaches();
		LOG.debug("Leaving invoke");
		return "Success";
	}

}