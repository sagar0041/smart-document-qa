package dev.sagarpatil.docqa.ask;

import dev.sagarpatil.docqa.search.SearchController;
import dev.sagarpatil.docqa.search.SearchResult;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ask")
public class AskController {

    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    public AskController(VectorStore vectorStore, ChatClient chatClient) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClient;
    }

    @PostMapping
    public ResponseEntity<?> ask(@RequestBody SearchController.AskRequest request) {
        try {
            List<org.springframework.ai.document.Document> matches =
                    vectorStore.similaritySearch(
                            org.springframework.ai.vectorstore.SearchRequest
                                    .builder()
                                    .query(request.question())
                                    .build()
                    );

            List<SearchResult> sources = new ArrayList<>();
            for (org.springframework.ai.document.Document match : matches) {
                String content = match.getText();
                String documentId = (String) match.getMetadata().get("documentId");
                double score = (match.getScore() != null) ? match.getScore() : 0.0;
                sources.add(new SearchResult(content, documentId, score));
            }

            StringBuilder prompt = new StringBuilder();
            prompt.append("Answer the question based only on the following context.\n");
            prompt.append("If the context does not contain enough information, say so honestly.\n\n");
            prompt.append("Context:\n");
            for (SearchResult source : sources) {
                prompt.append(source.content()).append("\n");
            }
            prompt.append("\nQuestion: ").append(request.question());

            String answer = chatClient.prompt()
                    .user(prompt.toString())
                    .call()
                    .content();

            return ResponseEntity.ok(new AskResponse(answer, sources));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error",
                            "Ask service temporarily unavailable: " + e.getMessage()));
        }
    }
}