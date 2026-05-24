package com.student.demo.service;

import java.util.*;
import java.util.regex.*;

public class SemanticCodeAnalyzer {

    public static class CodeMetadata {
        public String language = "Java";
        public List<String> classes = new ArrayList<>();
        public List<String> methods = new ArrayList<>();
        public int loopsCount = 0;
        public boolean hasRecursion = false;
        public int recursionDepth = 0;
        public List<String> collections = new ArrayList<>();
        public boolean hasStreams = false;
        public boolean hasMultithreading = false;
        public List<String> concurrencyPatterns = new ArrayList<>();
        public List<String> sorting = new ArrayList<>();
        public List<String> exceptions = new ArrayList<>();
        public List<String> databaseUsage = new ArrayList<>();
        public List<String> fileHandling = new ArrayList<>();
        public List<String> networkCalls = new ArrayList<>();
        public List<String> algorithms = new ArrayList<>();
        public boolean isSecuritySensitive = false;
        public boolean isScalabilityRelated = false;
        public boolean isGraphRelated = false;
        public boolean hasScannerInput = false;
    }

    public static CodeMetadata analyze(String code, String filename) {
        CodeMetadata meta = new CodeMetadata();
        if (code == null || code.isBlank()) {
            return meta;
        }

        // Deduce language
        if (filename != null) {
            String lower = filename.toLowerCase();
            if (lower.endsWith(".py")) {
                meta.language = "Python";
            } else if (lower.endsWith(".js")) {
                meta.language = "JavaScript";
            } else if (lower.endsWith(".java")) {
                meta.language = "Java";
            }
        }

        // 1. Classes
        Pattern classPattern = Pattern.compile("(?:class|interface|enum)\\s+(\\w+)");
        Matcher classMatcher = classPattern.matcher(code);
        while (classMatcher.find()) {
            meta.classes.add(classMatcher.group(1));
        }

        // 2. Methods
        Pattern methodPattern = Pattern.compile("(?:public|protected|private|static|\\s)+\\s+[\\w<>\\[\\]]+\\s+(\\w+)\\s*\\(");
        Matcher methodMatcher = methodPattern.matcher(code);
        while (methodMatcher.find()) {
            String mName = methodMatcher.group(1);
            if (!mName.equals("class") && !mName.equals("interface") && !mName.equals("new") && !mName.equals("return") && !mName.equals("if") && !mName.equals("while") && !mName.equals("for") && !mName.equals("switch")) {
                if (!meta.methods.contains(mName)) {
                    meta.methods.add(mName);
                }
            }
        }

        // 3. Loops
        meta.loopsCount = countOccurrences(code, "for\\s*\\(") + countOccurrences(code, "while\\s*\\(");
        if (meta.loopsCount >= 2) {
            meta.isScalabilityRelated = true;
        }

        // 4. Recursion
        for (String method : meta.methods) {
            // Check if method calls itself, i.e., methodName(...) appears inside the code
            // and we exclude the declaration
            if (!method.equals("main")) {
                int count = countOccurrences(code, method + "\\s*\\(");
                if (count > 1) { // 1 for declaration, >1 means it is called elsewhere (likely recursion or helper call)
                    // Let's check if it's recursive
                    Pattern recPattern = Pattern.compile("(?s)void\\s+" + method + "|[\\w<>]+\\s+" + method + "\\s*\\(.*?" + method + "\\s*\\(");
                    if (recPattern.matcher(code).find()) {
                        meta.hasRecursion = true;
                        meta.recursionDepth = 2; // general depth indicator
                        meta.isScalabilityRelated = true;
                    }
                }
            }
        }

        // 5. Collections
        String[] collKeys = {"TreeMap", "HashMap", "LinkedHashMap", "ConcurrentHashMap", "ArrayList", "LinkedList", "HashSet", "TreeSet", "Vector", "Queue", "Stack", "Map", "List", "Set"};
        for (String key : collKeys) {
            if (code.contains(key)) {
                meta.collections.add(key);
            }
        }

        // 6. Streams API
        if (code.contains(".stream()") || code.contains(".collect(") || code.contains(".filter(") || code.contains(".map(") || code.contains("Collectors.")) {
            meta.hasStreams = true;
        }

        // 7. Multithreading & Concurrency
        String[] multithreadKeys = {"Thread", "Runnable", "Callable", "ExecutorService", "Executors", "synchronized", "volatile", "Lock", "ReentrantLock", "Atomic", "Semaphore", "Future", "ForkJoinPool"};
        for (String key : multithreadKeys) {
            if (code.contains(key)) {
                meta.hasMultithreading = true;
                meta.concurrencyPatterns.add(key);
            }
        }
        if (meta.hasMultithreading) {
            meta.isScalabilityRelated = true;
        }

        // 8. Sorting
        if (code.contains("sort") || code.contains("Arrays.sort") || code.contains("Collections.sort")) {
            meta.sorting.add("Standard Library Sort");
        }

        // 9. Exceptions
        Pattern excPattern = Pattern.compile("catch\\s*\\(\\s*(\\w+)");
        Matcher excMatcher = excPattern.matcher(code);
        while (excMatcher.find()) {
            meta.exceptions.add(excMatcher.group(1));
        }
        if (code.contains("throw new ")) {
            meta.exceptions.add("Explicit Throw");
        }

        // 10. Database Usage
        String[] dbKeys = {"Connection", "PreparedStatement", "EntityManager", "Repository", "DataSource", "SQL", "select ", "insert ", "update ", "delete ", "JdbcTemplate"};
        for (String key : dbKeys) {
            if (code.contains(key)) {
                meta.databaseUsage.add(key);
            }
        }

        // 11. File Handling
        String[] fileKeys = {"File", "FileReader", "FileWriter", "FileInputStream", "FileOutputStream", "BufferedReader", "BufferedWriter", "Paths", "Files.write", "Files.read"};
        for (String key : fileKeys) {
            if (code.contains(key)) {
                meta.fileHandling.add(key);
            }
        }

        // 12. Network Calls
        String[] netKeys = {"HttpClient", "HttpURLConnection", "RestTemplate", "Socket", "ServerSocket", "URL", "WebClient"};
        for (String key : netKeys) {
            if (code.contains(key)) {
                meta.networkCalls.add(key);
            }
        }

        // 13. Scanner / Stdin
        if (code.contains("Scanner") || code.contains("System.in") || code.contains("stdin.read")) {
            meta.hasScannerInput = true;
        }

        // 14. Algorithms
        if (code.contains("TreeMap")) {
            meta.algorithms.add("Red-Black Tree Sorted Map");
        }
        if (code.contains("binarySearch") || (code.contains("low") && code.contains("high") && code.contains("mid") && code.contains("while"))) {
            meta.algorithms.add("Binary Search");
        }
        if (code.contains("dfs") || code.contains("DFS") || code.contains("bfs") || code.contains("BFS") || code.contains("adjacency") || code.contains("Graph")) {
            meta.algorithms.add("Graph Traversal");
            meta.isGraphRelated = true;
        }

        // 15. Security Sensitive Detection
        if (code.contains("password") || code.contains("passwd") || code.contains("secret") || code.contains("token") || code.contains("apikey") || code.contains("private_key")) {
            meta.isSecuritySensitive = true;
        }
        if (code.contains("DriverManager.getConnection") || code.contains("statement.executeQuery") || code.contains("MD5") || code.contains("SHA-1") || (code.contains("Random") && !code.contains("SecureRandom"))) {
            meta.isSecuritySensitive = true;
        }

        return meta;
    }

    private static int countOccurrences(String text, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }
}
