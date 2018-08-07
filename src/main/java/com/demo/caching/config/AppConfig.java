package com.demo.caching.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {

	List<CacheConfig> cacheConfig;

	public List<CacheConfig> getCacheConfig() {
		return cacheConfig;
	}

	public void setCacheConfig(List<CacheConfig> cacheConfig) {
		this.cacheConfig = cacheConfig;
	}

	@Override
	public String toString() {
		return "AppConfig [cacheConfig=" + cacheConfig + "]";
	}


	
}
