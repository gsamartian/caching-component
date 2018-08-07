package com.demo.caching.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import com.demo.caching.config.AppConfig;
import com.demo.caching.config.CacheConfig;

@Configuration
@EnableScheduling
public class CacheScheduledTask implements SchedulingConfigurer {
	
	private final Logger LOG = LoggerFactory.getLogger(getClass());
	
	
	@Autowired
	private CacheManager cacheManager;

	@Autowired
	private AppConfig appConfig;
	
	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		LOG.info("Adding Fixed Task...");
		
		Map<String, String> cacheExpiryTimeMap=getCacheExpiryTimeMap(appConfig);
		if(null!=cacheExpiryTimeMap && cacheExpiryTimeMap.size()>0) {
			cacheExpiryTimeMap.forEach((k, v) -> { 
				//LOG.info(k + ":" + v);
				CronTask cronTask=new CronTask(new CacheClearTask(cacheManager,k),v);
				taskRegistrar.addCronTask(cronTask);
				LOG.info("Added Cron Task for cache: {} with CRON: {}",k,v);
				/*IntervalTask task = new IntervalTask(new CacheClearTask(cacheManager,k), toLong(v));
				taskRegistrar.addFixedRateTask(task);
				LOG.info("Added Fixed Taskfor cache: {} with fixedtime: {}",k,v);*/
			});
		}
		

	}
	public Long toLong(String value) {
		return Long.parseLong(value) * 1000;
	}
	private Map<String, String> getCacheExpiryTimeMap(AppConfig appConfig) {
		Map<String, String> cacheExpiryTimeMap=new HashMap<>();
		if(null!=appConfig) {
			List<CacheConfig> cacheConfigList = appConfig.getCacheConfig();
			if (null != cacheConfigList && cacheConfigList.size()>0) {
				cacheExpiryTimeMap = cacheConfigList.stream().filter(
						cacheConfig -> cacheConfig.getExpiryTime() != null && cacheConfig.getExpiryTime().length() > 0)
						.collect(Collectors.toMap(x -> x.getName(), x -> x.getExpiryTime()));
			}
		}
		return cacheExpiryTimeMap;
	}
}
