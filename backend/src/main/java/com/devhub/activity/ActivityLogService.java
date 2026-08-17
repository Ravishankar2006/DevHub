package com.devhub.activity;

import com.devhub.activity.dto.ActivityLogDto;
import com.devhub.activity.dto.ActivityLogMapper;
import com.devhub.users.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    @Transactional
    public void log(User user, ActivityLogSource source, String actionType, String targetEntityRef, String summary) {
        activityLogRepository.save(ActivityLog.builder()
                .user(user)
                .source(source)
                .actionType(actionType)
                .targetEntityRef(targetEntityRef)
                .summary(summary)
                .build());
    }

    @Transactional(readOnly = true)
    public List<ActivityLogDto> listRecent(User currentUser) {
        return activityLogRepository.findTop50ByUserIdOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(ActivityLogMapper::toDto)
                .collect(Collectors.toList());
    }
}
