package com.springai.mobile.automation.service.mobile;

import com.springai.mobile.automation.model.TestAction;
import com.springai.mobile.automation.service.ai.ScreenAnalyzerService;
import com.springai.mobile.automation.service.learning.LearningService;
import io.appium.java_client.android.AndroidDriver;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Service for executing mobile automation actions via Appium
 */
@Service
public class MobileAutomationService {

    private final ApplicationContext context;
    private final ScreenAnalyzerService screenAnalyzerService;
    private final LearningService learningService;
    private AndroidDriver driver;
    
    @Value("${app.automation.execution.timeout:30000}")
    private long executionTimeoutMs;
    
    @Value("${app.automation.screenshot.directory:./screenshots}")
    private String screenshotDirectory;

    @Autowired
    public MobileAutomationService(ApplicationContext context, 
            ScreenAnalyzerService screenAnalyzerService,
            LearningService learningService) {
        this.context = context;
        this.screenAnalyzerService = screenAnalyzerService;
        this.learningService = learningService;
    }

    /**
     * Initialize the AndroidDriver
     * @throws Exception if driver initialization fails
     */
    public void initializeDriver() throws Exception {
        if (driver == null || driver.getSessionId() == null) {
            driver = context.getBean(AndroidDriver.class);
        }
    }
    
    /**
     * Execute a test action
     * @param testAction the test action to execute
     * @return true if execution was successful
     */
    public boolean executeAction(TestAction testAction) {
        try {
            // Take screenshot before action
            File screenshot = takeScreenshot("before_action_" + testAction.getId() + ".png");
            
            // Analyze screenshot to find element if needed
            if (needsElementAnalysis(testAction)) {
                Map<String, Object> elementInfo = screenAnalyzerService.analyzeScreenshot(screenshot, testAction);
                
                // Extract element locator from AI analysis
                String screenDescription = (String) elementInfo.get("screenDescription");
                Map<String, Object> matchedElement = (Map<String, Object>) elementInfo.get("matchedElement");
                Map<String, String> suggestedLocators = (Map<String, String>) matchedElement.get("suggestedLocators");
                
                // Check if we have learned anything about this element before
                Optional<String> learnedCorrection = learningService.getPastCorrection(testAction, screenDescription);
                if (learnedCorrection.isPresent()) {
                    // Apply correction from past learning
                    testAction.setElementLocator(learnedCorrection.get());
                } else {
                    // Use AI-suggested locator
                    String bestLocator = suggestedLocators.get("xpath");
                    testAction.setElementLocator(bestLocator);
                }
                
                // Store element identifiers for learning
                testAction.setElementDescription(matchedElement.get("description").toString());
            }
            
            // Execute the action based on type
            switch (testAction.getActionType()) {
                case TAP:
                    tap(testAction);
                    break;
                case LONG_PRESS:
                    longPress(testAction);
                    break;
                case TYPE:
                    typeText(testAction);
                    break;
                case CLEAR:
                    clearText(testAction);
                    break;
                case SWIPE:
                    swipe(testAction);
                    break;
                case SCROLL:
                    scroll(testAction);
                    break;
                case BACK:
                    driver.navigate().back();
                    break;
                case VERIFY_TEXT:
                    verifyText(testAction);
                    break;
                case VERIFY_ELEMENT:
                    verifyElement(testAction);
                    break;
                case WAIT:
                    wait(testAction);
                    break;
                case LAUNCH_APP:
                    driver.activateApp(testAction.getValue());
                    break;
                case CLOSE_APP:
                    driver.terminateApp(testAction.getValue());
                    break;
                case TAKE_SCREENSHOT:
                    takeScreenshot("manual_" + testAction.getValue() + ".png");
                    break;
                default:
                    throw new UnsupportedOperationException("Action type not implemented: " + testAction.getActionType());
            }
            
            // Record successful execution
            testAction.setSuccessful(true);
            testAction.setExecutedAt(LocalDateTime.now());
            
            // Take screenshot after action
            File afterScreenshot = takeScreenshot("after_action_" + testAction.getId() + ".png");
            testAction.setScreenshot(afterScreenshot.getName());
            
            // Learn from successful action
            learningService.learnFromAction(testAction, "Current Screen", 
                    testAction.getElementLocator(), null, null);
            
            return true;
        } catch (Exception e) {
            // Record failure
            testAction.setSuccessful(false);
            testAction.setErrorMessage(e.getMessage());
            testAction.setExecutedAt(LocalDateTime.now());
            
            // Take screenshot of failure
            try {
                File failureScreenshot = takeScreenshot("failure_" + testAction.getId() + ".png");
                testAction.setScreenshot(failureScreenshot.getName());
            } catch (Exception screenshotError) {
                // Ignore screenshot errors
            }
            
            // Learn from failure
            learningService.learnFromAction(testAction, "Current Screen", 
                    testAction.getElementLocator(), e.getMessage(), null);
            
            return false;
        }
    }
    
