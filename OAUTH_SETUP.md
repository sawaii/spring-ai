# OAuth 2.0 Authentication Setup Guide

This guide will help you set up OAuth 2.0 authentication for the Spring AI Mobile Automation Framework to authenticate with Google Cloud services, particularly Vertex AI Gemini.

## Prerequisites

- Google Cloud account with Vertex AI API enabled
- OAuth 2.0 client credentials
- Java 17 or higher
- Maven

## Step 1: Create OAuth 2.0 Client Credentials

1. Go to the [Google Cloud Console](https://console.cloud.google.com)
2. Select your project
3. Navigate to **APIs & Services** > **Credentials**
4. Click **Create Credentials** > **OAuth client ID**
5. Select **Desktop app** as the application type
6. Enter a name for your OAuth client (e.g., "Spring AI Mobile Automation")
7. Click **Create**
8. Download the JSON file and save it as `oauth-credentials.json` in your project root directory

## Step 2: Run Authentication Flow

The first time you use OAuth authentication, you need to run the interactive authentication flow to generate tokens:

```bash
./authenticate-oauth.sh
```

This script will:
1. Check if the OAuth credentials file exists
2. Compile the project
3. Run the OAuthAuthenticationHelper class
4. Open a browser window for you to authenticate with your Google account
5. After authentication, store the tokens in the `tokens` directory

## Step 3: Run Application with OAuth Authentication

After completing the authentication flow, you can run the application with:

```bash
./run-web-prod.sh
```

This will use the stored OAuth tokens to authenticate with Vertex AI Gemini.

## How It Works

The OAuth 2.0 authentication process follows these steps:

1. The application loads the OAuth 2.0 client credentials from `oauth-credentials.json`
2. It checks if tokens are already available in the `tokens` directory
3. If tokens exist, it uses the refresh token to obtain a new access token
4. If no tokens exist, it prompts for interactive authentication
5. The OAuth tokens are used to authenticate API requests to Vertex AI Gemini

## Troubleshooting

### Authentication Errors

If you encounter authentication errors:

1. Check that your OAuth client is properly configured with the required scopes
2. Delete the `tokens` directory and run `./authenticate-oauth.sh` again to regenerate tokens
3. Make sure you have the Vertex AI API enabled in your Google Cloud project

### Permission Issues

If you encounter permission issues:

1. Make sure your Google account has the necessary permissions to access Vertex AI Gemini
2. Ensure your OAuth client has been added to the project with the correct roles
3. Check that you have enabled the required APIs in your project:
   - Vertex AI API
   - Generative Language API

## Switching Between Authentication Methods

The application supports both OAuth 2.0 and API Key authentication:

- If both are configured, OAuth 2.0 is preferred
- If OAuth authentication fails, it will automatically fall back to API key authentication
- To force API key authentication, delete or rename the `tokens` directory

## Security Considerations

- Keep your OAuth credentials file secure; it contains your client secret
- Do not commit the `tokens` directory or `oauth-credentials.json` to version control
- Use different OAuth clients for development and production environments 