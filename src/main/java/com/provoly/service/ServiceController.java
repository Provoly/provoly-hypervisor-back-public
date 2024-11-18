package com.provoly.service;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import com.provoly.user.Role;

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
    @RolesAllowed({ Role.STR_SERVICE_WRITE })
    public void saveOrUpdateServices(@Valid Collection<ServiceWriteDto> services) {
        serviceService.saveOrUpdateServices(services);
    }

    @POST
    @Path("/external/id/{actionId}")
    @RolesAllowed({ Role.STR_SERVICE_EXTERNAL_WRITE })
    public Map<String, String> createExternalService(UUID actionId, @Valid ExternalServiceWriteDto dto) throws IOException {
        return serviceService.createExternalService(actionId, dto);
    }

    @GET
    @RolesAllowed({ Role.STR_SERVICE_READ })
    public Collection<ServiceReadDto> getServices() {
        var services = serviceService.getServices();
        return serviceMapper.mapToServiceReadDtos(services);
    }
}
