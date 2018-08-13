/**
 * 
 */
package com.flyover.boot.dns.config;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;

/**
 * @author mramach
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class DnsTxtPropertySourceLocatorTest {

    @Spy
    private DnsTxtConfiguration configuration = new DnsTxtConfiguration();
    @Mock
    private DnsAdapter dnsAdapter;
    @InjectMocks
    private DnsTxtPropertySourceLocator locator;
    
    @Test
    public void testLocate() {

        configuration.setRecords(Arrays.asList("env"));
        
        when(dnsAdapter.getValue(isA(String.class))).thenReturn("local");
        
        PropertySource<?> propertySource = locator.locate(new StandardEnvironment());
        
        assertNotNull("Checking that a non-null property sources was returned.", propertySource);
        
        assertTrue("Checking that the property source contains a property.", 
                propertySource.containsProperty("env"));
        
        assertEquals("Checking that the property source contains the expected value.", 
                "local", propertySource.getProperty("env"));
        
    }
    
    @Test(expected = RuntimeException.class)
    public void testFailFast() {

        configuration.setRecords(Arrays.asList("env"));
        configuration.setFailFast(true);
        
        when(dnsAdapter.getValue(isA(String.class))).thenThrow(new RuntimeException("fail"));
        
        locator.locate(new StandardEnvironment());
        
    }

}
