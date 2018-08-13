/**
 * 
 */
package com.flyover.boot.dns.config;

import java.net.UnknownHostException;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TXTRecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

/**
 * @author mramach
 *
 */
public class DnsAdapter {
    
    @Autowired
    private DnsTxtConfiguration configuration;
    
    public DnsAdapter(DnsTxtConfiguration configuration) {
        this.configuration = configuration;
    }

    public String getValue(String path) {
     
    	String fqdn = String.format("%s.%s", path, configuration.getSuffix());
    	
    	try {
			
    		try {
				
    			if(StringUtils.hasText(configuration.getEndpoint())) {
				
					SimpleResolver resolver = new SimpleResolver(configuration.getEndpoint());
					resolver.setPort(5003);
					
					Lookup.setDefaultResolver(resolver);
					
				}
				
			} catch (UnknownHostException e) {
				throw new RuntimeException("failed to configure dns resolver", e);
			}
    		
    		Lookup lookup = new Lookup(fqdn, Type.TXT);
			Record[] result = lookup.run();
			
			if(Lookup.SUCCESSFUL != lookup.getResult()) {
				throw new RuntimeException(String.format("failed during lookup txt of record %s", fqdn));
			}
			
			return Arrays.stream(result).map(r -> {
				
				TXTRecord txt = (TXTRecord)r;
				
				return (String)txt.getStrings().get(0);
				
			}).findFirst().get();
			
			
		} catch (TextParseException e) {
			throw new RuntimeException(String.format("failed to execute lookup of txt record %s", fqdn));
		}
    	
    }
    
}
