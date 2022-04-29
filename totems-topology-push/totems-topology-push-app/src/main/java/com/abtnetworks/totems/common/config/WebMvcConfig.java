package com.abtnetworks.totems.common.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Author: hw
 * @Date: 2018/10/25 11:18
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${resourceHandler}")
    private String resourceHandler;

    @Value("${resourceLocation}")
    private String resourceLocations;

    @Value("${virtual-directory.handler}")
    private String virtualDirectoryHandler;

    @Value("${virtual-directory.location}")
    private String virtualDirectoryLocations;

    @Value("${translation-directory.handler}")
    private String translationDirectoryHandler;

    @Value("${translation-directory.location}")
    private String translationDirectoryLocations;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(resourceHandler).addResourceLocations(resourceLocations);
        registry.addResourceHandler(virtualDirectoryHandler+"**").addResourceLocations(virtualDirectoryLocations);
        registry.addResourceHandler(translationDirectoryHandler+"**").addResourceLocations(translationDirectoryLocations);
    }

}
