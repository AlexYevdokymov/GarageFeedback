package org.example.util;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.Properties;

public class GptService {
    private String openaiToken;

    private final OpenAiService service;

    public GptService() {
        Properties props = new Properties();
        try (InputStream in = new FileInputStream("config.properties")) {
            props.load(in);
            openaiToken = props.getProperty("openai.token");
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.service = new OpenAiService(openaiToken, Duration.ofSeconds(60));
    }

    public String analyzeReview(String review) {
        ChatMessage systemMessage = new ChatMessage("system",
                "У повідомленні буде відгук користувача. Відповідай тільки одним словом: 'позитивний', 'негативний' або 'нейтральний'.");
        ChatMessage userMessage = new ChatMessage("user", review);

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-4o-mini")
                .messages(List.of(systemMessage, userMessage))
                .maxTokens(5)
                .temperature(0.0)
                .build();

        ChatCompletionResult result = service.createChatCompletion(request);
        return result.getChoices().get(0).getMessage().getContent().trim().toLowerCase();
    }
}
