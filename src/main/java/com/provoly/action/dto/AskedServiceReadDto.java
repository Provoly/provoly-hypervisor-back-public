package com.provoly.action.dto;

import com.provoly.service.ServiceStatus;

public class AskedServiceReadDto extends ActionReadDto {
    private final String name;
    private final String serviceExternalId;
    private final ServiceStatus serviceStatus;

    public AskedServiceReadDto(ActionReadDto dto, String name, String serviceExternalId, ServiceStatus serviceStatus) {
        super(dto);
        this.name = name;
        this.serviceExternalId = serviceExternalId;
        this.serviceStatus = serviceStatus;
    }

    public String getName() {
        return name;
    }

    public ServiceStatus getServiceStatus() {
        return serviceStatus;
    }

    public String getServiceExternalId() {
        return serviceExternalId;
    }
}
