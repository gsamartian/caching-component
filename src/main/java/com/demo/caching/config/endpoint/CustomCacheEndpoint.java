package com.demo.caching.config.endpoint;

import org.springframework.boot.actuate.endpoint.mvc.EndpointMvcAdapter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Component
public class CustomCacheEndpoint extends EndpointMvcAdapter {

		
	private final CacheEndpoint delegate;

	public CustomCacheEndpoint(CacheEndpoint delegate) {
		super(delegate);
		this.delegate = delegate;

	}

	@RequestMapping(value = "/expiration", method = RequestMethod.POST)
	@ResponseBody
	public String doRefresh() {
		return this.delegate.invoke();
	}

}