package com.student.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class AIPlatformFeatureTesterTest {

    private static final String BASE_URL = "http://localhost:8088";
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static String accessToken = null;

    @Test
    public void runFullValidationSuite() {
        System.out.println("==========================================");
        System.out.println("🚀 FAANG Automated Validation Suite Initiated via Maven");
        System.out.println("==========================================\n");

        if (waitForServer()) {
            runAuthFlow();
            long projectId = runProjectFlow();
            long fileId = runFileUpload(projectId);
            runAnalysisFlow(fileId);
        } else {
            fail("❌ Server failed to start within the timeout period. Test aborted.");
        }
        
        System.out.println("\n✅ All automated execution signals verified. End-to-End Analysis Pipeline is functional.");
    }

    private boolean waitForServer() {
        System.out.println("⏳ Waiting for API Service to start (up to 40 seconds)...");
        for (int i = 0; i < 20; i++) {
            try {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/actuator/health"))
                        .GET()
                        .build();
                HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
                if (res.statusCode() == 200) {
                    System.out.println("✅ Server is UP and healthy!");
                    return true;
                }
            } catch (Exception e) {
                // Ignore and retry
            }
            try { Thread.sleep(2000); } catch (InterruptedException e) {}
        }
        return false;
    }

    private void runAuthFlow() {
        System.out.println("▶ Testing Authentication (Signup & Login)...");
        try {
            String randomEmail = "test" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
            String signupJson = "{\"name\":\"Test User\",\"email\":\"" + randomEmail + "\",\"password\":\"password123\"}";
            
            HttpRequest signupReq = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/auth/signup"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(signupJson))
                    .build();

            HttpResponse<String> signupRes = client.send(signupReq, HttpResponse.BodyHandlers.ofString());
            if (signupRes.statusCode() == 200 || signupRes.statusCode() == 201) {
                System.out.println("  [PASS] Signup successful");
            } else {
                fail("Signup failed: " + signupRes.statusCode());
            }

            String loginJson = "{\"email\":\"" + randomEmail + "\",\"password\":\"password123\"}";
            HttpRequest loginReq = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(loginJson))
                    .build();

            HttpResponse<String> loginRes = client.send(loginReq, HttpResponse.BodyHandlers.ofString());
            
            if (loginRes.statusCode() == 200) {
                System.out.println("  [PASS] Login successful");
                boolean cookieFound = false;
                var cookies = loginRes.headers().allValues("Set-Cookie");
                for (String cookie : cookies) {
                    if (cookie.contains("access_token=")) {
                        Pattern pattern = Pattern.compile("access_token=([^;]+)");
                        Matcher matcher = pattern.matcher(cookie);
                        if (matcher.find()) {
                            accessToken = matcher.group(1);
                            System.out.println("  [PASS] JWT HttpOnly Cookie extracted");
                            cookieFound = true;
                        }
                    }
                }
                assertTrue(cookieFound, "JWT Cookie not found in response");

                // DIAGNOSTIC PROBE
                HttpRequest verifyReq = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/auth/verify"))
                        .header("Authorization", "Bearer " + accessToken)
                        .GET()
                        .build();
                HttpResponse<String> verifyRes = client.send(verifyReq, HttpResponse.BodyHandlers.ofString());
                System.out.println("  [DIAG] Auth Verify Result: " + verifyRes.body());
                if (!verifyRes.body().contains("AUTHENTICATED")) {
                    fail("Token validation failed in /auth/verify probe!");
                }

            } else {
                fail("Login failed: " + loginRes.statusCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception in Auth Flow: " + e.getMessage());
        }
    }

    private long runProjectFlow() {
        System.out.println("▶ Testing Project Creation...");
        try {
            String projectJson = "{\"projectName\":\"Validation Project " + UUID.randomUUID().toString().substring(0, 5) + "\"}";
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/projects"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + accessToken)
                    .POST(HttpRequest.BodyPublishers.ofString(projectJson))
                    .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() == 200 || res.statusCode() == 201) {
                System.out.println("  [PASS] Project created successfully");
                Pattern p = Pattern.compile("\"id\":(\\d+)");
                Matcher m = p.matcher(res.body());
                if (m.find()) {
                    return Long.parseLong(m.group(1));
                }
            }
            fail("Project creation failed: " + res.statusCode() + " -> " + res.body());
        } catch (Exception e) {
            fail("Project Flow Error: " + e.getMessage());
        }
        return -1;
    }

    private long runFileUpload(long projectId) {
        System.out.println("▶ Testing Code Upload...");
        try {
            String code = "public class HelloWorld { public static void main(String[] args) { System.out.println(\\\"Hello Validation\\\"); } }";
            String uploadJson = "{\"name\":\"HelloWorld.java\",\"content\":\"" + code + "\",\"language\":\"JAVA\",\"project\":{\"id\":" + projectId + "}}";
            
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/code/upload"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + accessToken)
                    .POST(HttpRequest.BodyPublishers.ofString(uploadJson))
                    .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() == 200 || res.statusCode() == 201) {
                System.out.println("  [PASS] Code uploaded successfully");
                Pattern p = Pattern.compile("\"id\":(\\d+)");
                Matcher m = p.matcher(res.body());
                if (m.find()) {
                    return Long.parseLong(m.group(1));
                }
            }
            fail("Code upload failed: " + res.statusCode() + " -> " + res.body());
        } catch (Exception e) {
            fail("Upload Flow Error: " + e.getMessage());
        }
        return -1;
    }

    private void runAnalysisFlow(long fileId) {
        System.out.println("▶ Triggering AI Analysis...");
        try {
            HttpRequest triggerReq = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/analyze/" + fileId))
                    .header("Authorization", "Bearer " + accessToken)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> triggerRes = client.send(triggerReq, HttpResponse.BodyHandlers.ofString());
            if (triggerRes.statusCode() == 202) {
                System.out.println("  [PASS] Analysis request accepted");
            } else {
                fail("Analysis trigger failed: " + triggerRes.statusCode());
            }

            System.out.println("⏳ Polling for AI Metrics (Max 60s)...");
            for (int i = 0; i < 30; i++) {
                HttpRequest pollReq = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/analyze/" + fileId))
                        .header("Authorization", "Bearer " + accessToken)
                        .GET()
                        .build();

                HttpResponse<String> pollRes = client.send(pollReq, HttpResponse.BodyHandlers.ofString());
                if (pollRes.statusCode() == 200 && pollRes.body().contains("\"status\":\"COMPLETED\"")) {
                    System.out.println("  [PASS] AI Analysis completed successfully!");
                    System.out.println("  [DATA] Analysis Results: " + pollRes.body().substring(0, Math.min(100, pollRes.body().length())) + "...");
                    return;
                } else if (pollRes.body().contains("\"status\":\"FAILED\"")) {
                    fail("AI Analysis failed: " + pollRes.body());
                }
                
                Thread.sleep(2000);
            }
            fail("AI Analysis timed out.");
        } catch (Exception e) {
            fail("Analysis Flow Error: " + e.getMessage());
        }
    }

}
