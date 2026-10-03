package com.styloflow.shared.application.port.out;

import com.styloflow.shared.domain.model.StoredImage;

/** External storage for images (logos, product photos, ...). */
public interface ImageStoragePort {

    /**
     * @param content image bytes
     * @param folder destination folder, e.g. "products"
     * @return where the image was stored
     */
    StoredImage upload(byte[] content, String folder);

    /** @param publicId id returned by {@link #upload} */
    void delete(String publicId);
}
