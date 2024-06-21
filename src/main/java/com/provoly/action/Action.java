package com.provoly.action;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;

import com.provoly.event.Status;
import com.provoly.model.ProcedureModel;
import com.provoly.procedure.Procedure;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Action {
    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    private ActionType type;

    @UpdateTimestamp
    private Instant lastModificationDate;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String name;

    @ManyToOne
    @Immutable
    private Procedure procedure;

    @ManyToOne
    @Immutable
    private ProcedureModel procedureModel;

    public Action() {
        // Only for JPA
    }

    public Action(UUID id) {
        this.id = id;
    }

    protected Action(UUID id, ActionType type, Instant lastModificationDate, Status status, String name) {
        this.id = id;
        this.type = type;
        this.lastModificationDate = lastModificationDate;
        this.status = status;
        this.name = name;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public ActionType getType() {
        return type;
    }

    public Instant getLastModificationDate() {
        return lastModificationDate;
    }

    public Status getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Procedure getProcedure() {
        return procedure;
    }

    public void setProcedure(Procedure procedure) {
        this.procedure = procedure;
    }

    public ProcedureModel getProcedureModel() {
        return procedureModel;
    }

    public void setProcedureModel(ProcedureModel procedureModel) {
        this.procedureModel = procedureModel;
    }
}
