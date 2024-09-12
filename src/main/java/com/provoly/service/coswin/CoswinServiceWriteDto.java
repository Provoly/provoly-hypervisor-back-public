package com.provoly.service.coswin;

import jakarta.validation.constraints.NotNull;

public record CoswinServiceWriteDto(
        @NotNull String jrjrRequester, // username
        @NotNull String jrjrPriority, // priority
        @NotNull String jrjrJobDescription, // request subject
        @NotNull String jrjrLongString, // request type
        @NotNull String jrjrRemarks, // request description
        Integer jrjrNumber1, // GTI
        Integer jrjrNumber2, // GTR
        Integer jrjrNumber3, // GTRP
        String jrjrSourceEquipment, // equipment code,
        String jrjrJobRequestType, // domain
        String jrjrString1 // district
) {
}
