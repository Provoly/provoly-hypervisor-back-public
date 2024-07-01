package com.provoly.action.dto;

import java.time.Instant;
import java.util.UUID;

import com.provoly.event.Status;

public class ActionReadDto {
    private final UUID id;
    private final String type;
    private final Status status;
    private final Instant lastModificationDate;

    public ActionReadDto(UUID id, String type, Status status, Instant lastModificationDate) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.lastModificationDate = lastModificationDate;
    }

    public ActionReadDto(ActionReadDto dto) {
        this.id = dto.getId();
        this.type = dto.getType();
        this.status = dto.getStatus();
        this.lastModificationDate = dto.getLastModificationDate();
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

    public Instant getLastModificationDate() {
        return lastModificationDate;
    }
}
