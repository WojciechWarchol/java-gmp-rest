package com.wojto.eventservice.impl;

import com.wojto.eventservice.api.EventService;
import com.wojto.eventservice.dto.Event;
import com.wojto.eventservice.dto.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;


    @Override
    public Optional<Event> getEvent(long id) {
        return eventRepository.findById(id);
    }

    @Override
    public Page<Event> getAllEvents(Pageable pageable) {
        return eventRepository.findAll(pageable);
    }

    @Override
    public Page<Event> getAllEventsByTitle(String title, Pageable pageable) {
        return eventRepository.findByTitleContaining(title, pageable);
    }

    @Override
    public Event createEvent(Event event) {
        return eventRepository.save(event);
    }

    @Override
    public Event updateEvent(long id, Event event) {
        Event updatedEvent = eventRepository.findById(id).map(foundEvent -> {
                    foundEvent.setTitle(event.getTitle());
                    foundEvent.setEventType(event.getEventType());
                    foundEvent.setPlace(event.getPlace());
                    foundEvent.setSpeaker(event.getSpeaker());
                    foundEvent.setDateTime(event.getDateTime());
                    return foundEvent;
                })
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + id));
        return eventRepository.save(updatedEvent);
    }

    @Override
    public void deleteEvent(long id) {
        if (eventRepository.existsById(id)) {
            eventRepository.deleteById(id);
        }
    }
}
