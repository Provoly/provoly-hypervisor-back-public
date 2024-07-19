package com.provoly.metrics;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.security.Authenticated;

import org.jboss.resteasy.reactive.RestQuery;

@Path("/metrics")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MetricsController {

    private final MetricsService metricsService;

    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @Path("/equipments-with-events/EP")
    @GET
    @Authenticated
    public EpEquipmentWithEventsDto getEpEquipmentWithEvent(@RestQuery List<String> criticality,
            @RestQuery List<String> category,
            @RestQuery List<String> entity,
            @RestQuery List<String> place) {

        return metricsService.getEpEquipmentsWithEvent(criticality, category, entity, place);
    }

    @Path("/equipments-with-events/EP/detailed")
    @GET
    @Authenticated
    public EpEquipmentWithEventsDetailedDto getEpEquipmentWithEventDetailed() {
        return metricsService.getEpEquipmentWithEventDetailed();
    }

    @Path("/equipments-with-events/VP")
    @GET
    @Authenticated
    public VpEquipmentWithEventsDto getVpEquipmentWithEvent(@RestQuery List<String> criticality,
            @RestQuery List<String> category,
            @RestQuery List<String> entity,
            @RestQuery List<String> place) {

        return metricsService.getVpEquipmentsWithEvent(criticality, category, entity, place);
    }

    @Path("/equipments-with-events/VP/detailed")
    @GET
    @Authenticated
    public VpEquipmentWithEventsDetailedDto getVpEquipmentWithEventDetailed() {
        return metricsService.getVpEquipmentsWithEventDetailed();
    }

    @Path("/events-by-equipments")
    @GET
    @Authenticated
    public Collection<EventsByEquipment> getEventsByEquipments(
            @RestQuery String category,
            @RestQuery String domain,
            @RestQuery @Positive @DefaultValue("10") int limit,
            @RestQuery Instant date,
            @RestQuery List<String> place,
            @RestQuery List<String> entity,
            @RestQuery List<String> criticality,
            @RestQuery List<String> family) {
        return metricsService.getEventsByEquipments(domain, category, limit, date, place, entity, criticality, family);
    }

    @Path("/equipments-by-entity")
    @GET
    @Authenticated
    public EquipmentByEntityDto getTotalEquipmentsByEntity(@RestQuery String family) {
        return metricsService.getTotalEpEquipmentsByEntity(family);
    }

    @Path("/services/closed/{interval}")
    @GET
    @Authenticated
    public Collection<AggregateServiceDto> aggregateDoneServices(DateInterval interval,
            @RestQuery Instant date,
            @RestQuery String domain,
            @RestQuery @Positive @DefaultValue("24") int buckets,
            @RestQuery List<String> family,
            @RestQuery List<String> entity,
            @RestQuery List<String> place) {
        return metricsService.aggregateDoneServices(interval, date, domain, buckets, family, entity, place);
    }

    @Path("/events/anomaly")
    @GET
    @Authenticated
    public Map<String, Long> getAnomalyEventsGroupedBySubCategories(
            @RestQuery String domain,
            @RestQuery Instant date,
            @RestQuery String status,
            @RestQuery List<String> place,
            @RestQuery List<String> entity,
            @RestQuery List<String> criticality,
            @RestQuery List<String> family,
            @RestQuery String equipment) {
        return metricsService.getAnomalyEventsBySubCategories(domain, date, status, place, entity, criticality, family,
                equipment);
    }

    @Path("/events/anomalies-by-entity")
    @GET
    @Authenticated
    public Collection<AnomalyQueryResult> getAnomalyEventsGroupedBySubCategoriesAndEntities(
            @RestQuery String domain,
            @RestQuery Instant startDate) {
        return metricsService.getAnomalyEventsGroupedBySubCategoriesAndEntities(domain, startDate);
    }

    @Path("/aggregate/anomalies/{interval}")
    @GET
    @Authenticated
    public Collection<AggregateServiceDto> aggregateAnomaliesEvents(
            DateInterval interval,
            @RestQuery @Positive @DefaultValue("12") int buckets,
            @RestQuery String domain,
            @RestQuery Instant startDate,
            @RestQuery List<String> place,
            @RestQuery List<String> entity,
            @RestQuery List<String> criticality,
            @RestQuery List<String> family) {
        return metricsService.aggregateAnomaliesEvents(interval, buckets, domain, startDate, place, entity, criticality,
                family);
    }

}
