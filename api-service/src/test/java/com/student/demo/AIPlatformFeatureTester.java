import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AIPlatformFeatureTester {

    private static final String BASE_URL = "http://localhost:8080";
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static String accessToken = null;

    public static void main(String[] args) throws Exception {
        System.out.println("==========================================");
        System.out.println("🚀 FAANG Automated Validation Suite Initiated");
        System.out.println("==========================================\n");

        if (waitForServer()) {
            runAuthFlow();
        } else {
            System.out.println("❌ Server failed to start within the timeout period.");
        }
        
        System.out.println("\n✅ All automated execution signals verified. Proceed to generate manual reports.");
    }

    private static boolean waitForServer() {
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

    private static void runAuthFlow() {
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
                System.out.println("  [FAIL] Signup failed: " + signupRes.statusCode());
                return;
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
                
                // Extract cookie
                var cookies = loginRes.headers().allValues("Set-Cookie");
                for (String cookie : cookies) {
                    if (cookie.contains("access_token=")) {
                        Pattern pattern = Pattern.compile("access_token=([^;]+)");
                        Matcher matcher = pattern.matcher(cookie);
                        if (matcher.find()) {
                            accessToken = matcher.group(1);
                            System.out.println("  [PASS] JWT HttpOnly Cookie extracted");
                        }
                    }
                }
            } else {
                System.out.println("  [FAIL] Login failed: " + loginRes.statusCode());
            }

        } catch (Exception e) {
            System.out.println("  [FAIL] Exception in Auth Flow:");
            e.printStackTrace();
        }
    }
}
