package com.provoly.metrics;

import java.util.List;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.security.Authenticated;

import org.jboss.resteasy.reactive.RestQuery;

@Path("/metrics")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MetricsController {

    private MetricsService metricsService;

    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @Path("/equipments-with-events")
    @GET
    @Authenticated
    public EquipmentWithEventsDto getEquipmentWithEvent(@RestQuery List<String> criticality,
            @RestQuery List<String> category,
            @RestQuery List<String> entity) {
        return metricsService.getEquipmentsWithEventMetrics(criticality, category, entity);
    }

    @Path("/equipments-by-entity")
    @GET
    @Authenticated
    public EquipmentByEntityDto getTotalEquipmentsByEntity(@RestQuery String family) {
        return metricsService.getTotalEquipmentsByEntity(family);
    }
}
