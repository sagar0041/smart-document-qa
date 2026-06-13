package dev.sagarpatil.docqa.document;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class TextExtractionService {

    private final Tika tika = new Tika();

    public String extractText(MultipartFile file) throws IOException {
        try {
            return tika.parseToString(file.getInputStream());
        } catch (Exception e) {
            throw new IOException("Failed to extract text from " + file.getOriginalFilename(), e);
        }
    }
}