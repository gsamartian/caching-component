package com.demo.caching.config.endpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.AbstractEndpoint;
import org.springframework.stereotype.Component;

import com.demo.caching.util.CacheUtils;

@Component
public class CacheEndpoint extends AbstractEndpoint<String> {
	@Autowired
	CacheUtils cacheUtils;

	public CacheEndpoint() {
		super("cache");
	}

	@Override
	public String invoke() {
		cacheUtils.findAndSetExpirationTimeForCaches();
		return "Success";
	}

}
