/**
 * 
 */
package com.flyover.boot.dns.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.EnumerablePropertySource;

/**
 * @author mramach
 *
 */
public class DnsTxtPropertySource extends EnumerablePropertySource<Map<String, Object>> {
    
    private static final Logger LOG = LoggerFactory.getLogger(DnsTxtPropertySourceLocator.class);
    
    private Map<String, Object> source = new LinkedHashMap<String, Object>();

    public DnsTxtPropertySource(DnsTxtConfiguration configuration, DnsAdapter adapter) {
        
        super("dns-txt-property-source-" + UUID.randomUUID().toString());
        
        try {
            
            configuration.getRecords().stream()
                .forEach(r -> setProperty(r, adapter.getValue(r)));
            
        } catch (RuntimeException e) {
            
            if(configuration.isFailFast()) {
                throw e;
            }
            
            LOG.info("Unable to fetch properties from dns.");
            
        } 
        
    }

    private Object setProperty(String key, String value) {
        return source.put(pathToProperty(key), value);
    }
    
    private String pathToProperty(String path) {
        return path.replace('/', '.');
    }
    
    @Override
    public String[] getPropertyNames() {
        return source.keySet().toArray(new String[source.size()]);
    }

    @Override
    public Object getProperty(String name) {
        return source.get(name);
    }

}
