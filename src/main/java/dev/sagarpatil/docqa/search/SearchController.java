package dev.sagarpatil.docqa.search;

import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private VectorStore vectorStore;

    public record AskRequest(String question) {}

    public record SearchResult(String content, String documentId, double score) {}

    @Autowired
    public SearchController(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @PostMapping
    public ResponseEntity<?> search(@RequestBody AskRequest request) {
        try {
            List<org.springframework.ai.document.Document> matches =
                    vectorStore.similaritySearch(
                            org.springframework.ai.vectorstore.SearchRequest.builder().query(request.question()).build()
                    );

            List<SearchResult> results = new ArrayList<>();

            for (org.springframework.ai.document.Document match : matches) {
                String content = match.getText();
                Object docIdObj = match.getMetadata().get("documentId");
                String documentId = (String) docIdObj;
                double score = (match.getScore() != null) ? match.getScore() : 0.0;

                SearchResult result = new SearchResult(content, documentId, score);
                results.add(result);
            }

            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Search is temporarily unavailable: " + e.getMessage()));
        }
    }
}