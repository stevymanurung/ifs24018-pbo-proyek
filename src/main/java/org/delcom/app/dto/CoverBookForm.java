package org.delcom.app.dto;

import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.NotNull;

public class CoverBookForm {

    private UUID id;

    @NotNull(message = "Cover tidak boleh kosong")
    private MultipartFile coverFile;

    public CoverBookForm() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public MultipartFile getCoverFile() {
        return coverFile;
    }

    public void setCoverFile(MultipartFile coverFile) {
        this.coverFile = coverFile;
    }

    public boolean isEmpty() {
        return coverFile == null || coverFile.isEmpty();
    }

    public String getOriginalFilename() {
        return coverFile != null ? coverFile.getOriginalFilename() : null;
    }

    public boolean isValidImage() {
        if (this.isEmpty()) {
            return false;
        }

        String contentType = coverFile.getContentType();
        return contentType != null &&
                (contentType.equals("image/jpeg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/gif") ||
                        contentType.equals("image/webp"));
    }

    public boolean isSizeValid(long maxSize) {
        return coverFile != null && coverFile.getSize() <= maxSize;
    }
}