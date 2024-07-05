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

        return metricsService.getEpEquipmentsWithEventMetrics(criticality, category, entity, place);
    }

    @Path("/equipments-with-events/VP")
    @GET
    @Authenticated
    public VpEquipmentWithEventsDto getVpEquipmentWithEvent(@RestQuery List<String> criticality,
            @RestQuery List<String> category,
            @RestQuery List<String> entity,
            @RestQuery List<String> place) {

        return metricsService.getVpEquipmentsWithEventMetrics(criticality, category, entity, place);
    }

    @Path("/equipments-by-entity")
    @GET
    @Authenticated
    public EquipmentByEntityDto getTotalEquipmentsByEntity(@RestQuery String family) {
        return metricsService.getTotalEquipmentsByEntity(family);
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
            @RestQuery String status) {
        return metricsService.getAnomalyEventsGroupedBySubCategories(domain, date, status);
    }

}
