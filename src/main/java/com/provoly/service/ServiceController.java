package com.provoly.service;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.security.Authenticated;

@Path("/services")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ServiceController {

    private final ServiceService serviceService;
    private final ServiceMapper serviceMapper;

    public ServiceController(ServiceService serviceService, ServiceMapper serviceMapper) {
        this.serviceService = serviceService;
        this.serviceMapper = serviceMapper;
    }

    @POST
    @Authenticated
    public void saveOrUpdateServices(@Valid Collection<ServiceWriteDto> services) {
        serviceService.saveOrUpdateServices(services);
    }

    @POST
    @Path("/external/id/{actionId}")
    @Authenticated
    public String createExternalService(UUID actionId, @Valid ExternalServiceWriteDto dto) throws IOException {
        return serviceService.createExternalService(actionId, dto);
    }

    @GET
    @Authenticated
    @Transactional
    public Collection<ServiceReadDto> getServices() {
        var services = serviceService.getServices();
        return serviceMapper.mapToServiceReadDtos(services);
    }
}
