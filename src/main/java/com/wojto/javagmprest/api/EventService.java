package com.wojto.javagmprest.api;

import com.wojto.javagmprest.dto.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface EventService {

    public Optional<Event> getEvent(long id);

    public Page<Event> getAllEvents(Pageable pageable);

    public Page<Event> getAllEventsByTitle(String title, Pageable pageable);

    public Event createEvent(Event event);

    public Event updateEvent(long id, Event event);

    public void deleteEvent(long id);

}
