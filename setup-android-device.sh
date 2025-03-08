#!/bin/bash

# Check if ADB is installed
if ! command -v adb &> /dev/null; then
    echo "Error: ADB not found. Please install Android SDK Platform Tools."
    exit 1
fi

# Check if device is connected
echo "Checking for connected Android devices..."
DEVICES=$(adb devices | grep -v "List" | grep "device$")

if [ -z "$DEVICES" ]; then
    echo "Error: No Android devices connected. Please connect a device with USB debugging enabled."
    exit 1
fi

echo "Found the following devices:"
adb devices | grep -v "List"
echo ""

# Get device details
echo "Retrieving device details..."
DEVICE_NAME=$(adb shell getprop ro.product.model)
DEVICE_ID=$(adb devices | grep -v "List" | grep "device$" | cut -f1)

# Get current app package and activity
echo "Getting current app package and activity..."
CURRENT_APP=$(adb shell dumpsys window | grep -E 'mCurrentFocus|mFocusedApp' | head -n1)
echo "Current app: $CURRENT_APP"

# Extract package and activity
if [[ $CURRENT_APP =~ ([a-zA-Z0-9.]+)/([a-zA-Z0-9.]+) ]]; then
    PACKAGE="${BASH_REMATCH[1]}"
    ACTIVITY="${BASH_REMATCH[2]}"
    
    if [[ $ACTIVITY == *"." ]]; then
        ACTIVITY="${PACKAGE}.${ACTIVITY}"
    elif [[ $ACTIVITY != *"." && $ACTIVITY != $PACKAGE* ]]; then
        ACTIVITY="${PACKAGE}.${ACTIVITY}"
    fi
    
    echo "Found package: $PACKAGE"
    echo "Found activity: $ACTIVITY"
else
    echo "Could not determine package and activity. Please launch your app first."
    PACKAGE=""
    ACTIVITY=""
fi

# Create properties file
cat > device-profile.properties << EOF
# Android Device Profile
# Generated on $(date)

# Device Information
app.automation.device.name=$DEVICE_ID
app.automation.appium.url=http://localhost:4355

# Application Information
app.automation.app.package=$PACKAGE
app.automation.app.activity=$ACTIVITY
app.automation.implicit.wait=10

# Additional capabilities
# Uncomment and modify as needed
# app.automation.no.reset=true
# app.automation.full.reset=false
EOF

echo ""
echo "Device profile created as device-profile.properties"
echo "To use these settings, copy the properties to your application.properties file"
echo "Or use them with Spring Boot's external configuration options." 