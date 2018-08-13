/**
 * 
 */
package com.flyover.boot.dns.config;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author mramach
 *
 */
public class DnsTxtBootstrapConfigurationTest {
    
    @Test
    public void testVaultPropertySourceLocatorCreated() {

        ConfigurableApplicationContext context = new SpringApplicationBuilder(
                DnsTxtBootstrapConfiguration.class).web(WebApplicationType.NONE).run();
        
        assertNotNull("Checking that the DnsAdapter adapter is available in the application context.",
                BeanFactoryUtils.beanOfType(context, DnsAdapter.class));
        
        assertNotNull("Checking that the property source locator is available in the application context.",
                BeanFactoryUtils.beanOfType(context, DnsTxtPropertySourceLocator.class));
        
    }
    
    @Test
    public void testVaultPropertiesCreated_WithDefaultProperties() {
        
        ConfigurableApplicationContext context = new SpringApplicationBuilder(
        		DnsTxtBootstrapConfiguration.class).web(WebApplicationType.NONE).run();
        
        DnsTxtConfiguration properties = BeanFactoryUtils.beanOfType(context, DnsTxtConfiguration.class);
        
        assertNotNull("Checking that the DnsAdapter adapter is available in the application context.", properties);
        assertEquals("Checking that the endpoint has been defaulted.", "default.skydns.local", properties.getSuffix());
        assertEquals("Checking that the fast fail option has been defaulted.", false, properties.isFailFast());
        
    }
    
}
