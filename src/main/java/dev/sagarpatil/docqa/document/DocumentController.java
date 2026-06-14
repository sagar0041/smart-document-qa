package dev.sagarpatil.docqa.document;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final TextExtractionService textExtractionService;
    private final DocumentRepository documentRepository;
    private final DocumentEventPublisher documentEventPublisher;

    public DocumentController(TextExtractionService textExtractionService,
                               DocumentRepository documentRepository,
                              DocumentEventPublisher documentEventPublisher) {
        this.textExtractionService = textExtractionService;
        this.documentRepository = documentRepository;
        this.documentEventPublisher = documentEventPublisher;
    }

    @PostMapping
    public ResponseEntity<?> uploadDocument(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }

        try {
            String text = textExtractionService.extractText(file);

            Document document = new Document(file.getOriginalFilename(), text, "UPLOADED");
            documentRepository.save(document);

            documentEventPublisher.publishDocumentUploaded(document.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "id", document.getId(),
                    "filename", document.getFilename(),
                    "status", document.getStatus(),
                    "extractedChars", text.length()
            ));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}