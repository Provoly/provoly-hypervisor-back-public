package com.provoly.event.dto;

import com.provoly.event.AlertCategory;

public class AlertEventReadDto extends EventReadDto {
    private String externalSourceRef;
    private AlertCategory category;

    public AlertEventReadDto(EventReadDto dto, String externalSourceRef, AlertCategory category) {
        super(dto);
        this.externalSourceRef = externalSourceRef;
        this.category = category;
    }

    public String getExternalSourceRef() {
        return externalSourceRef;
    }

    public AlertCategory getCategory() {
        return category;
    }
}
