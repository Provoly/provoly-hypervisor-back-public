package com.provoly.service.coswin;

import java.util.Collection;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceTypeMapper {

    public ServiceTypeReadDto mapToServiceTypeReadDto(ServiceType type) {
        return new ServiceTypeReadDto(
                type.getType(),
                type.getDomain(),
                type.getGti(),
                type.getGtr(),
                type.getGtrp());
    }

    public Collection<ServiceTypeReadDto> mapToServiceTypeReadDtos(Stream<ServiceType> types) {
        return types.map(this::mapToServiceTypeReadDto).toList();
    }
}
