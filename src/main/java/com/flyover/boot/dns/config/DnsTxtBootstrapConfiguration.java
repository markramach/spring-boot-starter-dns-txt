/**
 * 
 */
package com.flyover.boot.dns.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author mramach
 *
 */
@Configuration
@EnableConfigurationProperties
public class DnsTxtBootstrapConfiguration {
    
    @Autowired
    private Environment environment;
    
    @Bean
    @ConditionalOnProperty(value = "dns.txt.enabled", matchIfMissing = true)
    public DnsTxtConfiguration dnsTxtConfiguration() {
        return new DnsTxtConfiguration();
    }
    
    @Bean 
    @ConditionalOnProperty(value = "dns.txt.enabled", matchIfMissing = true)
    public DnsAdapter dnsAdapter(DnsTxtConfiguration configuration) {
        return new DnsAdapter(configuration);
    }

    @Bean
    @ConditionalOnProperty(value = "dns.txt.enabled", matchIfMissing = true)
    public DnsTxtPropertySourceLocator dnsTxtPropertySourceLocator() {
        return new DnsTxtPropertySourceLocator();
    }
    
}
