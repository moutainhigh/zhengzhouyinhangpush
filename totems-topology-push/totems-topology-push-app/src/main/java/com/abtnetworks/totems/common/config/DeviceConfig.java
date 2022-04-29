package com.abtnetworks.totems.common.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Data
@Configuration
@ConfigurationProperties(prefix = "vendor")
public class DeviceConfig {

    private Map<String, String> vendorSecurityGeneratorMap;

    private Map<String, List<String>> vendorSecurityProcedureMap;

    private Map<String, String> vendorNatGeneratorMap;

    private Map<String, List<String>> vendorNatProcedureMap;
}


