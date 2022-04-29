package com.abtnetworks.totems.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: myl
 */
@Component
@ConfigurationProperties(prefix = "protocol")
public class ProtocolMapConfig {

    private final Map<String, String> numMap = new HashMap<>();
    private final Map<String, String> strMap = new HashMap<>();

    public Map<String, String> getNumMap() {
        return numMap;
    }

    public Map<String, String> getStrMap() {
        return strMap;
    }
}
