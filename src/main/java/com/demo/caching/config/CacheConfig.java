package com.demo.caching.config;

public class CacheConfig {

	
	@Override
	public String toString() {
		return "CacheConfig [name=" + name + ", expirySeconds=" + expirySeconds + ", expiryTime=" + expiryTime
				+ "]";
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getExpirySeconds() {
		return expirySeconds;
	}
	public void setExpirySeconds(String expirySeconds) {
		this.expirySeconds = expirySeconds;
	}
	public String getExpiryTime() {
		return expiryTime;
	}
	public void setExpiryTime(String expiryTime) {
		this.expiryTime = expiryTime;
	}
	String name;
	String expirySeconds;
	String expiryTime;
	
	
}
