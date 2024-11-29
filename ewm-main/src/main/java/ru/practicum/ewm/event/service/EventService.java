package ru.practicum.ewm.event.service;

import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.model.EventState;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    List<EventFullDto> findAllAdminEvents(List<Long> users, List<EventState> states, List<Long> categories, LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    List<EventShortDto> findAllPublicEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, int from, int size);

    EventFullDto findPublicEventById(long id);

    List<EventShortDto> findAllEventsByUserId(Long userId, int from, int size);

    EventFullDto createEvent(NewEventDto newEventDto, long userId);

    EventFullDto findEventByUser(Long userId, Long eventId);

    EventFullDto updateEventById(long userId, long eventId, UpdateEventUserRequest updateEventDto);
}
