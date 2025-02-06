package com.provoly.event.dto;

import java.time.Instant;

import com.provoly.comment.CommentReadDto;
import com.provoly.equipment.EquipmentShortDto;
import com.provoly.event.Criticality;
import com.provoly.event.Status;

public record JournalEventDto(int id,
        String name,
        String address,
        EquipmentShortDto equipment,
        Criticality criticality,
        Status status,
        String externalSourceRef,
        float procedureProgress,
        String category,
        String subCategory,
        Instant creationDate,
        Instant lastModificationDate,
        long linkedEvents,
        Integer procedureId,
        String domain,
        Instant closeDate,
        CommentReadDto comment,
        String externalId) {
}
