package com.demo.caching.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import com.demo.caching.config.AppConfig;
import com.demo.caching.config.CacheConfig;

@Configuration
@ConditionalOnProperty(value="spring.cache.type", havingValue="redis")
@EnableScheduling
public class CacheScheduledTask implements SchedulingConfigurer {

	private final Logger LOG = LoggerFactory.getLogger(getClass());

	@Autowired
	private CacheManager cacheManager;

	@Autowired
	private AppConfig appConfig;

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		LOG.debug("Entering...");
		Map<String, String> cacheExpiryTimeMap = getCacheExpiryTimeMap(appConfig);
		if (null != cacheExpiryTimeMap && cacheExpiryTimeMap.size() > 0) {
			cacheExpiryTimeMap.forEach((k, v) -> {
				LOG.debug("Added Cron Task for cache: {} with CRON: {}", k, v);
				CronTask cronTask = new CronTask(new CacheClearTask(cacheManager, k), v);
				taskRegistrar.addCronTask(cronTask);
			});
		}
		LOG.debug("Leaving.");
	}

	private Map<String, String> getCacheExpiryTimeMap(AppConfig appConfig) {
		LOG.debug("Entering...");
		Map<String, String> cacheExpiryTimeMap = new HashMap<>();
		if (null != appConfig) {
			List<CacheConfig> cacheConfigList = appConfig.getCacheConfig();
			if (null != cacheConfigList && cacheConfigList.size() > 0) {
				cacheExpiryTimeMap = cacheConfigList.stream().filter(
						cacheConfig -> cacheConfig.getExpiryTime() != null && cacheConfig.getExpiryTime().length() > 0)
						.collect(Collectors.toMap(x -> x.getName(), x -> x.getExpiryTime()));
			}
		}
		LOG.debug("Leaving.");
		return cacheExpiryTimeMap;
	}
}
