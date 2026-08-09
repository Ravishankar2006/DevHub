package com.devhub.documents;

import com.devhub.common.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class DocumentFileStorageService {

    private final Path baseDir;

    public DocumentFileStorageService(@Value("${devhub.storage.documents-dir}") String documentsDir) {
        this.baseDir = Path.of(documentsDir).toAbsolutePath().normalize();
    }

    public String store(UUID userId, MultipartFile file) {
        try {
            Path userDir = baseDir.resolve(userId.toString()).normalize();
            Files.createDirectories(userDir);

            String cleanedName = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "document");
            String safeName = Path.of(cleanedName).getFileName().toString();
            String storedName = UUID.randomUUID() + "_" + safeName;

            Path target = userDir.resolve(storedName).normalize();
            if (!target.startsWith(userDir)) {
                throw new ApiException("Invalid file name", HttpStatus.BAD_REQUEST);
            }

            file.transferTo(target);
            return baseDir.relativize(target).toString();
        } catch (IOException e) {
            throw new ApiException("Failed to store file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public Resource loadAsResource(String storagePath) {
        Path target = baseDir.resolve(storagePath).normalize();
        if (!target.startsWith(baseDir) || !Files.exists(target)) {
            throw new ApiException("File not found", HttpStatus.NOT_FOUND);
        }
        return new FileSystemResource(target);
    }

    public void delete(String storagePath) {
        Path target = baseDir.resolve(storagePath).normalize();
        if (!target.startsWith(baseDir)) {
            return;
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            throw new ApiException("Failed to delete file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
