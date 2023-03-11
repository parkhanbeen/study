package com.helloboot.parkhanbeen.config.autoconfig;

import org.springframework.beans.factory.annotation.Value;

public class ServerProperties {

    @Value("${contextPath}")
    private String contextPath;

    @Value("${port:8080}")
    private int port;

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
