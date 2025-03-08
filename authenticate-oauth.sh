#!/bin/bash

# ================================================================
# OAuth 2.0 Authentication Flow Script for Google Cloud APIs
# ================================================================

# Check if OAuth credentials file exists
OAUTH_CREDS="oauth-credentials.json"
if [ ! -f "$OAUTH_CREDS" ]; then
    echo "Error: OAuth credentials file not found at: $OAUTH_CREDS"
    echo "Please create your OAuth credentials in Google Cloud Console:"
    echo "1. Go to https://console.cloud.google.com/apis/credentials"
    echo "2. Click 'Create Credentials' > 'OAuth client ID'"
    echo "3. Select 'Desktop app' as application type"
    echo "4. Download the JSON file and save it as $OAUTH_CREDS in this directory"
    exit 1
fi

# Compile the project with specific focus on the authentication helper
echo "Compiling project..."
mvn clean compile

# Verify the class exists in the expected location
CLASS_PATH="target/classes/com/springai/mobile/automation/util/OAuthAuthenticationHelper.class"
if [ ! -f "$CLASS_PATH" ]; then
    echo "Error: OAuthAuthenticationHelper class not found at: $CLASS_PATH"
    echo "Please ensure the class is properly defined and the package structure is correct."
    exit 1
fi

echo "Running OAuth 2.0 authentication flow..."
# Use a more direct approach to run the Java class
mvn exec:java \
  -Dexec.mainClass="com.springai.mobile.automation.util.OAuthAuthenticationHelper" \
  -Dexec.args="$OAUTH_CREDS" \
  -Dexec.classpathScope=runtime

# If the above method fails, try an alternative approach using direct Java execution
if [ $? -ne 0 ]; then
    echo "Maven exec failed, trying direct Java execution..."
    java -cp "target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" \
        com.springai.mobile.automation.util.OAuthAuthenticationHelper "$OAUTH_CREDS"
    EXIT_CODE=$?
else
    EXIT_CODE=0
fi

# Check if authentication was successful
if [ $EXIT_CODE -eq 0 ]; then
    echo "OAuth authentication completed successfully."
    echo "You can now run the application with OAuth 2.0 authentication."
    echo "Use ./run-web-prod.sh to run the application."
else
    echo "OAuth authentication failed. Please try again."
    exit 1
fi 