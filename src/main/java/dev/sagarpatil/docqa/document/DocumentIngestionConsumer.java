package dev.sagarpatil.docqa.document;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DocumentIngestionConsumer {

    private DocumentRepository documentRepository;

    public DocumentIngestionConsumer(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @KafkaListener(topics = "doc-ingestion", groupId = "docqa-ingestion")
    public void onDocumentUploaded(String documentId) {
      Optional<Document> result = documentRepository.findById(documentId);
      result.ifPresent(document -> {
          document.setStatus("PROCESSING");
          documentRepository.save(document);
          System.out.println("Processing document: " + documentId);
      });
    }
}