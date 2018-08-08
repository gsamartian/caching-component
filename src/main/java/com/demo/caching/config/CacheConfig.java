package com.demo.caching.config;

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

	public String getExpiryTime() {
		return expiryTime;
	}

	public void setExpiryTime(String expiryTime) {
		this.expiryTime = expiryTime;
	}

	String name;
	Long expirySeconds;
	String expiryTime;

}
