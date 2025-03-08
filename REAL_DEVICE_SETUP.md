# Real Device Testing Setup Guide

This guide will help you set up the Spring AI Mobile Automation Framework to run on a real Android device with Vertex AI integration.

## Prerequisites

- Android device with USB debugging enabled
- Android SDK and Platform Tools installed
- Appium Server installed (version 2.0 or higher)
- Google Cloud account with Vertex AI API enabled
- Service account with Vertex AI permissions

## Step 1: Set Up Google Cloud Authentication

1. Create a service account in Google Cloud Console with Vertex AI permissions
2. Download the JSON key file for the service account
3. Update `run-web-prod.sh` with your Google Cloud details:
   - Set `VERTEX_AI_PROJECT_ID` to your project ID
   - Set `VERTEX_AI_LOCATION` (typically "us-central1")
   - Set `GOOGLE_APPLICATION_CREDENTIALS` to the full path of your service account key file
   - (Optional) Set `SPRING_AI_VERTEX_AI_GEMINI_API_KEY` if using API key authentication

## Step 2: Configure Appium for Real Device

1. Connect your Android device via USB and ensure USB debugging is enabled
2. Verify the device is recognized by running `adb devices` in the terminal
3. Start the Appium server (default URL: http://localhost:4723)
4. Update `application.properties` with your real device settings:

```properties
# Appium Configuration
app.automation.appium.url=http://localhost:4723
app.automation.device.name=<Your Device Name from adb devices>
app.automation.app.package=<Your Android App Package>
app.automation.app.activity=<Your Android App Main Activity>
app.automation.implicit.wait=10
```

You can find your app's package and activity using this command:
```
adb shell dumpsys window | grep -E 'mCurrentFocus|mFocusedApp'
```

## Step 3: Run the Application

1. Make the run script executable:
   ```
   chmod +x run-web-prod.sh
   ```

2. Run the application:
   ```
   ./run-web-prod.sh
   ```

## Troubleshooting

### Authentication Issues

If you encounter Vertex AI authentication errors:
- Verify your service account has the necessary permissions
- Check that the path to your credentials file is correct
- Make sure your project has Vertex AI API enabled

### Appium Connection Issues

If you see "Connection refused" errors:
- Ensure Appium server is running
- Verify the correct port is configured (default: 4723)
- Check firewall settings

### Device Not Found

If the device is not recognized:
- Verify USB debugging is enabled
- Try a different USB cable or port
- Run `adb kill-server` followed by `adb start-server`
- Check device name matches what's shown in `adb devices`

## Logs and Debugging

Check logs for detailed error information:
- Application logs: `./logs/spring-ai-mobile-automation.log`
- Appium server logs (from your Appium server terminal)
- Android device logs: `adb logcat` 