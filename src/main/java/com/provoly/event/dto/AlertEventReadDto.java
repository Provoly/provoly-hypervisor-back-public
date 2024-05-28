package com.provoly.event.dto;

public class AlertEventReadDto extends EventReadDto {
    private String externalSourceRef;

    public AlertEventReadDto(EventReadDto dto, String externalSourceRef) {
        super(dto);
        this.externalSourceRef = externalSourceRef;
    }

    public String getExternalSourceRef() {
        return externalSourceRef;
    }

}
