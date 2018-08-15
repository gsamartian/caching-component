package com.demo.caching.config;

import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

@Configuration
@RefreshScope
public class CacheConfig {

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "CacheConfig [name=" + name + ", expirySeconds=" + expirySeconds + ", expiryTime=" + expiryTime + "]";
	}

	public Long getExpirySeconds() {
		return expirySeconds;
	}

	public void setExpirySeconds(Long expirySeconds) {
		this.expirySeconds = expirySeconds;
	}

	String name;
	Long expirySeconds;
	String expiryTime;

	public String getExpiryTime() {
		return expiryTime;
	}

	public void setExpiryTime(String expiryTime) {
		this.expiryTime = expiryTime;
	}

}
