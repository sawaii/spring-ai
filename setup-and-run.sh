#!/bin/bash

# Master script to set up real device testing environment
# and run the Spring AI Mobile Automation Framework

# Function to check if a command exists
command_exists() {
  command -v "$1" >/dev/null 2>&1
}

# Check for required tools
echo "Checking for required tools..."
if ! command_exists adb; then
  echo "Error: ADB not found. Please install Android SDK Platform Tools."
  exit 1
fi

if ! command_exists appium; then
  echo "Error: Appium not found. Please install Appium first:"
  echo "npm install -g appium"
  exit 1
fi

if ! command_exists mvn; then
  echo "Error: Maven not found. Please install Maven first."
  exit 1
fi

# Check for Google Cloud credentials
if [ -z "$GOOGLE_APPLICATION_CREDENTIALS" ]; then
  echo "Warning: GOOGLE_APPLICATION_CREDENTIALS environment variable not set."
  echo "Vertex AI integration will likely fail without proper authentication."
  read -p "Continue anyway? (y/n): " continue_anyway
  if [ "$continue_anyway" != "y" ]; then
    exit 1
  fi
fi

# Check if Android device is connected
echo "Checking for connected Android devices..."
DEVICES=$(adb devices | grep -v "List" | grep "device$")

if [ -z "$DEVICES" ]; then
  echo "Error: No Android devices connected."
  echo "Please connect a device with USB debugging enabled."
  exit 1
fi

echo "Found the following devices:"
adb devices | grep -v "List"
echo ""

# Generate device profile
echo "Generating device profile..."
./setup-android-device.sh

# Load device profile properties
echo "Loading device profile..."
if [ -f device-profile.properties ]; then
  # Source the properties file
  # This is a simplistic approach - in production, use proper parsing
  source <(grep -v '^#' device-profile.properties | sed -e 's/=/ /' | sed -e 's/^/export /')
  echo "Device profile loaded successfully."
else
  echo "Error: Could not find device-profile.properties"
  exit 1
fi

# Start Appium server in the background
echo "Starting Appium server in the background..."
./start-appium.sh &
APPIUM_PID=$!

# Give Appium time to start
echo "Waiting for Appium server to start..."
sleep 5

# Check if Appium is running
if ! pgrep -f "appium" > /dev/null; then
  echo "Error: Appium server failed to start."
  exit 1
fi

echo "Appium server started successfully."

# Get real Vertex AI credentials
if [ -f "run-web-prod.sh" ]; then
  echo "Running with real Vertex AI integration..."
  chmod +x run-web-prod.sh
  
  # Run the application with real Vertex AI
  ./run-web-prod.sh
else
  echo "Error: run-web-prod.sh not found."
  echo "Please create this script with your Vertex AI credentials."
  exit 1
fi

# When the application stops, clean up
echo "Application stopped. Cleaning up..."
if [ -n "$APPIUM_PID" ]; then
  echo "Stopping Appium server..."
  kill $APPIUM_PID
fi

echo "Setup and run process completed." 