package com.devhub.documents;

import com.devhub.common.ApiException;
import com.devhub.documents.dto.DocumentDto;
import com.devhub.documents.dto.DocumentMapper;
import com.devhub.jobs.AiJobService;
import com.devhub.jobs.AiJobType;
import com.devhub.users.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentUploadService {

    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024;

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final DocumentFileStorageService storageService;
    private final AiJobService aiJobService;

    @Transactional
    public DocumentDto uploadDocument(User currentUser, MultipartFile file, String title) {
        validateFile(file);

        String fileName = file.getOriginalFilename();
        String storagePath = storageService.store(currentUser.getId(), file);
        String extractedText = isTextFile(fileName) ? readAsText(file) : null;

        Document document = Document.builder()
                .user(currentUser)
                .sourceType(DocumentSourceType.UPLOAD)
                .title(StringUtils.hasText(title) ? title : fileName)
                .storagePath(storagePath)
                .fileName(fileName)
                .extractedText(extractedText)
                .build();

        document = documentRepository.save(document);
        aiJobService.createJob(currentUser, AiJobType.DOCUMENT_INDEX, document.getId());

        return DocumentMapper.toDto(document);
    }

    @Transactional(readOnly = true)
    public List<DocumentDto> listDocuments(User currentUser) {
        return documentRepository.findByUserIdAndSourceTypeOrderByUpdatedAtDesc(currentUser.getId(), DocumentSourceType.UPLOAD)
                .stream().map(DocumentMapper::toDto).collect(Collectors.toList());
    }

    @Transactional
    public void deleteDocument(User currentUser, UUID documentId) {
        Document document = getOwnedDocument(currentUser, documentId);

        documentChunkRepository.deleteByDocumentId(document.getId());
        if (document.getStoragePath() != null) {
            storageService.delete(document.getStoragePath());
        }
        documentRepository.delete(document);
    }

    private Document getOwnedDocument(User currentUser, UUID documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ApiException("Document not found", HttpStatus.NOT_FOUND));

        if (document.getSourceType() != DocumentSourceType.UPLOAD || !document.getUser().getId().equals(currentUser.getId())) {
            throw new ApiException("Document not found", HttpStatus.NOT_FOUND);
        }
        return document;
    }

    private boolean isTextFile(String fileName) {
        if (fileName == null) return false;
        String lower = fileName.toLowerCase();
        return lower.endsWith(".txt") || lower.endsWith(".md");
    }

    private String readAsText(MultipartFile file) {
        try {
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ApiException("Failed to read file contents", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException("A file is required", HttpStatus.BAD_REQUEST);
        }

        String filename = file.getOriginalFilename();
        boolean isSupported = filename != null && (
                filename.toLowerCase().endsWith(".txt")
                        || filename.toLowerCase().endsWith(".md")
                        || filename.toLowerCase().endsWith(".pdf"));
        if (!isSupported) {
            throw new ApiException("Only .txt, .md, and .pdf files are allowed", HttpStatus.BAD_REQUEST);
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new ApiException("File exceeds the 10MB size limit", HttpStatus.BAD_REQUEST);
        }
    }
}
