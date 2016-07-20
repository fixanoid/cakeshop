/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.ib.caonpd.cakeshop.test;

import com.jpmorgan.ib.caonpd.cakeshop.error.APIException;
import com.jpmorgan.ib.caonpd.cakeshop.model.Contract;
import com.jpmorgan.ib.caonpd.cakeshop.service.ContractRegistryService;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import static org.testng.Assert.assertEquals;
import org.testng.Reporter;
import org.testng.annotations.Test;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

/**
 *
 * @author I629630
 */


public class ContractRegistryCacheTest extends BaseGethRpcTest {
    
	@Autowired
    @Qualifier("cacheManager")
	CacheManager manager;
	
    @Autowired
    ContractRegistryService contractRegistry;
    
    
    @Test
	public void testCache() throws IOException, InterruptedException, APIException  {

		Contract first = contractRegistry.list().get(0);
        assertNotNull(first);

        String addr = first.getAddress();

	    // First invocation returns object returned by the method
	    Contract result = contractRegistry.getById(addr);
	    assertEquals(result.getAddress(), first.getAddress());
	    Reporter.log("######### " + manager.getCache("contracts").get(addr).get(), true);

	    // Second invocation should return cached value
	    Contract result2 = contractRegistry.getById(addr);
	    assertEquals(result2.getAddress(), first.getAddress());
        Reporter.log("######### " + manager.getCache("contracts").get(addr).get(), true);

	    // Verify repository method was invoked once
	    assertNotNull(manager.getCache("contracts").get(addr).get());
        
        addr = "0x1234567890";
        
        //first invocation with forced null value. Should be null in the cache
        result = contractRegistry.getById(addr);
        assertNull(result);
        Reporter.log("######### WITH FORCED NULL VALUE " + manager.getCache("contracts").get(addr), true);
        
        //second invocation with forced null value. Should still be null in the cache
        result = contractRegistry.getById(addr);
        assertNull(result);
        Reporter.log("######### WITH FORCED NULL VALUE " + manager.getCache("contracts").get(addr), true);
        assertNull(manager.getCache("contracts").get(addr));
	}
}
