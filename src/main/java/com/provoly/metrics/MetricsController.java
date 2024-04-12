package com.provoly.metrics;

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
    public EquipmentWithEventsDto getEquipmentWithEvent() {
        return metricsService.getEquipmentsWithEventMetrics();
    }

    @Path("/equipments-by-entity")
    @GET
    @Authenticated
    public EquipmentByEntityDto getEquipmentByEntity(@RestQuery String family) {
        return metricsService.getEquipmentByEntity(family);
    }
}
