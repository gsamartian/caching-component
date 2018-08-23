package com.demo.caching.util;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

public class CloudUtils {
	
	
	@Value("${spring.application.name}")
	private String appName;

	@Autowired
	private DiscoveryClient discoveryClient;

	public List<ServiceInstance> getServiceInstances() {
	    List<ServiceInstance> list = discoveryClient.getInstances(appName);
	    return list;
	}

	public String getTokenForEnvironment() {
		return "token";
	}
}
