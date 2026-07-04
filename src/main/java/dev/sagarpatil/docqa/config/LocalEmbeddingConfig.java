package dev.sagarpatil.docqa.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Configuration
@Profile("local")
public class LocalEmbeddingConfig {

    @Bean
    public EmbeddingModel embeddingModel() {
        return new EmbeddingModel() {
            private final Random random = new Random();

            @Override
            public EmbeddingResponse call(EmbeddingRequest request) {
                List<Embedding> embeddings = IntStream.range(0, request.getInstructions().size())
                        .mapToObj(i -> new Embedding(randomVector(), i))
                        .collect(Collectors.toList());
                return new EmbeddingResponse(embeddings);
            }

            @Override
            public float[] embed(org.springframework.ai.document.Document document) {
                return randomVector();
            }

            @Override
            public int dimensions() {
                return 1536;
            }

            private float[] randomVector() {
                float[] vector = new float[1536];
                for (int i = 0; i < 1536; i++) {
                    vector[i] = random.nextFloat();
                }
                return vector;
            }
        };
    }

    @Bean
    public ChatModel chatModel() {
        return prompt -> ChatResponse.builder()
                .generations(List.of(
                        new org.springframework.ai.chat.model.Generation(
                                new org.springframework.ai.chat.messages.AssistantMessage(
                                        "This is a fake local answer. Add OpenAI credit for real responses."))))
                .build();
    }

    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }

}