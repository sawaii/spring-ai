#!/bin/bash

# Appium Server starter script
# Uses common best practices for mobile automation testing

# Default Appium settings
PORT=4355
BASE_PATH="/wd/hub"
LOG_LEVEL="info"
LOG_FILE="./appium.log"

# Check if Appium is installed
if ! command -v appium &> /dev/null; then
    echo "Error: Appium not found. Please install Appium first."
    echo "You can install it with: npm install -g appium"
    exit 1
fi

# Check for plugins
PLUGIN_LIST=$(appium plugin list --installed 2>/dev/null)
if [[ ! $PLUGIN_LIST == *"uiautomator2"* ]]; then
    echo "Installing required Appium plugins..."
    appium plugin install uiautomator2
fi

echo "Starting Appium server on port $PORT..."
echo "Logs will be written to $LOG_FILE"

# Start the server
appium \
  --port $PORT \
  --base-path $BASE_PATH \
  --log $LOG_FILE \
  --log-level $LOG_LEVEL \
  --relaxed-security \
  --allow-cors \
  --allow-insecure chromedriver_autodownload \
  --use-drivers=uiautomator2

# If Appium is closed, this will execute
echo "Appium server stopped." 