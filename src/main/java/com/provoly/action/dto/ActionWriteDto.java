package com.provoly.action.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.provoly.event.Status;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true, defaultImpl = ActionWriteDto.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = EmailActionWriteDto.class, name = "EMAIL"),
        @JsonSubTypes.Type(value = OtherActionWriteDto.class, name = "OTHER"),
        @JsonSubTypes.Type(value = AskedServiceWriteDto.class, name = "ASKED_SERVICE"),
        @JsonSubTypes.Type(value = PhoneActionWriteDto.class, name = "PHONE"),
        @JsonSubTypes.Type(value = PhoneActionWriteDto.class, name = "SMS")
})
public class ActionWriteDto {

    @NotNull
    private final UUID id;

    @NotNull
    @NotBlank
    private final String type;

    private final Status status;

    public ActionWriteDto(UUID id, String type, Status status) {
        this.id = id;
        this.type = type;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public Status getStatus() {
        return status;
    }
}
