package ru.practicum.ewm.event.service;

import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.StatsClient;
import ru.practicum.ewm.ViewStatsDto;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.model.QEvent;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.DateTimeException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final StatsClient statsClient;

    @Override
    public List<EventFullDto> findAllAdminEvents(List<Long> users, List<EventState> states,
                                                 List<Long> categories, LocalDateTime rangeStart,
                                                 LocalDateTime rangeEnd, int from, int size) {
        List<Event> events = new ArrayList<>();
        PageRequest pageRequest = PageRequest.of(from, size);
        BooleanBuilder builder = new BooleanBuilder();
        if (users != null && !users.isEmpty()) {
            builder.and(QEvent.event.initiator.id.in(users));
        }
        if (states != null && !states.isEmpty()) {
            builder.and(QEvent.event.state.in(states));
        }
        if (categories != null && !categories.isEmpty()) {
            builder.and(QEvent.event.category.id.in(categories));
        }
        if (rangeStart != null) {
            builder.and(QEvent.event.eventDate.after(rangeStart));
        }
        if (rangeEnd != null) {
            builder.and(QEvent.event.eventDate.before(rangeEnd));
        }
        if (rangeStart == null && rangeEnd == null) {
            builder.and(QEvent.event.eventDate.after(LocalDateTime.now()));
        }
        if (builder.getValue() != null) {
            events = eventRepository.findAll(builder.getValue(), pageRequest).getContent();
        } else {
            events = eventRepository.findAll(pageRequest).getContent();
        }
        setViews(events);

        List<EventFullDto> eventFullDtoList = events.stream()
                .map(EventMapper::toEventFullDto)
                .collect(Collectors.toList());

        return eventFullDtoList;
    }


    @Override
    public List<EventShortDto> findAllPublicEvents(String text, List<Long> categories,
                                                   Boolean paid, LocalDateTime rangeStart,
                                                   LocalDateTime rangeEnd, Boolean onlyAvailable,
                                                   String sort, int from, int size) {
        List<Event> events = new ArrayList<>();

        PageRequest pageRequest = PageRequest.of(from, size, Sort.by("eventDate").ascending());
        BooleanBuilder builder = new BooleanBuilder();

        if ((rangeStart != null) && (rangeEnd != null) && (rangeStart.isAfter(rangeEnd))) {
            throw new DateTimeException("Start time cannot be after end time");
        }

        if (text != null && !text.isBlank()) {
            builder.and(QEvent.event.annotation.containsIgnoreCase(text.toLowerCase())
                    .or(QEvent.event.description.containsIgnoreCase(text.toLowerCase())));
        }
        if (paid != null) {
            builder.and(QEvent.event.paid.eq(paid));
        }
        if (categories != null && !categories.isEmpty()) {
            builder.and(QEvent.event.category.id.in(categories));
        }
        if (rangeStart != null) {
            builder.and(QEvent.event.eventDate.after(rangeStart));
        }
        if (rangeEnd != null) {
            builder.and(QEvent.event.eventDate.before(rangeEnd));
        }
        if (onlyAvailable) {
            builder.and(QEvent.event.participantLimit.eq(0L))
                    .or(QEvent.event.participantLimit.gt(QEvent.event.confirmedRequests));
        }
        if (builder.getValue() != null) {
            events = eventRepository.findAll(builder.getValue(), pageRequest).getContent();
        } else {
            events = eventRepository.findAll(pageRequest).getContent();
        }
        setViews(events);

        return events.stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateEvent) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event id: " + eventId + "not found."));

        if (updateEvent.getStateAction() != null) {
            if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1)) &&
                    updateEvent.getStateAction().equals(StateActionAdmin.PUBLISH_EVENT)) {
                throw new DateTimeException("The start date of the event is too early.");
            }

            if (updateEvent.getStateAction().equals(StateActionAdmin.PUBLISH_EVENT)) {
                if (!event.getState().equals(EventState.PENDING)) {
                    throw new ValidationException("Event cannot be published if not in pending state.");
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else {
                if (event.getState().equals(EventState.PUBLISHED)) {
                    throw new ValidationException("Event cannot be rejected in published state.");
                }
                event.setState(EventState.CANCELED);
            }
        }

        if (updateEvent.getEventDate() != null) {
            if (updateEvent.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                throw new DateTimeException("Event cannot be published earlier than 1 hour before date of event.");
            } else {
                event.setEventDate(updateEvent.getEventDate());
            }
        }
        if (updateEvent.getAnnotation() != null && !updateEvent.getAnnotation().isBlank()) {
            event.setAnnotation(updateEvent.getAnnotation());
        }
        if (updateEvent.getCategory() != null) {
            Category category = categoryRepository.findById(updateEvent.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category id: " + updateEvent.getCategory() + "not found."));
            event.setCategory(category);
        }
        if (updateEvent.getDescription() != null && !updateEvent.getDescription().isBlank()) {
            event.setDescription(updateEvent.getDescription());
        }
        if (updateEvent.getLocation() != null) {
            event.setLocation(updateEvent.getLocation());
        }
        if (updateEvent.getPaid() != null) {
            event.setPaid(updateEvent.getPaid());
        }
        if (updateEvent.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEvent.getParticipantLimit());
        }
        if (updateEvent.getRequestModeration() != null) {
            event.setRequestModeration(updateEvent.getRequestModeration());
        }
        if (updateEvent.getTitle() != null && !updateEvent.getTitle().isBlank()) {
            event.setTitle(updateEvent.getTitle());
        }
        setViews(List.of(event));
        return EventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public EventFullDto findPublicEventById(long id) {
        Event event = eventRepository.findByIdAndState(id, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event id: " + id + " not found."));
        setViews(List.of(event));
        return EventMapper.toEventFullDto(event);
    }

    @Override
    public List<EventShortDto> findAllEventsByUserId(Long userId, int from, int size) {
        PageRequest pageRequest = PageRequest.of(from, size);
        List<Event> events = eventRepository.findByInitiatorId(userId, pageRequest);
        setViews(events);

        return events.stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public EventFullDto createEvent(NewEventDto newEventDto, long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User id: " + userId + " not found"));
        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Category id: " + newEventDto.getCategory() + "not found."));
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().minusHours(2))) {
            throw new DateTimeException("Event cannot be published earlier than 1 hour before date of event.");
        }
        if (newEventDto.getPaid() == null) {
            newEventDto.setPaid(false);
        }
        if (newEventDto.getRequestModeration() == null) {
            newEventDto.setRequestModeration(true);
        }
        if (newEventDto.getParticipantLimit() == null) {
            newEventDto.setParticipantLimit(0L);
        }
        Event event = EventMapper.toEvent(newEventDto);
        event.setInitiator(user);
        event.setCategory(category);
        event.setState(EventState.PENDING);
        event.setCreatedOn(LocalDateTime.now());
        event.setConfirmedRequests(0L);
        return EventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public EventFullDto findEventByUser(Long userId, Long eventId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id: " + userId + " not found");
        }
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id: " + eventId + " not found"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ValidationException("User with id: " + userId + " is not initiator of the event");
        }
        setViews(List.of(event));
        return EventMapper.toEventFullDto(event);
    }

    @Transactional
    @Override
    public EventFullDto updateEventById(long userId, long eventId, UpdateEventUserRequest updateEvent) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event id: " + eventId + "not found."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User id: " + userId + " not found"));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ValidationException("User with id: " + userId + " is not initiator of the event");
        }

        if (updateEvent.getStateAction() != null) {
            switch (updateEvent.getStateAction()) {
                case CANCEL_REVIEW -> event.setState(EventState.CANCELED);
                case SEND_TO_REVIEW -> event.setState(EventState.PENDING);
            }
        }

        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new ValidationException("Published event couldn't be updated");
        }

        if (updateEvent.getEventDate() != null) {
            if (updateEvent.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                throw new DateTimeException("Event cannot be published earlier than 1 hour before date of event.");
            } else {
                event.setEventDate(updateEvent.getEventDate());
            }
        }
        if (updateEvent.getAnnotation() != null && !updateEvent.getAnnotation().isBlank()) {
            event.setAnnotation(updateEvent.getAnnotation());
        }
        if (updateEvent.getCategory() != null) {
            Category category = categoryRepository.findById(updateEvent.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category id: " + updateEvent.getCategory() + "not found."));
            event.setCategory(category);
        }
        if (updateEvent.getDescription() != null && !updateEvent.getDescription().isBlank()) {
            event.setDescription(updateEvent.getDescription());
        }
        if (updateEvent.getLocation() != null) {
            event.setLocation(updateEvent.getLocation());
        }
        if (updateEvent.getPaid() != null) {
            event.setPaid(updateEvent.getPaid());
        }
        if (updateEvent.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEvent.getParticipantLimit());
        }
        if (updateEvent.getRequestModeration() != null) {
            event.setRequestModeration(updateEvent.getRequestModeration());
        }
        if (updateEvent.getTitle() != null && !updateEvent.getTitle().isBlank()) {
            event.setTitle(updateEvent.getTitle());
        }
        setViews(List.of(event));
        return EventMapper.toEventFullDto(event);
    }

    private void setViews(List<Event> events) {
        if (events.isEmpty()) {
            return;
        }
        List<String> uri = events.stream()
                .map(event -> "/events/" + event.getId())
                .toList();
        List<ViewStatsDto> viewStatsDto = statsClient.getStats(LocalDateTime.now().minusYears(3), LocalDateTime.now(), uri, true);
        Map<String, Long> uriHitMap = viewStatsDto.stream()
                .collect(Collectors.toMap(ViewStatsDto::getUri, ViewStatsDto::getHits));
        for (Event event : events) {
            event.setViews(uriHitMap.getOrDefault("/events/" + event.getId(), 0L));
        }
    }
}
