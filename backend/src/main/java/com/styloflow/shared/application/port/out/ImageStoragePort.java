package com.styloflow.shared.application.port.out;

import com.styloflow.shared.domain.model.StoredImage;

public interface ImageStoragePort {

    StoredImage upload(byte[] content, String folder);

    void delete(String publicId);
}
