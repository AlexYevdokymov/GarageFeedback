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
            "У повідомленні буде відгук користувача. Відповідай у форматі JSON з трьома полями:\n" +
                    "1. tone — 'позитивний', 'негативний' або 'нейтральний'\n" +
                    "2. urgency — число від 1 до 5, де 5 найбільш критично для роботи автосервісу або безпеки, або потрібно в найближчий термін\n" +
                    "3. suggestion — короткий текст можливого рішення або дії\n\n" +
                    "Приклад відповіді:\n" +
                    "{ \"tone\": \"негативний\", \"urgency\": 4, \"suggestion\": \"Звернутися до служби підтримки\" }"
    );

    ChatMessage userMessage = new ChatMessage("user", review);

    ChatCompletionRequest request = ChatCompletionRequest.builder()
            .model("gpt-4o-mini")
            .messages(List.of(systemMessage, userMessage))
            .temperature(0.0)
            .maxTokens(100)
            .build();

    ChatCompletionResult result = service.createChatCompletion(request);
    return result.getChoices().get(0).getMessage().getContent().trim();
    }

}
