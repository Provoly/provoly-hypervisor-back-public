package com.provoly.event.dto;

public class ReportEventReadDto extends EventReadDto {
    private String externalSourceRef;

    public ReportEventReadDto(EventReadDto dto, String externalSourceRef) {
        super(dto);
        this.externalSourceRef = externalSourceRef;
    }

    public String getExternalSourceRef() {
        return externalSourceRef;
    }

}
