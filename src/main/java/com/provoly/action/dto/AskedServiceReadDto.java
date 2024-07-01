package com.provoly.action.dto;

public class AskedServiceReadDto extends ActionReadDto {
    private final String name;
    private final String serviceId;

    public AskedServiceReadDto(ActionReadDto dto, String name, String serviceId) {
        super(dto);
        this.name = name;
        this.serviceId = serviceId;
    }

    public String getName() {
        return name;
    }

    public String getServiceId() {
        return serviceId;
    }
}
