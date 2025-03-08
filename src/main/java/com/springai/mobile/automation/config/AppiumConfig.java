package com.springai.mobile.automation.config;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.remote.MobileCapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

/**
 * Configuration class for Appium driver and related settings
 */
@Configuration
public class AppiumConfig {

    @Value("${app.automation.appium.url:http://localhost:4355}")
    private String appiumUrl;

    @Value("${app.automation.device.name:Android Device}")
    private String deviceName;

    @Value("${app.automation.app.package:}")
    private String appPackage;

    @Value("${app.automation.app.activity:}")
    private String appActivity;

    @Value("${app.automation.implicit.wait:10}")
    private int implicitWaitSeconds;

    /**
     * Create and configure AndroidDriver with desired capabilities
     * @return AndroidDriver instance
     * @throws MalformedURLException if the Appium URL is invalid
     */
    @Bean
    @Scope("prototype")
    public AndroidDriver androidDriver() throws MalformedURLException {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, "Android");
        capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, deviceName);
        capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, "UiAutomator2");
        capabilities.setCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, 300);
        
        // Set app package and activity if provided
        if (appPackage != null && !appPackage.isEmpty()) {
            capabilities.setCapability("appPackage", appPackage);
        }
        
        if (appActivity != null && !appActivity.isEmpty()) {
            capabilities.setCapability("appActivity", appActivity);
        }
        
        AndroidDriver driver = new AndroidDriver(new URL(appiumUrl), capabilities);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWaitSeconds));
        
        return driver;
    }
} 