package dev.sagarpatil.docqa.document;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String filename;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String status; // UPLOADED, PROCESSING, INDEXED, FAILED

    private Instant createdAt;

    public Document() {}

    public Document(String filename, String content, String status) {
        this.filename = filename;
        this.content = content;
        this.status = status;
        this.createdAt = Instant.now();
    }

    // getters and setters
    public String getId() { return id; }
    public String getFilename() { return filename; }
    public String getContent() { return content; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
}