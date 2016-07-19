/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.ib.caonpd.cakeshop.test;

import com.jpmorgan.ib.caonpd.cakeshop.error.APIException;
import com.jpmorgan.ib.caonpd.cakeshop.model.Contract;
import com.jpmorgan.ib.caonpd.cakeshop.service.ContractRegistryService;
import com.jpmorgan.ib.caonpd.cakeshop.test.config.TestAppConfig;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheFactoryBean;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import org.testng.Reporter;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 *
 * @author I629630
 */


public class ContractRegisrtryCacheTest extends BaseGethRpcTest {
    
	@Autowired
	CacheManager manager;
	
    @Autowired
    ContractRegistryService contractRegistry;
    
    
    
    @Configuration
	@EnableCaching
	public static class TestConfiguration {
		
		@Bean
		public SimpleCacheManager cacheManager(){
			SimpleCacheManager cacheManager = new SimpleCacheManager();
			List<Cache> caches = new ArrayList<>();
			caches.add(cacheBean().getObject());
			cacheManager.setCaches(caches );
			return cacheManager;
		}
		
		@Bean
		public ConcurrentMapCacheFactoryBean cacheBean(){
			ConcurrentMapCacheFactoryBean cacheFactoryBean = new ConcurrentMapCacheFactoryBean();
			cacheFactoryBean.setName("contracts");
			return cacheFactoryBean;
		}
}
    
    
    @Test
	public void testCache() throws IOException, InterruptedException, APIException  {

		String addr = "0x1234567890";

	    Contract first;
        
        first = contractRegistry.getById(addr);

	    Reporter.log("#################### " + manager.getCache("contracts").get(addr), true);

	    // First invocation returns object returned by the method
	    Contract result = contractRegistry.getById(addr);
	    assertEquals(result, first);

	    Reporter.log("#################### " + manager.getCache("contracts").get(addr), true);

	    // Second invocation should return cached value, *not* second (as set up above)
	    result = contractRegistry.getById(addr);
	    assertEquals(result, first);
        
        
        //third invocation
        result = contractRegistry.getById(addr);
	    assertEquals(result, first);
        

	    // Verify repository method was invoked once
	    assertNotNull(manager.getCache("contracts").get(addr));
	}
}
