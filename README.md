# Spring AI Mobile Automation Framework

A framework that integrates Spring AI with mobile automation for testing Android applications using Appium.

## Features

- Integration with Google Vertex AI Gemini models for test automation
- Support for both API Key and OAuth authentication with Vertex AI
- Mobile automation with Appium for Android devices
- Learning mode to collect test execution data

## Setup and Configuration

### Prerequisites

- Java 17 or later
- Maven
- Android SDK with platform-tools
- Appium server (v2.0+)
- Google Cloud account with Vertex AI API enabled

### Configuration

1. **Create configuration files from templates**:

   ```bash
   # Copy and edit the application properties template
   cp application.properties.template src/main/resources/application.properties
   
   # Copy and edit script templates
   cp run-web-prod.sh.template run-web-prod.sh
   cp rebuild-and-run.sh.template rebuild-and-run.sh
   ```

2. **Update credentials and API keys**:

   Edit the copied files and replace placeholder values with your actual credentials:
   
   - In `src/main/resources/application.properties`:
     - Replace `your-project-id` with your Google Cloud project ID
     - Replace `your-api-key-here` with your Vertex AI API key
   
   - In script files (`run-web-prod.sh`, `rebuild-and-run.sh`):
     - Replace `your-project-id` with your Google Cloud project ID
     - Replace `your-api-key-here` with your Vertex AI API key

3. **Make scripts executable**:

   ```bash
   chmod +x *.sh
   ```

### Running the Application

1. **Start Appium Server**:

   ```bash
   ./start-appium.sh
   ```

2. **Run the application**:

   ```bash
   # Using API key authentication
   ./run-web-prod.sh
   
   # For development with rebuilding
   ./rebuild-and-run.sh
   ```

## Authentication Methods

### API Key Authentication

The default authentication method uses an API key for Vertex AI. Configure this in:
- Environment variables: `SPRING_AI_VERTEX_AI_GEMINI_API_KEY`
- Application properties: `spring.ai.vertex.ai.gemini.apiKey`

### OAuth Authentication (Optional)

For OAuth authentication:
1. Generate OAuth credentials in Google Cloud Console
2. Run `./authenticate-oauth.sh` to perform the OAuth flow
3. Run the application with the OAuth profile: `./run-web-oauth.sh`

## Security Notes

- Never commit sensitive credentials to Git
- All template files are safe to commit (they contain placeholders)
- The actual configuration files with real credentials are excluded in `.gitignore`
- Use environment variables when possible instead of hardcoding credentials

## Project Structure

```
spring-ai-mobile-automation/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── springai/
│   │   │           └── mobile/
│   │   │               └── automation/
│   │   │                   ├── config/         # Configuration classes
│   │   │                   ├── model/          # Data models
│   │   │                   ├── service/        # Business logic
│   │   │                   │   ├── ai/         # AI services
│   │   │                   │   ├── mobile/     # Mobile automation
│   │   │                   │   └── learning/   # Learning system
│   │   │                   ├── repository/     # Database repositories
│   │   │                   ├── controller/     # API endpoints
│   │   │                   └── util/           # Utility classes
│   │   └── resources/
│   │       ├── application.properties    # Application configuration
│   │       └── logback.xml               # Logging configuration
│   └── test/                           # Test classes
├── screenshots/                        # Test screenshots
├── logs/                              # Application logs
├── data/                              # H2 database files
├── pom.xml                            # Maven configuration
└── README.md                          # Project documentation
```

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Spring AI team for the excellent Spring AI framework
- Google Vertex AI for the Gemini model
- Appium community for the mobile automation tools 