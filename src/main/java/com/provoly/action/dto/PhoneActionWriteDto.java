package com.provoly.action.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import com.provoly.event.Status;

public class PhoneActionWriteDto extends ActionWriteDto {

    @NotNull
    @NotBlank
    private String name;

    @NotNull
    @NotBlank
    @Pattern(regexp = "^(?:(?:\\+|00)33|0)[1-9][0-9]{8}$", message = "'number' must match +33xxx, 0033xxx or 0xxx")
    private String number;

    public PhoneActionWriteDto(UUID id, String type, Status status, String name, String number) {
        super(id, type, status);
        this.name = name;
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }
}
