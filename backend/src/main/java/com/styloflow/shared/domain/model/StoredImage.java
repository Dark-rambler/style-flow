package com.styloflow.shared.domain.model;

/**
 * Image kept in external storage.
 *
 * @param publicId storage id, needed to delete it
 * @param url public HTTPS url
 */
public record StoredImage(String publicId, String url) {}
