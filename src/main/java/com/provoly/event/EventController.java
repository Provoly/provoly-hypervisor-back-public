package com.provoly.event;

import java.time.Instant;
import java.util.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.provoly.event.dto.*;

import io.quarkus.security.Authenticated;

import org.jboss.resteasy.reactive.RestQuery;

@Path("/events")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EventController {
    private EventService eventService;
    private EventMapper eventMapper;

    public EventController(EventService eventService, EventMapper eventMapper) {
        this.eventService = eventService;
        this.eventMapper = eventMapper;
    }

    @POST
    @Path("/operator")
    @Authenticated
    public Response saveEventOperator(@Valid OperatorEventWriteDto eventDto) {
        var response = eventService.saveOrUpdateEvent(eventDto);
        return Response.ok().status(response == ResponseCode.UPDATED ? 204 : 201).build();
    }

    @POST
    @Path("/report")
    @Authenticated
    public Response saveEventOperator(@Valid ReportEventWriteDto eventDto) {
        var response = eventService.saveOrUpdateEvent(eventDto);
        return Response.ok().status(response == ResponseCode.UPDATED ? 204 : 201).build();
    }

    @POST
    @Path("/alert")
    @Authenticated
    public Response saveEventAlert(@Valid AlertEventWriteDto eventDto) {
        eventService.saveOrUpdateEvent(eventDto);
        return Response.ok().status(Response.Status.CREATED).build();
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
            @RestQuery List<String> familiy) {
        var events = eventService.getEvents(page, pageSize, sort, order, creationDate, criticality, status, category, entity,
                familiy);
        return events.stream()
                .map(event -> eventMapper.mapToEventReadDto(event))
                .toList();
    }

    @Path("/id/{id}")
    @GET
    @Authenticated
    public EventReadDto getEventDetails(UUID id) {
        var event = eventService.getEventDetails(id);
        return eventMapper.mapToEventReadDto(event);
    }

    @Path("/id/{id}/close")
    @PUT
    @Authenticated
    public void closeEvent(UUID id) {
        eventService.closeEventById(id);
    }

    @Path("/summary")
    @GET
    @Authenticated
    public Map<Status, EventsSummariesByStatusDto> getEventSummaries(
            @DefaultValue("5") @Positive @RestQuery int limit,
            @RestQuery String criticality) {
        return eventService.getEventSummariesGroupByStatus(limit, criticality);
    }

}
