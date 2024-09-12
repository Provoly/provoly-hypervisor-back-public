package com.provoly.action.dto;

public class AskedServiceReadDto extends ActionReadDto {
    private final String name;
    private final String serviceExternalId;

    public AskedServiceReadDto(ActionReadDto dto, String name, String serviceExternalId) {
        super(dto);
        this.name = name;
        this.serviceExternalId = serviceExternalId;
    }

    public String getName() {
        return name;
    }

    public String getServiceExternalId() {
        return serviceExternalId;
    }
}
