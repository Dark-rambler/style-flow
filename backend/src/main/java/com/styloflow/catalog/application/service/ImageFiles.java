package com.styloflow.catalog.application.service;

import com.styloflow.shared.domain.exception.BusinessRuleException;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.springframework.web.multipart.MultipartFile;

final class ImageFiles {

    private ImageFiles() {}

    /** @return the image bytes, or {@code null} when no file was sent */
    static byte[] bytes(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String type = file.getContentType();
        if (type == null || !type.startsWith("image/")) {
            throw new BusinessRuleException("The file must be an image");
        }
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
