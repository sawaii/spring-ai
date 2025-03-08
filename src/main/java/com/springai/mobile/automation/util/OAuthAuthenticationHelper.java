package com.springai.mobile.automation.util;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

/**
 * Helper utility for OAuth 2.0 authentication with Google APIs
 */
public class OAuthAuthenticationHelper {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList("https://www.googleapis.com/auth/cloud-platform");
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final int PORT = 8888;

    /**
     * Run the OAuth 2.0 authentication flow interactively to obtain credentials
     * @param credentialsFilePath Path to the OAuth credentials file
     * @return The authorized Credential object
     * @throws IOException If the credentials.json file cannot be found
     * @throws GeneralSecurityException If there is a security issue with the connection
     */
    public static Credential runAuthenticationFlow(String credentialsFilePath) 
            throws IOException, GeneralSecurityException {
        
        // Load client secrets
        File credentialsFile = new File(credentialsFilePath);
        if (!credentialsFile.exists()) {
            throw new IOException("OAuth credentials file not found at: " + credentialsFilePath);
        }
        
        // Initialize transport and JSON factory
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        
        // Load client secrets
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, 
                new InputStreamReader(new FileInputStream(credentialsFile)));
        
        // Create a directory for storing tokens
        File tokenDirectory = new File(TOKENS_DIRECTORY_PATH);
        if (!tokenDirectory.exists()) {
            tokenDirectory.mkdirs();
        }
        
        // Build the authorization code flow
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(tokenDirectory))
                .setAccessType("offline")
                .build();
        
        // Authorize and get credentials
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(PORT).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
    
    /**
     * Command-line tool to generate OAuth tokens
     */
    public static void main(String[] args) {
        try {
            String credentialsPath = args.length > 0 ? args[0] : "oauth-credentials.json";
            System.out.println("Running OAuth authentication flow with credentials file: " + credentialsPath);
            Credential credential = runAuthenticationFlow(credentialsPath);
            System.out.println("Authentication successful!");
            System.out.println("Access token: " + credential.getAccessToken());
            System.out.println("Refresh token available: " + (credential.getRefreshToken() != null));
            System.out.println("Tokens stored in directory: " + TOKENS_DIRECTORY_PATH);
        } catch (IOException | GeneralSecurityException e) {
            System.err.println("Error during authentication: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 