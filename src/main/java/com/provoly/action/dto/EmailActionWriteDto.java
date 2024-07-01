package com.provoly.action.dto;

import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.provoly.event.Status;

public class EmailActionWriteDto extends ActionWriteDto {

    @NotNull
    @NotBlank
    private final String name;

    @NotNull
    @NotBlank
    @Email(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,3}$")
    private final String email;

    public EmailActionWriteDto(UUID id, String type, Status status, String name, String email) {
        super(id, type, status);
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
