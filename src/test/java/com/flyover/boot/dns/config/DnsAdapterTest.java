/**
 * 
 */
package com.flyover.boot.dns.config;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author mramach
 *
 */
public class DnsAdapterTest {
	
	@Test
	public void testGetValue() throws Exception {
		
		DnsTxtConfiguration configuration = new DnsTxtConfiguration();
		configuration.setSuffix("google.com");
		
		DnsAdapter adapter = new DnsAdapter(configuration);
		
		String result = adapter.getValue("_spf");
		
		assertNotNull("The record lookup result is null.", result);
		
		assertEquals("The resord result does not have the expected text.", 
				"v=spf1 include:_netblocks.google.com include:_netblocks2.google.com include:_netblocks3.google.com ~all", result);
		
	}
	
	@Test(expected = RuntimeException.class)
	public void testGetValue_DoesNotExist() throws Exception {
		
		DnsTxtConfiguration configuration = new DnsTxtConfiguration();
		configuration.setSuffix("google.com");
		
		DnsAdapter adapter = new DnsAdapter(configuration);
		
		adapter.getValue("_i_do_not_exist");
		
	}

}
