package com.styloflow.catalog.application.utils;

import com.styloflow.shared.domain.exception.BusinessRuleException;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.springframework.web.multipart.MultipartFile;

public final class ImageFilesUtil {

    private ImageFilesUtil() {}

    public static byte[] bytes(MultipartFile file) {
        if (file == null || file.isEmpty())
            return null;
        var type = file.getContentType();
        if (type == null || !type.startsWith("image/"))
            throw new BusinessRuleException("The file must be an image");
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
