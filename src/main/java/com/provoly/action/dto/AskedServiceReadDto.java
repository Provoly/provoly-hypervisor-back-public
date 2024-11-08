package com.provoly.action.dto;

import com.provoly.service.ServiceStatus;

public class AskedServiceReadDto extends ActionReadDto {
    private final String name;
    private final String serviceExternalId;
    private final String serviceAdditionalInfo;
    private final ServiceStatus serviceStatus;

    public AskedServiceReadDto(ActionReadDto dto, String name, String serviceExternalId, String serviceAdditionalInfo,
            ServiceStatus serviceStatus) {
        super(dto);
        this.name = name;
        this.serviceExternalId = serviceExternalId;
        this.serviceAdditionalInfo = serviceAdditionalInfo;
        this.serviceStatus = serviceStatus;
    }

    public String getName() {
        return name;
    }

    public String getServiceExternalId() {
        return serviceExternalId;
    }

    public String getServiceAdditionalInfo() {
        return serviceAdditionalInfo;
    }

    public ServiceStatus getServiceStatus() {
        return serviceStatus;
    }
}
