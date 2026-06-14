package dev.sagarpatil.docqa.document;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class DocumentEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public DocumentEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishDocumentUploaded(String documentId) {
        kafkaTemplate.send("doc-ingestion", documentId);
    }
}