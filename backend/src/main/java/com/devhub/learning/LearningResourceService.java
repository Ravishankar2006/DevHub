package com.devhub.learning;

import com.devhub.common.ApiException;
import com.devhub.learning.dto.LearningResourceDto;
import com.devhub.learning.dto.LearningResourceMapper;
import com.devhub.learning.dto.LearningResourceRequest;
import com.devhub.users.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LearningResourceService {

    private final LearningResourceRepository learningResourceRepository;

    @Transactional
    public LearningResourceDto createResource(User currentUser, LearningResourceRequest request) {
        LearningResource resource = LearningResource.builder()
                .user(currentUser)
                .title(request.getTitle())
                .resourceType(request.getResourceType() != null ? request.getResourceType() : LearningResourceType.COURSE)
                .provider(request.getProvider())
                .url(request.getUrl())
                .status(request.getStatus() != null ? request.getStatus() : LearningStatus.NOT_STARTED)
                .progressPercent(clampProgress(request.getProgressPercent()))
                .estimatedCompletionDate(request.getEstimatedCompletionDate())
                .tags(LearningResourceMapper.joinTags(request.getTags()))
                .notes(request.getNotes())
                .build();

        resource = learningResourceRepository.save(resource);
        return LearningResourceMapper.toDto(resource);
    }

    @Transactional(readOnly = true)
    public List<LearningResourceDto> listResources(User currentUser, LearningStatus status, LearningResourceType resourceType) {
        UUID userId = currentUser.getId();
        List<LearningResource> resources;
        if (status != null && resourceType != null) {
            resources = learningResourceRepository.findByUserIdAndStatusAndResourceTypeOrderByUpdatedAtDesc(userId, status, resourceType);
        } else if (status != null) {
            resources = learningResourceRepository.findByUserIdAndStatusOrderByUpdatedAtDesc(userId, status);
        } else if (resourceType != null) {
            resources = learningResourceRepository.findByUserIdAndResourceTypeOrderByUpdatedAtDesc(userId, resourceType);
        } else {
            resources = learningResourceRepository.findByUserIdOrderByUpdatedAtDesc(userId);
        }
        return resources.stream().map(LearningResourceMapper::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LearningResourceDto getResource(User currentUser, UUID resourceId) {
        return LearningResourceMapper.toDto(getOwnedResource(currentUser, resourceId));
    }

    @Transactional
    public LearningResourceDto updateResource(User currentUser, UUID resourceId, LearningResourceRequest request) {
        LearningResource resource = getOwnedResource(currentUser, resourceId);

        resource.setTitle(request.getTitle());
        if (request.getResourceType() != null) {
            resource.setResourceType(request.getResourceType());
        }
        resource.setProvider(request.getProvider());
        resource.setUrl(request.getUrl());
        if (request.getStatus() != null) {
            resource.setStatus(request.getStatus());
        }
        resource.setProgressPercent(clampProgress(request.getProgressPercent()));
        resource.setEstimatedCompletionDate(request.getEstimatedCompletionDate());
        resource.setTags(LearningResourceMapper.joinTags(request.getTags()));
        resource.setNotes(request.getNotes());

        resource = learningResourceRepository.save(resource);
        return LearningResourceMapper.toDto(resource);
    }

    @Transactional
    public void deleteResource(User currentUser, UUID resourceId) {
        LearningResource resource = getOwnedResource(currentUser, resourceId);
        learningResourceRepository.delete(resource);
    }

    LearningResource getOwnedResource(User currentUser, UUID resourceId) {
        LearningResource resource = learningResourceRepository.findById(resourceId)
                .orElseThrow(() -> new ApiException("Learning resource not found", HttpStatus.NOT_FOUND));

        if (!resource.getUser().getId().equals(currentUser.getId())) {
            throw new ApiException("Learning resource not found", HttpStatus.NOT_FOUND);
        }
        return resource;
    }

    private int clampProgress(Integer progressPercent) {
        if (progressPercent == null) return 0;
        return Math.max(0, Math.min(100, progressPercent));
    }
}
