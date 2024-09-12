package com.provoly.service.coswin;

import java.util.Collection;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.security.Authenticated;

import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestQuery;

@Path("/servicesType")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ServiceTypeController {
    private final Logger logger;
    private final ServiceTypeService serviceTypeService;
    private final ServiceTypeMapper serviceMapper;

    public ServiceTypeController(Logger logger, ServiceTypeService serviceTypeService, ServiceTypeMapper serviceMapper) {
        this.logger = logger;
        this.serviceTypeService = serviceTypeService;
        this.serviceMapper = serviceMapper;
    }

    @GET
    @Authenticated
    @Transactional
    public Collection<ServiceTypeReadDto> getServiceTypes(@RestQuery String domain) {
        logger.infof("get service types with domain %s", domain);
        var types = serviceTypeService.getServicesType(domain);
        return serviceMapper.mapToServiceTypeReadDtos(types);
    }
}
