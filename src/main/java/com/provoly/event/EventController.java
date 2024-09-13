package com.provoly.event;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import com.provoly.comment.CommentReadDto;
import com.provoly.comment.CommentService;
import com.provoly.comment.CommentWriteDto;
import com.provoly.event.dto.EventReadDto;
import com.provoly.event.dto.EventWriteDto;
import com.provoly.event.dto.EventsSummariesByStatusDto;
import com.provoly.user.Role;

import io.quarkus.security.Authenticated;

import org.jboss.resteasy.reactive.RestQuery;

@Path("/events")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EventController {
    private final EventService eventService;
    private final EventMapper eventMapper;
    private final CommentService commentService;

    public EventController(EventService eventService, EventMapper eventMapper, CommentService commentService) {
        this.eventService = eventService;
        this.eventMapper = eventMapper;
        this.commentService = commentService;
    }

    @POST
    @RolesAllowed({ Role.STR_EVENT_WRITE })
    public EventReadDto saveEvent(@Valid EventWriteDto eventDto) {
        var event = eventService.saveEvent(eventDto);
        return eventMapper.mapToEventReadDto(event);
    }

    @PUT
    @Path("/id/{id}")
    @RolesAllowed({ Role.STR_EVENT_WRITE })
    public void updateEvent(Integer id, @Valid EventWriteDto eventDto) {
        eventService.updateEvent(id, eventDto);
    }

    @GET
    @Authenticated
    public Collection<EventReadDto> getEvents(
            @DefaultValue("1") @Positive @RestQuery int page,
            @DefaultValue("20") @Positive @RestQuery int pageSize,
            @RestQuery String sort,
            @DefaultValue("ASC") @RestQuery String order,
            @RestQuery Instant creationDate,
            @RestQuery List<String> criticality,
            @RestQuery List<String> status,
            @RestQuery List<String> category,
            @RestQuery List<String> entity,
            @RestQuery List<String> family,
            @RestQuery String search) {
        var events = eventService.getEvents(page,
                pageSize,
                sort,
                order,
                creationDate,
                criticality,
                status,
                category,
                entity,
                family,
                search);
        return events.stream()
                .map(eventMapper::mapToEventReadDto)
                .toList();
    }

    @Path("/id/{id}")
    @GET
    @RolesAllowed({ Role.STR_EVENT_READ })
    public EventReadDto getEventDetails(Integer id) {
        var event = eventService.getEventDetails(id);
        return eventMapper.mapToEventReadDto(event);
    }

    @Path("/id/{id}/close")
    @PUT
    @RolesAllowed({ Role.STR_EVENT_WRITE })
    public void closeEvent(Integer id, @Valid CommentWriteDto dto) {
        commentService.closeEventWithComment(id, dto);
    }

    @Path("/summary")
    @GET
    @Authenticated
    public Map<Status, EventsSummariesByStatusDto> getEventSummaries(
            @DefaultValue("5") @Positive @RestQuery int limit,
            @RestQuery String criticality) {
        return eventService.getEventSummariesGroupByStatus(limit, criticality);
    }

    @Path("/id/{id}/comments")
    @PUT
    @RolesAllowed({ Role.STR_EVENT_WRITE })
    public void saveOrUpdateCommentForEvent(Integer id, @Valid CommentWriteDto comment) {
        commentService.saveOrUpdateCommentForEvent(id, comment);
    }

    @Path("/id/{id}/comments")
    @GET
    @RolesAllowed({ Role.STR_EVENT_READ })
    public List<CommentReadDto> getCommentsForEvent(Integer id) {
        return commentService.getCommentsForEvent(id);
    }

}
