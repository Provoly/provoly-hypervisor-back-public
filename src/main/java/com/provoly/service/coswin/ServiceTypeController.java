package com.provoly.service.coswin;

import java.util.Collection;

import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.provoly.user.Role;

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
    @RolesAllowed({ Role.STR_SERVICE_READ })
    @Transactional
    public Collection<ServiceTypeReadDto> getServiceTypes(@RestQuery String domain) {
        logger.infof("get service types with domain %s", domain);
        var types = serviceTypeService.getServicesType(domain);
        return serviceMapper.mapToServiceTypeReadDtos(types);
    }
}
