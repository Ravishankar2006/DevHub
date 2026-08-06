package com.devhub.calendar;

import com.devhub.calendar.dto.CalendarEventDto;
import com.devhub.calendar.dto.CalendarEventMapper;
import com.devhub.calendar.dto.CalendarEventRequest;
import com.devhub.common.ApiException;
import com.devhub.users.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalendarEventService {

    private final CalendarEventRepository calendarEventRepository;

    @Transactional
    public CalendarEventDto createEvent(User currentUser, CalendarEventRequest request) {
        CalendarEvent event = CalendarEvent.builder()
                .user(currentUser)
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory() != null ? request.getCategory() : CalendarEventCategory.OTHER)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .allDay(request.isAllDay())
                .location(request.getLocation())
                .build();

        event = calendarEventRepository.save(event);
        return CalendarEventMapper.toDto(event);
    }

    @Transactional(readOnly = true)
    public List<CalendarEventDto> listEvents(User currentUser, Instant from, Instant to) {
        return calendarEventRepository.findByUserIdAndStartTimeBetweenOrderByStartTimeAsc(currentUser.getId(), from, to)
                .stream().map(CalendarEventMapper::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CalendarEventDto getEvent(User currentUser, UUID eventId) {
        return CalendarEventMapper.toDto(getOwnedEvent(currentUser, eventId));
    }

    @Transactional
    public CalendarEventDto updateEvent(User currentUser, UUID eventId, CalendarEventRequest request) {
        CalendarEvent event = getOwnedEvent(currentUser, eventId);

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        if (request.getCategory() != null) {
            event.setCategory(request.getCategory());
        }
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setAllDay(request.isAllDay());
        event.setLocation(request.getLocation());

        event = calendarEventRepository.save(event);
        return CalendarEventMapper.toDto(event);
    }

    @Transactional
    public void deleteEvent(User currentUser, UUID eventId) {
        CalendarEvent event = getOwnedEvent(currentUser, eventId);
        calendarEventRepository.delete(event);
    }

    CalendarEvent getOwnedEvent(User currentUser, UUID eventId) {
        CalendarEvent event = calendarEventRepository.findById(eventId)
                .orElseThrow(() -> new ApiException("Calendar event not found", HttpStatus.NOT_FOUND));

        if (!event.getUser().getId().equals(currentUser.getId())) {
            throw new ApiException("Calendar event not found", HttpStatus.NOT_FOUND);
        }
        return event;
    }
}
