/**
 * 
 */
package com.flyover.boot.dns.config;

import java.util.LinkedList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author mramach
 *
 */
@ConfigurationProperties(DnsTxtConfiguration.PREFIX)
public class DnsTxtConfiguration {
    
    public static final String PREFIX = "dns";
    public static final int DEFAULT_PORT = 53;
    
    private boolean failFast = false;
    private String suffix = "default.skydns.local";
    private List<String> records = new LinkedList<String>();
    private String endpoint;
    private int port = DEFAULT_PORT;

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	public List<String> getRecords() {
		return records;
	}

	public void setRecords(List<String> records) {
		this.records = records;
	}

	public boolean isFailFast() {
		return failFast;
	}

	public void setFailFast(boolean failFast) {
		this.failFast = failFast;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

}
