/**
 * 
 */
package com.flyover.boot.dns.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

/**
 * @author mramach
 *
 */
public class DnsTxtPropertySourceLocator implements PropertySourceLocator {
    
    @Autowired
    private DnsTxtConfiguration configuration;
    @Autowired
    private DnsAdapter dnsAdapter;
    
    @Override
    public PropertySource<?> locate(Environment environment) {
        return new DnsTxtPropertySource(configuration, dnsAdapter);
    }

}
