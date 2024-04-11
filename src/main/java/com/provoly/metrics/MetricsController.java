package com.provoly.metrics;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.security.Authenticated;

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
}
