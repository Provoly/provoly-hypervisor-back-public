package com.provoly.event.dto;

import java.time.Instant;

import com.provoly.comment.CommentReadDto;
import com.provoly.equipment.EquipmentShortDto;
import com.provoly.event.Criticality;
import com.provoly.event.Status;

public class EventReadDto {
    private Integer id;
    private String name;
    private String address;
    private String description;
    private Criticality criticality;
    private String category;
    private String subCategory;
    private Status status;
    private Instant lastModificationDate;
    private Instant creationDate;
    private Instant closeDate;
    private EquipmentShortDto equipment;
    private Integer procedureId;
    private long linkedEvents;
    private float procedureProgress;
    private String domain;
    private Instant startDate;
    private Instant endDate;
    private String externalSourceRef;
    private CommentReadDto lastComment;
    private int commentCount;
    private ParentReadDto parent;

    public EventReadDto(Integer id,
            String name,
            String address,
            String description,
            Criticality criticality,
            String category,
            String subCategory,
            Status status,
            Instant lastModificationDate,
            Instant creationDate,
            Instant closeDate,
            EquipmentShortDto equipment,
            Integer procedureId,
            long linkedEvents,
            float procedureProgress,
            String domain,
            Instant startDate,
            Instant endDate,
            String externalSourceRef,
            CommentReadDto lastComment,
            int commentCount,
            ParentReadDto parent) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.description = description;
        this.criticality = criticality;
        this.category = category;
        this.subCategory = subCategory;
        this.status = status;
        this.lastModificationDate = lastModificationDate;
        this.creationDate = creationDate;
        this.closeDate = closeDate;
        this.equipment = equipment;
        this.procedureId = procedureId;
        this.linkedEvents = linkedEvents;
        this.procedureProgress = procedureProgress;
        this.domain = domain;
        this.startDate = startDate;
        this.endDate = endDate;
        this.externalSourceRef = externalSourceRef;
        this.lastComment = lastComment;
        this.commentCount = commentCount;
        this.parent = parent;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getDescription() {
        return description;
    }

    public Criticality getCriticality() {
        return criticality;
    }

    public String getCategory() {
        return category;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public Status getStatus() {
        return status;
    }

    public Instant getLastModificationDate() {
        return lastModificationDate;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public Instant getCloseDate() {
        return closeDate;
    }

    public EquipmentShortDto getEquipment() {
        return equipment;
    }

    public Integer getProcedureId() {
        return procedureId;
    }

    public long getLinkedEvents() {
        return linkedEvents;
    }

    public float getProcedureProgress() {
        return procedureProgress;
    }

    public String getDomain() {
        return domain;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public String getExternalSourceRef() {
        return externalSourceRef;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public ParentReadDto getParent() {
        return parent;
    }

    public CommentReadDto getLastComment() {
        return lastComment;
    }
}