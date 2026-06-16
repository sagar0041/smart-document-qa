package dev.sagarpatil.docqa.document;

import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DocumentIngestionConsumer {

    private DocumentRepository documentRepository;
    private final TextSplitter textSplitter = new TokenTextSplitter();
    private VectorStore vectorStore;

    public DocumentIngestionConsumer(DocumentRepository documentRepository, VectorStore vectorStore) {
        this.documentRepository = documentRepository;
        this.vectorStore = vectorStore;
    }

    @KafkaListener(topics = "doc-ingestion", groupId = "docqa-ingestion")
    public void onDocumentUploaded(String documentId) {
      Optional<Document> result = documentRepository.findById(documentId);
      result.ifPresent(document -> {
          document.setStatus("PROCESSING");
          documentRepository.save(document);
          System.out.println("Processing document: " + documentId);

          try {
              String text = document.getContent();
              org.springframework.ai.document.Document aiDocs = new org.springframework.ai.document.Document(text, Map.of("documentId", documentId));
              List<org.springframework.ai.document.Document> originalList = List.of(aiDocs);
              List<org.springframework.ai.document.Document> chunks = textSplitter.apply(originalList);
              vectorStore.add(chunks);

              document.setStatus("INDEXED");
              documentRepository.save(document);
          } catch (Exception e) {
              document.setStatus("FAILED");
              documentRepository.save(document);
              System.out.println("Failed to process document " + documentId + ": " + e.getMessage());
          }

      });
    }
}