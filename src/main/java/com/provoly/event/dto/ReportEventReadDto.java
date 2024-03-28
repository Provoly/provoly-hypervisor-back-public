package com.provoly.event.dto;

import com.provoly.event.ReportCategory;

public class ReportEventReadDto extends EventReadDto {
    private String externalSourceRef;
    private ReportCategory category;

    public ReportEventReadDto(EventReadDto dto, String externalSourceRef, ReportCategory category) {
        super(dto);
        this.externalSourceRef = externalSourceRef;
        this.category = category;
    }

    public String getExternalSourceRef() {
        return externalSourceRef;
    }

    public ReportCategory getCategory() {
        return category;
    }
}
