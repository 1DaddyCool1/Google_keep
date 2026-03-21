package com.mykola.keep.mediaservice.service.impl;

import com.mykola.keep.mediaservice.entity.Media;
import com.mykola.keep.mediaservice.repository.MediaRepository;
import com.mykola.keep.mediaservice.service.MediaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.UUID;

@Service
public class MediaServiceImpl implements MediaService {

    private final MediaRepository mediaRepository;
    @Value("${app.images.base-path}")
    private String imageBasePath;
    public MediaServiceImpl(MediaRepository mediaRepository) {this.mediaRepository = mediaRepository;}


    @Override
    public Media uploadImage(String username, String base64Image) throws IOException {
        String mimeType = extractMimeTypeIfDataUrl(base64Image);
        String cleanBase64 = stripDataUrlPrefixAndWhitespace(base64Image);

        byte[] data = Base64.getMimeDecoder().decode(cleanBase64);
        String ext = extensionFromMime(mimeType);

        String sanitizedUsername = sanitizePathSegment(username);
        Path userDir = Paths.get(imageBasePath, sanitizedUsername);
        Files.createDirectories(userDir);

        String fileName = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().replace("-", "") + "_image" + (ext.isEmpty() ? "" : "." + ext);
        Path imagePath = userDir.resolve(fileName);
        Files.write(imagePath, data);

        Media media = new Media();
        media.setUsername(username);
        media.setImagePath(imagePath.toString());
        return mediaRepository.save(media);
    }

    @Override
    public byte[] getImageData(Long id, String username) throws IOException {
        Media media = mediaRepository.findByIdAndUsername(id, username).orElse(null);
        if (media == null) {
            return null;
        }
        Path imagePath = Paths.get(media.getImagePath());
        if (!Files.exists(imagePath)) {
            return null;
        }
        return Files.readAllBytes(imagePath);
    }

    @Override
    public String getContentType(Long id, String username) throws IOException {
        Media media = mediaRepository.findByIdAndUsername(id, username).orElse(null);
        if (media == null) {
            return null;
        }
        Path imagePath = Paths.get(media.getImagePath());
        return Files.probeContentType(imagePath);
    }

    @Override
    public Media copyImage(Long sourceId, String username) throws IOException {
        Media source = mediaRepository.findByIdAndUsername(sourceId, username).orElse(null);
        if (source == null) {
            throw new IllegalArgumentException("Source image not found");
        }

        Path srcPath = Paths.get(source.getImagePath());
        if (!Files.exists(srcPath)) {
            throw new IOException("Source file does not exist");
        }

        String sanitizedUsername = sanitizePathSegment(username);
        Path userDir = Paths.get(imageBasePath, sanitizedUsername);
        Files.createDirectories(userDir);

        String ext = "";
        String name = srcPath.getFileName() != null ? srcPath.getFileName().toString() : "";
        int dot = name.lastIndexOf('.');
        if (dot >= 0) ext = name.substring(dot);

        String newName = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().replace("-", "") + "_image" + ext;
        Path dstPath = userDir.resolve(newName);
        Files.copy(srcPath, dstPath, StandardCopyOption.COPY_ATTRIBUTES);

        Media media = new Media();
        media.setUsername(username);
        media.setImagePath(dstPath.toString());
        return mediaRepository.save(media);
    }

    @Override
    public void deleteImage(Long id, String username) {
        Media media = mediaRepository.findByIdAndUsername(id, username).orElse(null);
        if (media != null) {
            Path p = Paths.get(media.getImagePath());
            try {
                Files.deleteIfExists(p);
            } catch (Exception ignored) {}
            mediaRepository.delete(media);
        }
    }

    @Override
    public String extractMimeTypeIfDataUrl(String input) {
        if (input != null && input.startsWith("data:")) {
            int semiIdx = input.indexOf(';');
            if (semiIdx > 5) {
                return input.substring(5, semiIdx);
            }
        }
        return "";
    }

    @Override
    public String stripDataUrlPrefixAndWhitespace(String input) {
        if (input == null) return "";
        String s = input.trim();
        if (s.startsWith("data:")) {
            int commaIdx = s.indexOf(',');
            if (commaIdx >= 0) {
                s = s.substring(commaIdx + 1);
            }
        }
        return s.replaceAll("\\s", "");
    }

    @Override
    public String extensionFromMime(String mimeType) {
        if (mimeType == null || mimeType.isBlank()) return "";
        return switch (mimeType.toLowerCase()) {
            case "image/jpeg", "image/jpg" -> "jpg";
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            case "image/webp" -> "webp";
            case "image/svg+xml" -> "svg";
            default -> "";
        };
    }

    @Override
    public String sanitizePathSegment(String segment) {
        if (segment == null) return "unknown";
        return segment.replaceAll("[^a-zA-Z0-9_-]", "_");
    }
}
