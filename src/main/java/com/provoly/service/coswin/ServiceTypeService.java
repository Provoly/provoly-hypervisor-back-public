package com.provoly.service.coswin;

import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ServiceTypeService {

    private final ServiceTypeDatabaseReader databaseReader;

    public ServiceTypeService(ServiceTypeDatabaseReader databaseReader) {
        this.databaseReader = databaseReader;
    }

    @Transactional
    public Stream<ServiceType> getServicesType(String domain) {
        return databaseReader.getAllServicesType(domain);
    }

    @Transactional
    public ServiceType getServiceType(String type) {
        return databaseReader.getServiceType(type);
    }
}
