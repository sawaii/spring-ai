package com.springai.mobile.automation.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Web configuration for the application
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.automation.screenshot.directory:./screenshots}")
    private String screenshotDirectory;
    
    /**
     * Configure resource handlers for serving screenshots and other static resources
     * @param registry the resource handler registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve screenshots from the configured directory
        Path screenshotPath = Paths.get(screenshotDirectory);
        String screenshotLocation = screenshotPath.toAbsolutePath().toString();
        
        registry.addResourceHandler("/screenshots/**")
                .addResourceLocations("file:" + screenshotLocation + "/")
                .setCachePeriod(3600);
        
        // Serve static resources from the classpath
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600);
    }
} 