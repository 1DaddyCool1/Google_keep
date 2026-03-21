package com.mykola.keep.mediaservice.service;

import com.mykola.keep.mediaservice.entity.Media;

import java.io.IOException;

public interface MediaService {
    Media uploadImage(String username, String base64Image) throws IOException;
    byte[] getImageData(Long id, String username) throws IOException;
    String getContentType(Long id, String username) throws IOException;
    Media copyImage(Long sourceId, String username) throws IOException;
    void deleteImage(Long id, String username);
    String extractMimeTypeIfDataUrl(String input);
    String stripDataUrlPrefixAndWhitespace(String input);
    String extensionFromMime(String mimeType);
    String sanitizePathSegment(String segment);
}