    /**
     * Check if the action needs element analysis before execution
     * @param testAction the test action
     * @return true if element analysis is needed
     */
    private boolean needsElementAnalysis(TestAction testAction) {
        // Actions that require element analysis
        return testAction.getElementLocator() == null && 
               (testAction.getActionType() == TestAction.ActionType.TAP ||
                testAction.getActionType() == TestAction.ActionType.LONG_PRESS ||
                testAction.getActionType() == TestAction.ActionType.TYPE ||
                testAction.getActionType() == TestAction.ActionType.CLEAR ||
                testAction.getActionType() == TestAction.ActionType.VERIFY_TEXT ||
                testAction.getActionType() == TestAction.ActionType.VERIFY_ELEMENT);
    }
    
    /**
     * Take a screenshot
     * @param filename the name of the screenshot file
     * @return the screenshot file
     * @throws IOException if screenshot capture fails
     */
    private File takeScreenshot(String filename) throws IOException {
        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        File targetFile = new File(screenshotDirectory, filename);
        FileUtils.copyFile(screenshot, targetFile);
        return targetFile;
    }
    
    /**
     * Find an element with explicit wait
     * @param locator the element locator
     * @return the WebElement found
     */
    private WebElement findElement(String locator) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(executionTimeoutMs));
        return wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(locator)));
    }
    
    /**
     * Tap on an element
     * @param action the test action containing element info
     */
    private void tap(TestAction action) {
        WebElement element = findElement(action.getElementLocator());
        element.click();
    }
    
    /**
     * Long press on an element
     * @param action the test action containing element info
     */
    private void longPress(TestAction action) {
        WebElement element = findElement(action.getElementLocator());
        
        Point location = element.getLocation();
        Dimension size = element.getSize();
        Point center = new Point(location.getX() + size.getWidth() / 2, 
                                location.getY() + size.getHeight() / 2);
        
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence sequence = new Sequence(finger, 0);
        
        sequence.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), center.getX(), center.getY()));
        sequence.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        // Use Thread.sleep instead of createPause which may not be available in all versions
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        sequence.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        
        driver.perform(Collections.singletonList(sequence));
    }
    
    /**
     * Type text into an element
     * @param action the test action containing element and text info
     */
    private void typeText(TestAction action) {
        WebElement element = findElement(action.getElementLocator());
        element.sendKeys(action.getValue());
    }
    
    /**
     * Clear text from an element
     * @param action the test action containing element info
     */
    private void clearText(TestAction action) {
        WebElement element = findElement(action.getElementLocator());
        element.clear();
    }
    
    /**
     * Swipe on the screen
     * @param action the test action containing swipe direction
     */
    private void swipe(TestAction action) {
        Dimension size = driver.manage().window().getSize();
        int startX = size.width / 2;
        int startY = size.height / 2;
        int endX = startX;
        int endY = startY;
        
        // Parse direction from action value
        String direction = action.getValue().toUpperCase();
        switch (direction) {
            case "UP":
                endY = (int) (startY * 0.3);
                break;
            case "DOWN":
                endY = (int) (startY * 1.7);
                break;
            case "LEFT":
                endX = (int) (startX * 0.3);
                break;
            case "RIGHT":
                endX = (int) (startX * 1.7);
                break;
        }
        
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence sequence = new Sequence(finger, 0);
        
        sequence.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, startY));
        sequence.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        sequence.addAction(finger.createPointerMove(Duration.ofMillis(300), PointerInput.Origin.viewport(), endX, endY));
        sequence.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        
        driver.perform(Collections.singletonList(sequence));
    }
    
    /**
     * Scroll on the screen
     * @param action the test action containing scroll direction
     */
    private void scroll(TestAction action) {
        // Scroll is similar to swipe but slower
        Dimension size = driver.manage().window().getSize();
        int startX = size.width / 2;
        int startY = size.height / 2;
        int endX = startX;
        int endY = startY;
        
        // Parse direction from action value
        String direction = action.getValue().toUpperCase();
        switch (direction) {
            case "UP":
                endY = (int) (startY * 0.3);
                break;
            case "DOWN":
                endY = (int) (startY * 1.7);
                break;
            case "LEFT":
                endX = (int) (startX * 0.3);
                break;
            case "RIGHT":
                endX = (int) (startX * 1.7);
                break;
        }
        
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence sequence = new Sequence(finger, 0);
        
        sequence.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, startY));
        sequence.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        sequence.addAction(finger.createPointerMove(Duration.ofMillis(800), PointerInput.Origin.viewport(), endX, endY));
        sequence.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        
        driver.perform(Collections.singletonList(sequence));
    }
    
    /**
     * Verify that specific text is present
     * @param action the test action containing text to verify
     * @throws AssertionError if verification fails
     */
    private void verifyText(TestAction action) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(executionTimeoutMs));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(@text,'" + action.getValue() + "')]")));
    }
    
    /**
     * Verify that an element is present
     * @param action the test action containing element info
     * @throws AssertionError if verification fails
     */
    private void verifyElement(TestAction action) {
        findElement(action.getElementLocator());
    }
    
    /**
     * Wait for specified amount of time
     * @param action the test action containing wait time in milliseconds
     * @throws InterruptedException if wait is interrupted
     */
    private void wait(TestAction action) throws InterruptedException {
        int waitTime = Integer.parseInt(action.getValue());
        Thread.sleep(waitTime);
    }
    
    /**
     * Cleanup resources when done
     */
    public void cleanup() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }
} 