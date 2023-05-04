package com.wojto.eventservice.rest;

import com.wojto.eventservice.api.EventService;
import com.wojto.eventservice.dto.Event;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/events")
@Api(value = "Event Service API", tags = "Event Service")
public class EventRestController {

    private final EventService eventService;

    @GetMapping("/{id}")
    @ApiOperation(value = "Get an event by ID",
            notes = "Returns the event with the specified ID",
            response = Event.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved event", response = Event.class),
            @ApiResponse(code = 404, message = "The event with the specified ID was not found")
    })
    public ResponseEntity<EntityModel<Event>> getEventById(
            @ApiParam(value = "The ID of the event to retrieve",
                    required = true,
                    type = "Long")
            @PathVariable Long id) {
        Event event = eventService.getEvent(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + id));

        Link selfLink = linkTo(methodOn(EventRestController.class).getEventById(id)).withSelfRel();
        EntityModel<Event> eventModel = EntityModel.of(event, selfLink);

        return ResponseEntity.ok(eventModel);
    }

    @GetMapping
    @ApiOperation(value = "Get a paginated list of all Events",
            notes = "Returns a paginated list of all Events. The response contains links to first, previous, next and last page.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved list of Events", response = List.class),
            @ApiResponse(code = 404, message = "There was an error during retrieval of Events")
    })
    public ResponseEntity<PagedModel<EntityModel<Event>>> getAllEvents(
            @ApiParam(value = "The page number to be retrieved",
                    required = true,
                    type = "int",
                    defaultValue = "0")
            @RequestParam(defaultValue = "0") int pageNumber,
            @ApiParam(value = "The number of Events on a single page",
                    required = true,
                    type = "int",
                    defaultValue = "10")
            @RequestParam(defaultValue = "10") int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Event> page = eventService.getAllEvents(pageable);

        List<EntityModel<Event>> events = getListOfEntityModelEventsFromPage(page);
        PagedModel<EntityModel<Event>> pagedEvents = getPageModelFromPageAndEvents(page, events);

        // Maybe reassign pagedEvents to new object
        addLinksToOtherPages(pageable, page, pagedEvents);

        return ResponseEntity.ok(pagedEvents);
    }

    @GetMapping("/byTitle")
    @ApiOperation(value = "Get a paginated list of Events with a specific title",
            notes = "Returns a paginated list of Events containing a specified phrase in their titles. The response contains links to first, previous, next and last page.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved list of Events", response = List.class),
            @ApiResponse(code = 404, message = "There was an error during retrieval of Events")
    })
    public ResponseEntity<PagedModel<EntityModel<Event>>> getAllEventsByTitle(
            @ApiParam(value = "The phrase to search for in the Event titles",
                    required = true,
                    type = "String")
            @RequestParam String title,
            @ApiParam(value = "The page number to be retrieved",
                    required = true,
                    type = "int",
                    defaultValue = "0")
            @RequestParam(defaultValue = "0") int pageNumber,
            @ApiParam(value = "The number of Events on a single page",
                    required = true,
                    type = "int",
                    defaultValue = "10")
            @RequestParam(defaultValue = "10") int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Event> page = eventService.getAllEventsByTitle(title, pageable);

        List<EntityModel<Event>> events = getListOfEntityModelEventsFromPage(page);
        PagedModel<EntityModel<Event>> pagedEvents = getPageModelFromPageAndEvents(page, events);

        // Maybe reassign pagedEvents to new object
        addLinksToOtherPages(title, pageable, page, pagedEvents);

        return ResponseEntity.ok(pagedEvents);
    }

    @PostMapping
    @ApiOperation(value = "Post a new Event",
            notes = "Saves the provided Event to the database. Returns the created Event (containing generated ID).")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created the Event", response = Event.class),
            @ApiResponse(code = 404, message = "There was an error during creating the Event")
    })
    public ResponseEntity<EntityModel<Event>> createEvent(
            @ApiParam(value = "An Event to be created in JSON format. Doesn't need an ID.",
                    required = true,
                    type = "Event")
            @RequestBody Event newEvent) {
        Event savedEvent = eventService.createEvent(newEvent);

        Link selfLink = linkTo(methodOn(EventRestController.class).getEventById(savedEvent.getId())).withSelfRel();
        EntityModel<Event> eventModel = EntityModel.of(savedEvent, selfLink);

        return ResponseEntity.created(selfLink.toUri()).body(eventModel);
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "Update an existing Event",
            notes = "Saves the modified Event to the database, and returns the Event.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully updated the Event", response = Event.class),
            @ApiResponse(code = 404, message = "Event with provided ID doesn't exist.")
    })
    public ResponseEntity<EntityModel<Event>> updateEventById(
            @ApiParam(value = "ID of the Event that will be modified",
                    required = true,
                    type = "Long")
            @PathVariable Long id,
            @ApiParam(value = "The Event to be modified",
                    required = true,
                    type = "Event")
            @RequestBody Event event) {
        Event updatedEvent = eventService.updateEvent(id, event);

        Link selfLink = linkTo(methodOn(EventRestController.class).getEventById(id)).withSelfRel();
        EntityModel<Event> eventModel = EntityModel.of(updatedEvent, selfLink);

        return ResponseEntity.ok(eventModel);
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "Delete an existing Event by ID",
            notes = "Deletes the Event that has the provided ID.")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successfully deleted the Event"),
            @ApiResponse(code = 404, message = "Event with provided ID doesn't exist.")
    })
    public ResponseEntity<?> deleteEventById(
            @ApiParam(value = "ID of the Event that will be deleted",
                    required = true,
                    type = "Long")
            @PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }


    private static List<EntityModel<Event>> getListOfEntityModelEventsFromPage(Page<Event> page) {
        return page.getContent().stream()
                .map(event -> EntityModel.of(event, linkTo(methodOn(EventRestController.class).getEventById(event.getId())).withSelfRel()))
                .collect(Collectors.toList());
    }

    private static PagedModel<EntityModel<Event>> getPageModelFromPageAndEvents(Page<Event> page, List<EntityModel<Event>> events) {
        return PagedModel.of(events, new PagedModel.PageMetadata(page.getSize(), page.getNumber(), page.getTotalElements()));
    }

    private static void addLinksToOtherPages(Pageable pageable, Page<Event> page, PagedModel<EntityModel<Event>> pagedEvents) {
        pagedEvents.add(linkTo(methodOn(EventRestController.class).getAllEvents(pageable.first().getPageNumber(), pageable.getPageSize())).withRel(IanaLinkRelations.FIRST));
        if (page.hasPrevious()) {
            pagedEvents.add(linkTo(methodOn(EventRestController.class).getAllEvents(pageable.previousOrFirst().getPageNumber(), pageable.getPageSize())).withRel(IanaLinkRelations.PREV));
        }
        if (page.hasNext()) {
            pagedEvents.add(linkTo(methodOn(EventRestController.class).getAllEvents(pageable.next().getPageNumber(), pageable.getPageSize())).withRel(IanaLinkRelations.NEXT));
        }
        if (page.getTotalPages() > 1 && !page.isLast()) {
            pagedEvents.add(linkTo(methodOn(EventRestController.class).getAllEvents(page.getTotalPages() - 1, pageable.getPageSize())).withRel(IanaLinkRelations.LAST));
        }
        pagedEvents.add(linkTo(methodOn(EventRestController.class).getAllEvents(pageable.getPageNumber(), pageable.getPageSize())).withSelfRel());
    }

    private static void addLinksToOtherPages(String title, Pageable pageable, Page<Event> page, PagedModel<EntityModel<Event>> pagedEvents) {
        pagedEvents.add(linkTo(methodOn(EventRestController.class).getAllEventsByTitle(title, pageable.first().getPageNumber(), pageable.getPageSize())).withRel(IanaLinkRelations.FIRST));
        if (page.hasPrevious()) {
            pagedEvents.add(linkTo(methodOn(EventRestController.class).getAllEventsByTitle(title, pageable.previousOrFirst().getPageNumber(), pageable.getPageSize())).withRel(IanaLinkRelations.PREV));
        }
        if (page.hasNext()) {
            pagedEvents.add(linkTo(methodOn(EventRestController.class).getAllEventsByTitle(title, pageable.next().getPageNumber(), pageable.getPageSize())).withRel(IanaLinkRelations.NEXT));
        }
        if (page.getTotalPages() > 1 && !page.isLast()) {
            pagedEvents.add(linkTo(methodOn(EventRestController.class).getAllEventsByTitle(title, page.getTotalPages() - 1, pageable.getPageSize())).withRel(IanaLinkRelations.LAST));
        }
        pagedEvents.add(linkTo(methodOn(EventRestController.class).getAllEventsByTitle(title, pageable.getPageNumber(), pageable.getPageSize())).withSelfRel());
    }

}
