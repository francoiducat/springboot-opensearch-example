package com.ducatillon.opensearch;

import org.springframework.stereotype.Component;

@Component
public class OpenSearchConfiguration {

    private String host = "localhost";
    private String port = "9200";
    private String username = "";
    private String password = "";
    private String protocol = "http";

    public OpenSearchConfiguration() {}

    public OpenSearchConfiguration(String host, String port, String username, String password, String protocol) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.protocol = protocol;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
}
