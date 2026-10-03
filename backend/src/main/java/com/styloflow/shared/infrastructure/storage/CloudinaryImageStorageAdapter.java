package com.styloflow.shared.infrastructure.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.styloflow.shared.application.port.out.ImageStoragePort;
import com.styloflow.shared.domain.model.StoredImage;
import com.styloflow.shared.infrastructure.config.AppProperties;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class CloudinaryImageStorageAdapter implements ImageStoragePort {

    private final Cloudinary cloudinary;

    public CloudinaryImageStorageAdapter(AppProperties props) {
        String url = props.cloudinary().url();
        this.cloudinary = new Cloudinary(url);
        this.cloudinary.config.secure = true;
    }

    @Override
    public StoredImage upload(byte[] content, String folder) {
        try {
            Map<?, ?> result = cloudinary.uploader()
                    .upload(content, ObjectUtils.asMap("folder", folder, "resource_type", "image"));
            return new StoredImage((String) result.get("public_id"), (String) result.get("secure_url"));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not upload image to Cloudinary", e);
        }
    }

    @Override
    public void delete(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not delete image " + publicId + " from Cloudinary", e);
        }
    }
}
