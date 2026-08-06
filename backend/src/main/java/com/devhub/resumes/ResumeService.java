package com.devhub.resumes;

import com.devhub.common.ApiException;
import com.devhub.resumes.dto.ResumeDto;
import com.devhub.resumes.dto.ResumeMapper;
import com.devhub.resumes.dto.ResumeMetadataRequest;
import com.devhub.users.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024;

    private final ResumeRepository resumeRepository;
    private final ResumeFileStorageService storageService;

    @Transactional
    public ResumeDto createResume(User currentUser, MultipartFile file, String name, String label, String notes) {
        validateFile(file);

        String storagePath = storageService.store(currentUser.getId(), file);

        Resume resume = Resume.builder()
                .user(currentUser)
                .name(name)
                .label(label)
                .fileName(file.getOriginalFilename())
                .storagePath(storagePath)
                .fileSizeBytes(file.getSize())
                .notes(notes)
                .build();

        resume = resumeRepository.save(resume);
        return ResumeMapper.toDto(resume);
    }

    @Transactional(readOnly = true)
    public List<ResumeDto> listResumes(User currentUser, String label) {
        List<Resume> resumes = label != null
                ? resumeRepository.findByUserIdAndLabelOrderByUpdatedAtDesc(currentUser.getId(), label)
                : resumeRepository.findByUserIdOrderByUpdatedAtDesc(currentUser.getId());
        return resumes.stream().map(ResumeMapper::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ResumeDto getResume(User currentUser, UUID resumeId) {
        return ResumeMapper.toDto(getOwnedResume(currentUser, resumeId));
    }

    @Transactional
    public ResumeDto updateResume(User currentUser, UUID resumeId, ResumeMetadataRequest request) {
        Resume resume = getOwnedResume(currentUser, resumeId);

        resume.setName(request.getName());
        resume.setLabel(request.getLabel());
        resume.setNotes(request.getNotes());

        resume = resumeRepository.save(resume);
        return ResumeMapper.toDto(resume);
    }

    @Transactional
    public void deleteResume(User currentUser, UUID resumeId) {
        Resume resume = getOwnedResume(currentUser, resumeId);
        storageService.delete(resume.getStoragePath());
        resumeRepository.delete(resume);
    }

    @Transactional(readOnly = true)
    public Resume downloadResume(User currentUser, UUID resumeId) {
        return getOwnedResume(currentUser, resumeId);
    }

    Resume getOwnedResume(User currentUser, UUID resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ApiException("Resume not found", HttpStatus.NOT_FOUND));

        if (!resume.getUser().getId().equals(currentUser.getId())) {
            throw new ApiException("Resume not found", HttpStatus.NOT_FOUND);
        }
        return resume;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException("A PDF file is required", HttpStatus.BAD_REQUEST);
        }

        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        boolean isPdf = "application/pdf".equals(contentType)
                || (filename != null && filename.toLowerCase().endsWith(".pdf"));
        if (!isPdf) {
            throw new ApiException("Only PDF files are allowed", HttpStatus.BAD_REQUEST);
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new ApiException("File exceeds the 10MB size limit", HttpStatus.BAD_REQUEST);
        }
    }
}
