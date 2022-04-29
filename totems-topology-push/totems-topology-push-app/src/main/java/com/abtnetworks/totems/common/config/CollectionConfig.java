package com.abtnetworks.totems.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: hw
 * @Date: 2018/8/13 16:08
 */
@Component
@ConfigurationProperties(prefix = "manufacturer")
public class CollectionConfig {

    private final Map<String, String> map = new HashMap<>();

    public Map<String, String> getMap() {
        return this.map;
    }

}
