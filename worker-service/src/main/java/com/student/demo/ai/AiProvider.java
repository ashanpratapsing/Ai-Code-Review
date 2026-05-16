package com.student.demo.ai;

public interface AiProvider {

    /**
     * @return The unique name of the AI provider (e.g., "GROQ", "OPENAI")
     */
    String getProviderName();

    /**
     * @return true if the necessary API keys and URLs are configured.
     */
    boolean isConfigured();

    /**
     * Sends the code to the AI model for analysis.
     *
     * @param code         The source code to analyze
     * @param systemPrompt The instruction prompt for the model
     * @return The JSON string response from the AI model
     * @throws Exception if the API call fails or times out
     */
    String analyze(String code, String systemPrompt) throws Exception;
}
